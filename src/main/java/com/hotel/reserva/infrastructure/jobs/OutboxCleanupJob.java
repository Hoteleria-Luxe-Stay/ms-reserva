package com.hotel.reserva.infrastructure.jobs;

import com.hotel.reserva.core.outbox.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class OutboxCleanupJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutboxCleanupJob.class);

    private final OutboxEventRepository outboxRepository;

    @Value("${app.outbox.cleanup.retention-days:7}")
    private int retentionDays;

    public OutboxCleanupJob(OutboxEventRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    @Scheduled(cron = "${app.outbox.cleanup.cron:0 0 3 * * *}")
    @Transactional
    public void run() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        int deleted = outboxRepository.deleteSentBefore(cutoff);
        if (deleted > 0) {
            LOGGER.info("[OUTBOX-CLEANUP] Eliminados {} eventos sent con sentAt < {}",
                    deleted, cutoff);
        }
    }
}
