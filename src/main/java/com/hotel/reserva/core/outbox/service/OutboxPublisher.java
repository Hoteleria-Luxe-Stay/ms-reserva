package com.hotel.reserva.core.outbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.reserva.core.outbox.model.OutboxEvent;
import com.hotel.reserva.core.outbox.repository.OutboxEventRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Punto unico de emision de eventos de dominio.
 *
 * Persiste en outbox_event dentro de la transaccion del caller. Si la tx hace
 * rollback, el evento NO queda. Un relay job lo publica a Kafka asincronicamente.
 *
 * {@link Propagation#MANDATORY} obliga a que exista una tx activa — falla
 * explicitamente si alguien llama desde un metodo sin @Transactional.
 */
@Component
public class OutboxPublisher {

    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(OutboxEventRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(String topic,
                        String aggregateType,
                        String aggregateId,
                        String eventType,
                        Object payload) {
        String json = serialize(payload);
        OutboxEvent event = new OutboxEvent();
        event.setTopic(topic);
        event.setAggregateType(aggregateType);
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setPayload(json);
        event.setCreatedAt(LocalDateTime.now());
        event.setSent(false);
        event.setAttempts(0);
        outboxRepository.save(event);
    }

    private String serialize(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "No se pudo serializar payload de outbox: " + payload.getClass().getName(), e
            );
        }
    }
}
