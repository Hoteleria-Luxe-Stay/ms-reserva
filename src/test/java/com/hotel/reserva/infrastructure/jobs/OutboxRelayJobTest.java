package com.hotel.reserva.infrastructure.jobs;

import com.hotel.reserva.core.outbox.service.OutboxRelayService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxRelayJobTest {

    @Mock private OutboxRelayService outboxRelayService;

    @InjectMocks
    private OutboxRelayJob outboxRelayJob;

    @Test
    void runInvocaRelayBatch() {
        when(outboxRelayService.relayBatch()).thenReturn(5);

        outboxRelayJob.run();

        verify(outboxRelayService).relayBatch();
    }

    @Test
    void runNoLanzaExcepcionCuandoRelayFalla() {
        when(outboxRelayService.relayBatch()).thenThrow(new RuntimeException("Kafka down"));

        // No debe propagar la excepcion
        outboxRelayJob.run();

        verify(outboxRelayService).relayBatch();
    }
}
