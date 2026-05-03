package com.hotel.reserva.core.reserva.service;

import com.hotel.reserva.core.cliente.model.Cliente;
import com.hotel.reserva.core.detalle_reserva.model.DetalleReserva;
import com.hotel.reserva.core.habitacion_dia.service.HabitacionDiaService;
import com.hotel.reserva.core.reserva.model.EstadoReserva;
import com.hotel.reserva.core.reserva.model.Reserva;
import com.hotel.reserva.core.reserva.repository.ReservaRepository;
import com.hotel.reserva.infrastructure.events.ReservaNotificationEvent;
import com.hotel.reserva.infrastructure.events.ReservaNotificationPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handler dedicado a las transiciones de estado disparadas por la SAGA
 * (eventos pago.events o expiracion por timeout).
 *
 * Por que un bean separado de ReservaService: cada metodo @Transactional
 * tiene que ser invocado a traves del proxy de Spring. Tener este handler
 * como bean independiente garantiza que cuando el KafkaListener o el
 * @Scheduled lo llamen, el proxy intercepta correctamente.
 *
 * Idempotencia: cada metodo verifica el estado actual antes de transicionar.
 * Si la reserva ya esta en el estado destino (o un terminal), no hace nada
 * — esto cubre redelivery de Kafka y race con webhook duplicado.
 */
@Service
public class ReservaSagaHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservaSagaHandler.class);

    private final ReservaRepository reservaRepository;
    private final HabitacionDiaService habitacionDiaService;
    private final ReservaNotificationPublisher reservaNotificationPublisher;

    public ReservaSagaHandler(ReservaRepository reservaRepository,
                              HabitacionDiaService habitacionDiaService,
                              ReservaNotificationPublisher reservaNotificationPublisher) {
        this.reservaRepository = reservaRepository;
        this.habitacionDiaService = habitacionDiaService;
        this.reservaNotificationPublisher = reservaNotificationPublisher;
    }

    @Transactional
    public void aplicarPagoAprobado(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId).orElse(null);
        if (reserva == null) {
            LOGGER.warn("[SAGA] PagoAprobado para reserva inexistente: {}", reservaId);
            return;
        }
        if (reserva.getEstado() == EstadoReserva.CONFIRMADA) {
            LOGGER.info("[SAGA] PagoAprobado idempotente: reserva {} ya estaba CONFIRMADA", reservaId);
            return;
        }
        if (reserva.getEstado().esTerminal() && reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            LOGGER.warn("[SAGA] PagoAprobado para reserva {} en estado terminal {} — ignorado, " +
                    "posible doble pago, requiere reembolso manual",
                    reservaId, reserva.getEstado());
            return;
        }
        try {
            reserva.transicionarA(EstadoReserva.CONFIRMADA);
        } catch (IllegalStateException e) {
            LOGGER.error("[SAGA] No se pudo confirmar reserva {} desde estado {}: {}",
                    reservaId, reserva.getEstado(), e.getMessage());
            return;
        }
        reserva.setExpiresAt(null);
        Reserva saved = reservaRepository.save(reserva);
        publishNotification("CONFIRMED", saved);
        LOGGER.info("[SAGA] Reserva {} confirmada por pago aprobado", reservaId);
    }

    @Transactional
    public void aplicarPagoRechazado(Long reservaId, String errorMessage) {
        Reserva reserva = reservaRepository.findById(reservaId).orElse(null);
        if (reserva == null) {
            LOGGER.warn("[SAGA] PagoRechazado para reserva inexistente: {}", reservaId);
            return;
        }
        if (reserva.getEstado() == EstadoReserva.PAGO_FALLIDO
                || reserva.getEstado().esTerminal()) {
            LOGGER.info("[SAGA] PagoRechazado idempotente o reserva ya cerrada: {} estado={}",
                    reservaId, reserva.getEstado());
            return;
        }
        try {
            reserva.transicionarA(EstadoReserva.PAGO_FALLIDO);
        } catch (IllegalStateException e) {
            LOGGER.error("[SAGA] No se pudo marcar PAGO_FALLIDO en reserva {} desde estado {}: {}",
                    reservaId, reserva.getEstado(), e.getMessage());
            return;
        }
        reserva.setExpiresAt(null);
        if (errorMessage != null) {
            reserva.setMotivoCancelacion(errorMessage);
        }
        habitacionDiaService.liberarSlots(reservaId);
        Reserva saved = reservaRepository.save(reserva);
        publishNotification("PAYMENT_FAILED", saved);
        LOGGER.info("[SAGA] Reserva {} marcada PAGO_FALLIDO + slots liberados", reservaId);
    }

    @Transactional
    public void expirarReservaPendiente(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId).orElse(null);
        if (reserva == null) {
            return;
        }
        // Solo expiramos PENDIENTE_PAGO. Si esta en PAGO_EN_PROCESO el usuario ya
        // arranco el flujo en Stripe — el webhook va a resolver la SAGA.
        if (reserva.getEstado() != EstadoReserva.PENDIENTE_PAGO) {
            return;
        }
        try {
            reserva.transicionarA(EstadoReserva.EXPIRADA);
        } catch (IllegalStateException e) {
            LOGGER.warn("[SAGA] No se pudo expirar reserva {}: {}", reservaId, e.getMessage());
            return;
        }
        reserva.setExpiresAt(null);
        habitacionDiaService.liberarSlots(reservaId);
        Reserva saved = reservaRepository.save(reserva);
        publishNotification("EXPIRED", saved);
        LOGGER.info("[SAGA] Reserva {} expirada por timeout de pago", reservaId);
    }

    private void publishNotification(String eventType, Reserva reserva) {
        Cliente cliente = reserva.getCliente();
        List<ReservaNotificationEvent.HabitacionDetalle> habitaciones = reserva.getDetalles().stream()
                .map(d -> new ReservaNotificationEvent.HabitacionDetalle(
                        d.getHabitacionId(), d.getPrecioNoche()
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
