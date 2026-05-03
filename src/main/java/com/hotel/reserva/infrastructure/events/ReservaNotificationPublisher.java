package com.hotel.reserva.infrastructure.events;

import com.hotel.reserva.core.outbox.service.OutboxPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Publisher de eventos de notificacion (consumidos por ms-notificacion).
 *
 * Round 6: en lugar de publicar directo a Kafka (dual-write problem), persiste
 * el evento en la tabla outbox dentro de la transaccion del caller. El job
 * {@code OutboxRelayJob} lo publica a Kafka asincronicamente con garantia
 * at-least-once.
 *
 * IMPORTANTE: el caller (ReservaService, ReservaSagaHandler) DEBE estar dentro
 * de una @Transactional. {@link OutboxPublisher} usa {@code Propagation.MANDATORY}
 * — falla si no hay tx activa, evitando inconsistencias por accidente.
 */
@Component
public class ReservaNotificationPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservaNotificationPublisher.class);
    private static final String AGGREGATE_TYPE = "Reserva";

    private final OutboxPublisher outboxPublisher;
    private final String topic;

    public ReservaNotificationPublisher(OutboxPublisher outboxPublisher,
                                        @Value("${app.kafka.topics.reserva-notifications}") String topic) {
        this.outboxPublisher = outboxPublisher;
        this.topic = topic;
    }

    public void publish(ReservaNotificationEvent event) {
        String aggregateId = event.getReservaId() != null ? event.getReservaId().toString() : null;
        outboxPublisher.publish(
                topic,
                AGGREGATE_TYPE,
                aggregateId,
                event.getEventType(),
                event
        );
        LOGGER.debug("[OUTBOX] Encolado {} reservaId={}",
                event.getEventType(), event.getReservaId());
    }
}
