package com.hotel.reserva.core.reserva.service;

import com.hotel.reserva.api.dto.ReservaAdminUpdateRequest;
import com.hotel.reserva.api.dto.ReservaRequest;
import com.hotel.reserva.api.dto.ReservaUpdateRequest;
import com.hotel.reserva.core.cliente.model.Cliente;
import com.hotel.reserva.core.cliente.service.ClienteService;
import com.hotel.reserva.core.detalle_reserva.model.DetalleReserva;
import com.hotel.reserva.core.habitacion_dia.service.HabitacionDiaService;
import com.hotel.reserva.core.reserva.model.EstadoReserva;
import com.hotel.reserva.core.reserva.model.Reserva;
import com.hotel.reserva.core.reserva.repository.ReservaRepository;
import com.hotel.reserva.helpers.exceptions.BusinessException;
import com.hotel.reserva.helpers.exceptions.ConflictException;
import com.hotel.reserva.helpers.exceptions.EntityNotFoundException;
import com.hotel.reserva.helpers.exceptions.ValidationException;
import com.hotel.reserva.infrastructure.events.ReservaNotificationEvent;
import com.hotel.reserva.infrastructure.events.ReservaNotificationPublisher;
import com.hotel.reserva.internal.HotelInternalApi;
import com.hotel.reserva.internal.PagoInternalApi;
import com.hotel.reserva.internal.dto.CrearPagoInternalRequest;
import com.hotel.reserva.internal.dto.CrearPagoInternalResponse;
import com.hotel.reserva.internal.dto.HabitacionInternalResponse;
import com.hotel.reserva.internal.dto.HotelInternalResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReservaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservaService.class);
    private static final long PAGO_TIMEOUT_MINUTOS = 5;

    private final ReservaRepository reservaRepository;
    private final ClienteService clienteService;
    private final HotelInternalApi hotelInternalApi;
    private final HabitacionDiaService habitacionDiaService;
    private final ReservaNotificationPublisher reservaNotificationPublisher;
    private final PagoInternalApi pagoInternalApi;
    private final TransactionTemplate transactionTemplate;

    @Value("${app.pago.default-currency:USD}")
    private String defaultCurrency;

    @Value("${app.pago.success-url:http://localhost:4200/reservas/success}")
    private String pagoSuccessUrl;

    @Value("${app.pago.cancel-url:http://localhost:4200/reservas/cancel}")
    private String pagoCancelUrl;

    public ReservaService(ReservaRepository reservaRepository,
                          ClienteService clienteService,
                          HotelInternalApi hotelInternalApi,
                          HabitacionDiaService habitacionDiaService,
                          ReservaNotificationPublisher reservaNotificationPublisher,
                          PagoInternalApi pagoInternalApi,
                          PlatformTransactionManager transactionManager) {
        this.reservaRepository = reservaRepository;
        this.clienteService = clienteService;
        this.hotelInternalApi = hotelInternalApi;
        this.habitacionDiaService = habitacionDiaService;
        this.reservaNotificationPublisher = reservaNotificationPublisher;
        this.pagoInternalApi = pagoInternalApi;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public List<Reserva> listarReservas(String dni, String estadoStr) {
        if (dni != null && !dni.isBlank()) {
            return reservaRepository.findByClienteDni(dni);
        }
        EstadoReserva estado = EstadoReserva.fromString(estadoStr);
        if (estado != null) {
            return reservaRepository.findByEstado(estado);
        }
        return reservaRepository.findAll();
    }

    public Reserva buscarPorId(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reserva", id));
    }

    public List<Reserva> buscarReservasPorUsuarioId(Long userId) {
        return reservaRepository.findByClienteUserId(userId);
    }

    public List<Reserva> buscarReservasPorUsuarioIdYFechas(Long userId, LocalDate fechaInicio,
                                                           LocalDate fechaFin, String estadoStr) {
        EstadoReserva estado = EstadoReserva.fromString(estadoStr);
        return reservaRepository.findByUserIdAndFilters(userId, fechaInicio, fechaFin, estado);
    }

    @Transactional
    public Reserva crearReserva(ReservaRequest request, Long userId) {
        validarReservaRequest(request);
        validarFechasAdmin(request.getFechaInicio(), request.getFechaFin());

        HotelInternalResponse hotel = hotelInternalApi.getHotelById(request.getHotelId())
                .orElseThrow(() -> new EntityNotFoundException("Hotel", request.getHotelId()));

        List<HabitacionInternalResponse> habitaciones = validarYObtenerHabitaciones(
                request.getHotelId(),
                request.getHabitacionesIds()
        );

        Cliente cliente = clienteService.crearOActualizar(request.getCliente(), userId);

        long noches = calcularNoches(request.getFechaInicio(), request.getFechaFin());
        double total = calcularTotal(habitaciones, noches);

        Reserva reserva = new Reserva();
        reserva.setFechaReserva(LocalDate.now());
        reserva.setFechaInicio(request.getFechaInicio());
        reserva.setFechaFin(request.getFechaFin());
        reserva.setTotal(total);
        reserva.setEstado(EstadoReserva.PENDIENTE_PAGO);
        reserva.setExpiresAt(LocalDateTime.now().plusMinutes(PAGO_TIMEOUT_MINUTOS));
        reserva.setCliente(cliente);
        aplicarHotelSnapshot(reserva, hotel);

        habitaciones.forEach(habitacion -> {
            DetalleReserva detalle = new DetalleReserva();
            detalle.setHabitacionId(habitacion.getId());
            detalle.setPrecioNoche(habitacion.getPrecio() != null ? habitacion.getPrecio() : 0.0);
            reserva.addDetalle(detalle);
        });

        Reserva savedReserva = reservaRepository.save(reserva);

        // Insertar slots con saveAllAndFlush. La UNIQUE constraint protege contra TOCTOU:
        // si otra transaccion ya inserto un slot para la misma (habitacion, fecha), el INSERT
        // falla con DataIntegrityViolationException → traducimos a 409 Conflict.
        try {
            habitacionDiaService.reservarSlots(
                    savedReserva,
                    request.getHabitacionesIds(),
                    request.getFechaInicio(),
                    request.getFechaFin()
            );
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(
                    "Una o mas habitaciones ya estan reservadas para las fechas seleccionadas",
                    "HABITACION_FECHAS_OCUPADAS"
            );
        }

        publishReservaNotification("CREATED", savedReserva);
        return savedReserva;
    }

    @Transactional
    public Reserva actualizarReservaAdmin(Long id, ReservaAdminUpdateRequest request) {
        Reserva reserva = buscarPorId(id);
        EstadoReserva estadoAnterior = reserva.getEstado();

        validarFechas(request.getFechaInicio(), request.getFechaFin());

        // El admin tambien esta sujeto a la state machine (decision 2B).
        EstadoReserva estadoNuevo = EstadoReserva.fromString(String.valueOf(request.getEstado()));
        if (estadoNuevo != null && estadoNuevo != estadoAnterior) {
            reserva.transicionarA(estadoNuevo);
        }

        reserva.setFechaInicio(request.getFechaInicio());
        reserva.setFechaFin(request.getFechaFin());

        if (request.getHotelId() != null && !request.getHotelId().equals(reserva.getHotelId())) {
            HotelInternalResponse hotel = hotelInternalApi.getHotelById(request.getHotelId())
                    .orElseThrow(() -> new EntityNotFoundException("Hotel", request.getHotelId()));
            aplicarHotelSnapshot(reserva, hotel);
        }

        Cliente cliente = reserva.getCliente();
        cliente.setNombre(request.getCliente().getNombre());
        cliente.setApellido(request.getCliente().getApellido());
        cliente.setEmail(request.getCliente().getEmail());
        cliente.setDni(request.getCliente().getDni());
        cliente.setTelefono(request.getCliente().getTelefono());
        clienteService.guardar(cliente);

        if (request.getHabitaciones() != null && !request.getHabitaciones().isEmpty()) {
            actualizarHabitacionesReserva(reserva, request.getHabitaciones(),
                    request.getFechaInicio(), request.getFechaFin());
        }

        boolean cambioACancelada = reserva.getEstado() == EstadoReserva.CANCELADA
                && estadoAnterior != EstadoReserva.CANCELADA;
        if (cambioACancelada) {
            reserva.setFechaCancelacion(LocalDate.now());
            reserva.setMotivoCancelacion(request.getMotivoCancelacion());
        }

        // Si el estado actual libera slots, los liberamos para que la habitacion
        // vuelva a estar disponible.
        if (reserva.getEstado().liberaSlots()) {
            habitacionDiaService.liberarSlots(reserva.getId());
        }

        Reserva savedReserva = reservaRepository.save(reserva);

        if (cambioACancelada) {
            publishReservaNotification("CANCELLED_ADMIN", savedReserva);
        }

        return savedReserva;
    }

    @Transactional
    public Reserva actualizarFechas(Long id, ReservaUpdateRequest request) {
        Reserva reserva = buscarPorId(id);
        validarFechas(request.getFechaInicio(), request.getFechaFin());

        long noches = calcularNoches(request.getFechaInicio(), request.getFechaFin());
        double nuevoTotal = reserva.getDetalles().stream()
                .mapToDouble(det -> det.getPrecioNoche() * noches)
                .sum();

        reserva.setFechaInicio(request.getFechaInicio());
        reserva.setFechaFin(request.getFechaFin());
        reserva.setTotal(nuevoTotal);

        return reservaRepository.save(reserva);
    }

    /**
     * SAGA — paso 1 del orquestador.
     *
     * Transiciona la reserva a PAGO_EN_PROCESO y crea una Stripe Checkout Session
     * via ms-pago. El metodo NO es @Transactional porque hacemos un call externo
     * largo (Stripe) y no queremos sostener la transaccion DB durante esa I/O.
     *
     * Estructura:
     *   1. {@link #marcarPagoEnProceso(Long)} (transactional, fast) — valida estado
     *      y persiste transicion + commit.
     *   2. Llamada externa a ms-pago (sin transaccion abierta).
     *   3a. Si ok: devolvemos checkoutUrl al frontend.
     *   3b. Si falla: {@link #revertirPagoEnProceso(Long)} (transactional) — vuelve
     *       a PENDIENTE_PAGO para que el usuario pueda reintentar.
     *
     * Idempotencia: garantizada en api-gateway via Idempotency-Key (Round 5.3).
     */
    public CrearPagoInternalResponse iniciarPago(Long id) {
        // Tx 1: validar + transicion + commit (rapido, libera el lock antes de I/O externa).
        Reserva reserva = transactionTemplate.execute(status -> {
            Reserva r = buscarPorId(id);
            try {
                r.transicionarA(EstadoReserva.PAGO_EN_PROCESO);
            } catch (IllegalStateException e) {
                throw new BusinessException(
                        "No se puede iniciar pago en estado " + r.getEstado(),
                        "ESTADO_INVALIDO"
                );
            }
            return reservaRepository.save(r);
        });

        try {
            CrearPagoInternalRequest request = new CrearPagoInternalRequest(
                    reserva.getId(),
                    BigDecimal.valueOf(reserva.getTotal()).setScale(2, RoundingMode.HALF_UP),
                    defaultCurrency,
                    "Reserva #" + reserva.getId() + " - " + safe(reserva.getHotelNombre()),
                    pagoSuccessUrl,
                    pagoCancelUrl
            );
            return pagoInternalApi.crearPago(request);
        } catch (RuntimeException ex) {
            // Tx 2 (compensacion SAGA): revertir el estado para que el usuario pueda reintentar.
            LOGGER.error("Fallo creando pago para reserva {}: {}. Revirtiendo a PENDIENTE_PAGO.",
                    id, ex.getMessage());
            transactionTemplate.executeWithoutResult(status -> {
                reservaRepository.findById(id).ifPresent(r -> {
                    if (r.getEstado() == EstadoReserva.PAGO_EN_PROCESO) {
                        // Bypass state machine porque PAGO_EN_PROCESO -> PENDIENTE_PAGO
                        // no es una transicion permitida (es una compensacion).
                        r.setEstado(EstadoReserva.PENDIENTE_PAGO);
                        reservaRepository.save(r);
                    }
                });
            });
            throw ex;
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    /**
     * Confirma una reserva. Renombrado de 'confirmarPago' a 'confirmar' porque
     * el metodo NO procesa pago real — solo cambia el estado. El cobro real lo
     * gestiona la SAGA con ms-pago + Stripe (Ronda 5.3).
     *
     * Solo es valido transicionar a CONFIRMADA desde PAGO_EN_PROCESO (state machine).
     */
    @Transactional
    public Reserva confirmar(Long id) {
        Reserva reserva = buscarPorId(id);
        try {
            reserva.transicionarA(EstadoReserva.CONFIRMADA);
        } catch (IllegalStateException e) {
            throw new BusinessException(
                    "No se puede confirmar la reserva en estado " + reserva.getEstado(),
                    "ESTADO_INVALIDO"
            );
        }
        // Una reserva confirmada ya no expira por timeout de pago.
        reserva.setExpiresAt(null);

        Reserva savedReserva = reservaRepository.save(reserva);
        publishReservaNotification("CONFIRMED", savedReserva);
        return savedReserva;
    }

    @Transactional
    public Reserva cancelarReserva(Long id) {
        Reserva reserva = buscarPorId(id);
        try {
            reserva.transicionarA(EstadoReserva.CANCELADA);
        } catch (IllegalStateException e) {
            throw new BusinessException(
                    "No se puede cancelar la reserva en estado " + reserva.getEstado(),
                    "ESTADO_INVALIDO"
            );
        }
        reserva.setFechaCancelacion(LocalDate.now());
        reserva.setMotivoCancelacion("Cancelada por el usuario");

        habitacionDiaService.liberarSlots(reserva.getId());

        Reserva savedReserva = reservaRepository.save(reserva);
        publishReservaNotification("CANCELLED", savedReserva);
        return savedReserva;
    }

    @Transactional
    public void eliminar(Long id) {
        if (!reservaRepository.existsById(id)) {
            throw new EntityNotFoundException("Reserva", id);
        }
        // habitacion_dia tiene FK ON DELETE CASCADE, los slots se liberan automaticamente.
        reservaRepository.deleteById(id);
    }

    // ---------- Helpers privados ----------

    private void validarReservaRequest(ReservaRequest dto) {
        if (dto.getHotelId() == null) {
            throw new ValidationException("hotelId", "El hotel es requerido");
        }
        if (dto.getFechaInicio() == null) {
            throw new ValidationException("fechaInicio", "La fecha de inicio es requerida");
        }
        if (dto.getFechaFin() == null) {
            throw new ValidationException("fechaFin", "La fecha de fin es requerida");
        }
        if (dto.getHabitacionesIds() == null || dto.getHabitacionesIds().isEmpty()) {
            throw new ValidationException("habitacionesIds", "Debe seleccionar al menos una habitacion");
        }
        if (dto.getCliente() == null) {
            throw new ValidationException("cliente", "Los datos del cliente son requeridos");
        }
    }

    private void validarFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDate hoy = LocalDate.now();
        LocalDate maniana = hoy.plusDays(1);

        if (fechaInicio.isBefore(maniana)) {
            throw new BusinessException(
                    "Las reservas deben realizarse con al menos 24 horas de anticipacion. La fecha minima de check-in es: " + maniana,
                    "FECHA_MINIMA_24_HORAS"
            );
        }
        if (fechaFin.isBefore(fechaInicio) || fechaFin.isEqual(fechaInicio)) {
            throw new BusinessException(
                    "La fecha de salida debe ser posterior a la fecha de entrada",
                    "FECHA_FIN_INVALIDA"
            );
        }
    }

    private void validarFechasAdmin(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaFin.isBefore(fechaInicio) || fechaFin.isEqual(fechaInicio)) {
            throw new BusinessException(
                    "La fecha de salida debe ser posterior a la fecha de entrada",
                    "FECHA_FIN_INVALIDA"
            );
        }
    }

    /**
     * Valida que las habitaciones existan y pertenezcan al hotel. La verificacion
     * de disponibilidad por fechas YA NO se hace aca: la slot table 'habitacion_dia'
     * con UNIQUE(habitacion_id, fecha) es la fuente de verdad y se valida atomicamente
     * en el INSERT (anti-TOCTOU sin race conditions).
     */
    private List<HabitacionInternalResponse> validarYObtenerHabitaciones(Long hotelId, List<Long> habitacionesIds) {
        return habitacionesIds.stream().map(habitacionId -> {
            HabitacionInternalResponse habitacion = hotelInternalApi.getHabitacionById(habitacionId)
                    .orElseThrow(() -> new EntityNotFoundException("Habitacion", habitacionId));

            if (habitacion.getHotelId() != null && !habitacion.getHotelId().equals(hotelId)) {
                throw new BusinessException(
                        "La habitacion " + habitacionId + " no pertenece al hotel seleccionado",
                        "HABITACION_HOTEL_INCORRECTO"
                );
            }

            if (!hotelInternalApi.checkDisponibilidad(habitacionId)) {
                throw new ConflictException(
                        "La habitacion " + habitacionId + " no esta disponible (estado operativo)",
                        "HABITACION_NO_DISPONIBLE"
                );
            }

            return habitacion;
        }).toList();
    }

    private long calcularNoches(LocalDate fechaInicio, LocalDate fechaFin) {
        long noches = ChronoUnit.DAYS.between(fechaInicio, fechaFin);
        return noches <= 0 ? 1 : noches;
    }

    private double calcularTotal(List<HabitacionInternalResponse> habitaciones, long noches) {
        return habitaciones.stream()
                .mapToDouble(h -> (h.getPrecio() != null ? h.getPrecio() : 0.0) * noches)
                .sum();
    }

    private void actualizarHabitacionesReserva(Reserva reserva, List<Long> habitacionesIds,
                                               LocalDate inicio, LocalDate fin) {
        reserva.getDetalles().clear();

        long noches = calcularNoches(inicio, fin);
        double total = 0;

        for (Long habitacionId : habitacionesIds) {
            HabitacionInternalResponse habitacion = hotelInternalApi.getHabitacionById(habitacionId)
                    .orElseThrow(() -> new EntityNotFoundException("Habitacion", habitacionId));

            DetalleReserva detalle = new DetalleReserva();
            detalle.setReserva(reserva);
            detalle.setHabitacionId(habitacionId);
            detalle.setPrecioNoche(habitacion.getPrecio() != null ? habitacion.getPrecio() : 0.0);
            reserva.getDetalles().add(detalle);

            total += (habitacion.getPrecio() != null ? habitacion.getPrecio() : 0.0) * noches;
        }

        reserva.setTotal(total);
    }

    private void aplicarHotelSnapshot(Reserva reserva, HotelInternalResponse hotel) {
        reserva.setHotelId(hotel.getId());
        reserva.setHotelNombre(hotel.getNombre());
        reserva.setHotelDireccion(hotel.getDireccion());
        if (hotel.getDepartamento() != null) {
            reserva.setDepartamentoId(hotel.getDepartamento().getId());
            reserva.setDepartamentoNombre(hotel.getDepartamento().getNombre());
        }
    }

    private void publishReservaNotification(String eventType, Reserva reserva) {
        Cliente cliente = reserva.getCliente();
        List<ReservaNotificationEvent.HabitacionDetalle> habitaciones = reserva.getDetalles().stream()
                .map(detalle -> new ReservaNotificationEvent.HabitacionDetalle(
                        detalle.getHabitacionId(),
                        detalle.getPrecioNoche()
                ))
                .toList();

        ReservaNotificationEvent event = new ReservaNotificationEvent(
                eventType,
                reserva.getId(),
                cliente != null ? cliente.getUserId() : null,
                cliente != null ? cliente.getNombre() + " " + cliente.getApellido() : null,
                cliente != null ? cliente.getEmail() : null,
                reserva.getHotelNombre(),
                reserva.getHotelDireccion(),
                reserva.getFechaInicio() != null ? reserva.getFechaInicio().toString() : null,
                reserva.getFechaFin() != null ? reserva.getFechaFin().toString() : null,
                reserva.getFechaCancelacion() != null ? reserva.getFechaCancelacion().toString() : null,
                reserva.getTotal(),
                reserva.getEstado() != null ? reserva.getEstado().name() : null,
                reserva.getMotivoCancelacion(),
                habitaciones
        );
        reservaNotificationPublisher.publish(event);
    }
}
