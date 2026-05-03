package com.hotel.reserva.infrastructure.jobs;

import com.hotel.reserva.core.outbox.service.OutboxRelayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxRelayJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutboxRelayJob.class);

    private final OutboxRelayService outboxRelayService;

    public OutboxRelayJob(OutboxRelayService outboxRelayService) {
        this.outboxRelayService = outboxRelayService;
    }

    @Scheduled(fixedDelayString = "${app.outbox.relay.fixed-delay-ms:2000}",
               initialDelayString = "${app.outbox.relay.initial-delay-ms:5000}")
    public void run() {
        try {
            outboxRelayService.relayBatch();
        } catch (RuntimeException ex) {
            LOGGER.error("[OUTBOX] Error en tick del relay: {}", ex.getMessage());
        }
    }
}
