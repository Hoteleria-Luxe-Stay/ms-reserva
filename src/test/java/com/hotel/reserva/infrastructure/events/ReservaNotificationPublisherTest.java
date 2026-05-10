package com.hotel.reserva.infrastructure.events;

import com.hotel.reserva.core.outbox.service.OutboxPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReservaNotificationPublisherTest {

    @Mock private OutboxPublisher outboxPublisher;

    @InjectMocks
    private ReservaNotificationPublisher publisher;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(publisher, "topic", "reserva.notifications");
    }

    @Test
    void publishLlamaOutboxPublisherConParametrosCorrectos() {
        ReservaNotificationEvent event = new ReservaNotificationEvent(
                "CREATED", 1L, 42L, "Juan Perez", "juan@test.com",
                "Hotel Luxe", "Calle 123",
                "2026-06-01", "2026-06-05", null,
                400.0, "PENDIENTE_PAGO", null,
                List.of()
        );

        publisher.publish(event);

        verify(outboxPublisher).publish(
                eq("reserva.notifications"),
                eq("Reserva"),
                eq("1"),
                eq("CREATED"),
                eq(event)
        );
    }

    @Test
    void publishConReservaIdNuloUsaAggregateIdNulo() {
        ReservaNotificationEvent event = new ReservaNotificationEvent(
                "CREATED", null, 42L, "Juan Perez", "juan@test.com",
                "Hotel Luxe", "Calle 123",
                "2026-06-01", "2026-06-05", null,
                400.0, "PENDIENTE_PAGO", null,
                List.of()
        );

        publisher.publish(event);

        verify(outboxPublisher).publish(
                eq("reserva.notifications"),
                eq("Reserva"),
                isNull(),
                eq("CREATED"),
                eq(event)
        );
    }
}
