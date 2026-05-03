package com.hotel.reserva.infrastructure.events;

import com.hotel.reserva.core.reserva.service.ReservaSagaHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumer del topic 'pago.events' que publica ms-pago.
 *
 * Este consumer es la SEGUNDA mitad del SAGA orchestrator: ms-reserva inicio el pago,
 * el usuario paga (o no) en Stripe, ms-pago recibe el webhook y publica el evento,
 * y ESTE listener cierra el ciclo transicionando la reserva.
 *
 * Idempotencia: el handler verifica el estado actual de la reserva antes de
 * transicionar, asi reintentos del consumer (rebalance, redelivery) no rompen.
 */
@Component
public class PagoEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PagoEventListener.class);

    private final ReservaSagaHandler sagaHandler;

    public PagoEventListener(ReservaSagaHandler sagaHandler) {
        this.sagaHandler = sagaHandler;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.pago-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "pagoEventListenerContainerFactory"
    )
    public void onPagoEvent(PagoEvent event) {
        if (event == null || event.getEventType() == null || event.getReservaId() == null) {
            LOGGER.warn("[KAFKA] PagoEvent invalido recibido: {}", event);
            return;
        }
        LOGGER.info("[KAFKA] PagoEvent recibido: eventType={} reservaId={} pagoId={}",
                event.getEventType(), event.getReservaId(), event.getPagoId());

        switch (event.getEventType()) {
            case "PagoAprobado" -> sagaHandler.aplicarPagoAprobado(event.getReservaId());
            case "PagoRechazado" -> sagaHandler.aplicarPagoRechazado(
                    event.getReservaId(),
                    event.getErrorMessage()
            );
            default -> LOGGER.debug("[KAFKA] Tipo de PagoEvent ignorado: {}", event.getEventType());
        }
    }
}
