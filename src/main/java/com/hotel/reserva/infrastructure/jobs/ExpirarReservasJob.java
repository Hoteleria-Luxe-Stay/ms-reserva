package com.hotel.reserva.infrastructure.jobs;

import com.hotel.reserva.core.reserva.model.EstadoReserva;
import com.hotel.reserva.core.reserva.model.Reserva;
import com.hotel.reserva.core.reserva.repository.ReservaRepository;
import com.hotel.reserva.core.reserva.service.ReservaSagaHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Job periodico que expira reservas en PENDIENTE_PAGO cuyo {@code expiresAt} ya paso.
 *
 * IMPORTANTE — solo procesamos PENDIENTE_PAGO, no PAGO_EN_PROCESO.
 *
 * Por que: si una reserva esta en PAGO_EN_PROCESO significa que el usuario clickeo
 * "Pagar", el frontend lo redirigio a Stripe Checkout, y estamos esperando el webhook
 * con el resultado del cobro. Si el job expirara estas reservas, podriamos llegar a
 * marcar EXPIRADA una reserva que JUSTO se cobro y todavia no llego el webhook —
 * race condition que termina en un cliente cobrado sin reserva.
 *
 * Para PAGO_EN_PROCESO confiamos exclusivamente en el webhook de Stripe (que llegara
 * en segundos o, en el peor caso, despues del retry de Stripe). El cliente "expirado"
 * sin webhook se resuelve manualmente con reembolso.
 *
 * Por eso aca filtramos solo PENDIENTE_PAGO.
 */
@Component
public class ExpirarReservasJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpirarReservasJob.class);

    private final ReservaRepository reservaRepository;
    private final ReservaSagaHandler sagaHandler;

    public ExpirarReservasJob(ReservaRepository reservaRepository,
                              ReservaSagaHandler sagaHandler) {
        this.reservaRepository = reservaRepository;
        this.sagaHandler = sagaHandler;
    }

    /**
     * Cada 30 segundos. Sin overlap (fixedDelay).
     * El delay inicial de 30s evita correr durante el arranque/migrations.
     */
    @Scheduled(fixedDelayString = "${app.jobs.expirar-reservas.fixed-delay-ms:30000}",
               initialDelayString = "${app.jobs.expirar-reservas.initial-delay-ms:30000}")
    public void run() {
        List<Reserva> candidatas = reservaRepository.findExpiradas(
                List.of(EstadoReserva.PENDIENTE_PAGO),
                LocalDateTime.now()
        );
        if (candidatas.isEmpty()) {
            return;
        }
        LOGGER.info("[JOB] Expirando {} reserva(s) PENDIENTE_PAGO con expiresAt vencido", candidatas.size());
        for (Reserva r : candidatas) {
            try {
                sagaHandler.expirarReservaPendiente(r.getId());
            } catch (RuntimeException ex) {
                // Una reserva fallida no debe abortar el batch.
                LOGGER.error("[JOB] Error expirando reserva {}: {}", r.getId(), ex.getMessage());
            }
        }
    }
}
