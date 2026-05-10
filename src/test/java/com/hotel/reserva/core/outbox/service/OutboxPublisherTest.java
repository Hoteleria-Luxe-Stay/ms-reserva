package com.hotel.reserva.core.outbox.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.reserva.core.outbox.model.OutboxEvent;
import com.hotel.reserva.core.outbox.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherTest {

    @Mock private OutboxEventRepository outboxRepository;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private OutboxPublisher outboxPublisher;

    @Test
    void publishPersistsOutboxEventConCamposCorrectos() throws Exception {
        String payload = "{\"key\":\"value\"}";
        when(objectMapper.writeValueAsString(any())).thenReturn(payload);
        when(outboxRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        outboxPublisher.publish("test-topic", "Reserva", "42", "CREATED", Map.of("key", "value"));

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxRepository).save(captor.capture());

        OutboxEvent saved = captor.getValue();
        assertThat(saved.getTopic()).isEqualTo("test-topic");
        assertThat(saved.getAggregateType()).isEqualTo("Reserva");
        assertThat(saved.getAggregateId()).isEqualTo("42");
        assertThat(saved.getEventType()).isEqualTo("CREATED");
        assertThat(saved.getPayload()).isEqualTo(payload);
        assertThat(saved.isSent()).isFalse();
        assertThat(saved.getAttempts()).isEqualTo(0);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void publishLanzaIllegalStateExceptionCuandoSerializacionFalla() throws Exception {
        when(objectMapper.writeValueAsString(any()))
                .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("error") {});

        assertThatThrownBy(() ->
                outboxPublisher.publish("topic", "Reserva", "1", "CREATED", new Object())
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No se pudo serializar");

        verify(outboxRepository, never()).save(any());
    }
}
