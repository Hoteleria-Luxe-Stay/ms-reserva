package com.hotel.reserva.infrastructure.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReservaNotificationPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservaNotificationPublisher.class);

    private final KafkaTemplate<String, ReservaNotificationEvent> kafkaTemplate;

    @Value("${app.kafka.topics.reserva-notifications}")
    private String topic;

    public ReservaNotificationPublisher(KafkaTemplate<String, ReservaNotificationEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(ReservaNotificationEvent event) {
        try {
            String key = event.getReservaId() != null ? String.valueOf(event.getReservaId()) : null;
            kafkaTemplate.send(topic, key, event);
            LOGGER.info("[KAFKA] ReservaNotification published for reservaId: {}", event.getReservaId());
        } catch (Exception e) {
            LOGGER.error("[KAFKA] Error publishing ReservaNotification: {}", e.getMessage(), e);
        }
    }
}
