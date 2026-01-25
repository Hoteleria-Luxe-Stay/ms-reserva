package com.hotel.reserva.infrastructure.events;

import com.hotel.reserva.infrastructure.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishReservaCreated(ReservaCreatedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.EVENTS_EXCHANGE,
                    RabbitConfig.RESERVA_CREATED_ROUTING_KEY,
                    event
            );
            LOGGER.info("[EVENT] ReservaCreated published for reservaId: {}", event.getReservaId());
        } catch (Exception e) {
            LOGGER.error("[EVENT] Error publishing ReservaCreated event: {}", e.getMessage(), e);
        }
    }

    public void publishReservaConfirmed(ReservaConfirmedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.EVENTS_EXCHANGE,
                    RabbitConfig.RESERVA_CONFIRMED_ROUTING_KEY,
                    event
            );
            LOGGER.info("[EVENT] ReservaConfirmed published for reservaId: {}", event.getReservaId());
        } catch (Exception e) {
            LOGGER.error("[EVENT] Error publishing ReservaConfirmed event: {}", e.getMessage(), e);
        }
    }

    public void publishReservaCancelled(ReservaCancelledEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.EVENTS_EXCHANGE,
                    RabbitConfig.RESERVA_CANCELLED_ROUTING_KEY,
                    event
            );
            LOGGER.info("[EVENT] ReservaCancelled published for reservaId: {}", event.getReservaId());
        } catch (Exception e) {
            LOGGER.error("[EVENT] Error publishing ReservaCancelled event: {}", e.getMessage(), e);
        }
    }
}
