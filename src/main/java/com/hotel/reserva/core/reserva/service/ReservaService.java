package com.hotel.reserva.core.reserva.service;

import com.hotel.reserva.api.dto.ReservaAdminUpdateRequest;
import com.hotel.reserva.api.dto.ReservaRequest;
import com.hotel.reserva.api.dto.ReservaUpdateRequest;
import com.hotel.reserva.core.cliente.model.Cliente;
import com.hotel.reserva.core.cliente.service.ClienteService;
import com.hotel.reserva.core.detalle_reserva.model.DetalleReserva;
import com.hotel.reserva.core.reserva.model.Reserva;
import com.hotel.reserva.core.reserva.repository.ReservaRepository;
import com.hotel.reserva.helpers.exceptions.BusinessException;
import com.hotel.reserva.helpers.exceptions.ConflictException;
import com.hotel.reserva.helpers.exceptions.EntityNotFoundException;
import com.hotel.reserva.helpers.exceptions.ValidationException;
import com.hotel.reserva.internal.HotelInternalApi;
import com.hotel.reserva.internal.dto.HabitacionInternalResponse;
import com.hotel.reserva.internal.dto.HotelInternalResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReservaService {

    private static final String ESTADO_PENDIENTE = "PENDIENTE";
    private static final String ESTADO_CONFIRMADA = "CONFIRMADA";
    private static final String ESTADO_CANCELADA = "CANCELADA";

    private final ReservaRepository reservaRepository;
    private final ClienteService clienteService;
    private final HotelInternalApi hotelInternalApi;

    public ReservaService(ReservaRepository reservaRepository,
                          ClienteService clienteService,
                          HotelInternalApi hotelInternalApi) {
        this.reservaRepository = reservaRepository;
        this.clienteService = clienteService;
        this.hotelInternalApi = hotelInternalApi;
    }

    public List<Reserva> listarReservas(String dni, String estado) {
        if (dni != null && !dni.isBlank()) {
            return reservaRepository.findByClienteDni(dni);
        }
        if (estado != null && !estado.isBlank()) {
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

    public List<Reserva> buscarReservasPorUsuarioIdYFechas(Long userId, LocalDate fechaInicio, LocalDate fechaFin, String estado) {
        List<Reserva> reservas = reservaRepository.findByClienteUserId(userId);

        if (estado != null && !estado.isBlank()) {
            reservas = reservas.stream()
                    .filter(reserva -> estado.equals(reserva.getEstado()))
                    .toList();
        }

        if (fechaInicio != null && fechaFin != null) {
            reservas = reservas.stream()
                    .filter(r -> !r.getFechaInicio().isBefore(fechaInicio) && !r.getFechaFin().isAfter(fechaFin))
                    .toList();
        } else if (fechaInicio != null) {
            reservas = reservas.stream()
                    .filter(r -> !r.getFechaInicio().isBefore(fechaInicio))
                    .toList();
        } else if (fechaFin != null) {
            reservas = reservas.stream()
                    .filter(r -> !r.getFechaFin().isAfter(fechaFin))
                    .toList();
        }

        return reservas;
    }

    @Transactional
    public Reserva crearReserva(ReservaRequest request, Long userId) {
        validarReservaRequest(request);
        validarFechas(request.getFechaInicio(), request.getFechaFin());

        HotelInternalResponse hotel = hotelInternalApi.getHotelById(request.getHotelId())
                .orElseThrow(() -> new EntityNotFoundException("Hotel", request.getHotelId()));

        List<HabitacionInternalResponse> habitaciones = validarYObtenerHabitaciones(
                request.getHotelId(),
                request.getHabitacionesIds(),
                request.getFechaInicio(),
                request.getFechaFin()
        );

        Cliente cliente = clienteService.crearOActualizar(request.getCliente(), userId);

        long noches = calcularNoches(request.getFechaInicio(), request.getFechaFin());
        double total = calcularTotal(habitaciones, noches);

        Reserva reserva = new Reserva();
        reserva.setFechaReserva(LocalDate.now());
        reserva.setFechaInicio(request.getFechaInicio());
        reserva.setFechaFin(request.getFechaFin());
        reserva.setTotal(total);
        reserva.setEstado(ESTADO_PENDIENTE);
        reserva.setCliente(cliente);
        aplicarHotelSnapshot(reserva, hotel);

        habitaciones.forEach(habitacion -> {
            DetalleReserva detalle = new DetalleReserva();
            detalle.setHabitacionId(habitacion.getId());
            detalle.setPrecioNoche(habitacion.getPrecio() != null ? habitacion.getPrecio() : 0.0);
            reserva.addDetalle(detalle);
        });

        return reservaRepository.save(reserva);
    }

    @Transactional
    public Reserva actualizarReservaAdmin(Long id, ReservaAdminUpdateRequest request) {
        Reserva reserva = buscarPorId(id);

        validarFechas(request.getFechaInicio(), request.getFechaFin());

        reserva.setFechaInicio(request.getFechaInicio());
        reserva.setFechaFin(request.getFechaFin());
        reserva.setEstado(String.valueOf(request.getEstado()));

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
            actualizarHabitacionesReserva(reserva, request.getHabitaciones(), request.getFechaInicio(), request.getFechaFin());
        }

        return reservaRepository.save(reserva);
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

    @Transactional
    public Reserva confirmarPago(Long id) {
        Reserva reserva = buscarPorId(id);
        if (!ESTADO_PENDIENTE.equals(reserva.getEstado())) {
            throw new BusinessException(
                    "Solo se pueden confirmar reservas pendientes. Estado actual: " + reserva.getEstado(),
                    "ESTADO_INVALIDO"
            );
        }
        reserva.setEstado(ESTADO_CONFIRMADA);
        return reservaRepository.save(reserva);
    }

    @Transactional
    public Reserva cancelarReserva(Long id) {
        Reserva reserva = buscarPorId(id);
        reserva.setEstado(ESTADO_CANCELADA);
        return reservaRepository.save(reserva);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!reservaRepository.existsById(id)) {
            throw new EntityNotFoundException("Reserva", id);
        }
        reservaRepository.deleteById(id);
    }

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
            throw new ValidationException("habitacionesIds", "Debe seleccionar al menos una habitación");
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
                    "Las reservas deben realizarse con al menos 24 horas de anticipación. La fecha mínima de check-in es: " + maniana,
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

    private List<HabitacionInternalResponse> validarYObtenerHabitaciones(
            Long hotelId,
            List<Long> habitacionesIds,
            LocalDate inicio,
            LocalDate fin) {
        return habitacionesIds.stream().map(habitacionId -> {
            HabitacionInternalResponse habitacion = hotelInternalApi.getHabitacionById(habitacionId)
                    .orElseThrow(() -> new EntityNotFoundException("Habitacion", habitacionId));

            if (habitacion.getHotelId() != null && !habitacion.getHotelId().equals(hotelId)) {
                throw new BusinessException(
                        "La habitación " + habitacionId + " no pertenece al hotel seleccionado",
                        "HABITACION_HOTEL_INCORRECTO"
                );
            }

            if (!hotelInternalApi.checkDisponibilidad(habitacionId, inicio, fin)) {
                throw new ConflictException(
                        "La habitación " + habitacionId + " no está disponible para las fechas seleccionadas",
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

    private void actualizarHabitacionesReserva(Reserva reserva, List<Long> habitacionesIds, LocalDate inicio, LocalDate fin) {
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
}
