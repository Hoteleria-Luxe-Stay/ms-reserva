package com.hotel.reserva.infrastructure.jobs;

import com.hotel.reserva.core.outbox.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxCleanupJobTest {

    @Mock private OutboxEventRepository outboxRepository;

    @InjectMocks
    private OutboxCleanupJob outboxCleanupJob;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(outboxCleanupJob, "retentionDays", 7);
    }

    @Test
    void runEliminaEventosSentAntiguos() {
        when(outboxRepository.deleteSentBefore(any(LocalDateTime.class))).thenReturn(3);

        outboxCleanupJob.run();

        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(outboxRepository).deleteSentBefore(captor.capture());

        LocalDateTime cutoff = captor.getValue();
        // El cutoff debe ser aproximadamente 7 dias antes de ahora
        assertThat(cutoff).isBefore(LocalDateTime.now().minusDays(6));
    }

    @Test
    void runNoFallaCuandoNoHayEventosParaEliminar() {
        when(outboxRepository.deleteSentBefore(any(LocalDateTime.class))).thenReturn(0);

        outboxCleanupJob.run();

        verify(outboxRepository).deleteSentBefore(any(LocalDateTime.class));
    }
}
