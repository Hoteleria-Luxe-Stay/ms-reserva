package com.hotel.reserva.core.outbox.service;

import com.hotel.reserva.core.outbox.model.OutboxEvent;
import com.hotel.reserva.core.outbox.repository.OutboxEventRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Relay del outbox. Lee unsent con FOR UPDATE SKIP LOCKED, publica a Kafka
 * sincronicamente (.get() del Future) y marca sent=true. Si Kafka falla, la tx
 * hace rollback y el evento queda para el proximo tick.
 *
 * REQUIRES_NEW para aislar el lock del scheduler thread.
 */
@Service
public class OutboxRelayService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutboxRelayService.class);

    private final OutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, String> outboxKafkaTemplate;

    @Value("${app.outbox.batch-size:100}")
    private int batchSize;

    @Value("${app.outbox.send-timeout-ms:5000}")
    private long sendTimeoutMs;

    public OutboxRelayService(OutboxEventRepository outboxRepository,
                              KafkaTemplate<String, String> outboxKafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.outboxKafkaTemplate = outboxKafkaTemplate;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int relayBatch() {
        List<OutboxEvent> batch = outboxRepository.findUnsentBatchForUpdate(batchSize);
        if (batch.isEmpty()) {
            return 0;
        }
        int sent = 0;
        for (OutboxEvent event : batch) {
            try {
                publishToKafka(event);
                event.setSent(true);
                event.setSentAt(LocalDateTime.now());
                event.setLastError(null);
                sent++;
            } catch (Exception ex) {
                event.setAttempts(event.getAttempts() + 1);
                event.setLastError(truncate(ex.getMessage(), 1000));
                LOGGER.error("[OUTBOX] Fallo publicando evento id={} topic={}: {}",
                        event.getId(), event.getTopic(), ex.getMessage());
            }
        }
        outboxRepository.saveAll(batch);
        if (sent > 0) {
            LOGGER.info("[OUTBOX] Publicados {} de {} evento(s) en este batch", sent, batch.size());
        }
        return sent;
    }

    private void publishToKafka(OutboxEvent event) throws Exception {
        ProducerRecord<String, String> record = new ProducerRecord<>(
                event.getTopic(), null, event.getAggregateId(), event.getPayload()
        );
        record.headers().add(new RecordHeader("event-type", bytes(event.getEventType())));
        record.headers().add(new RecordHeader("aggregate-type", bytes(event.getAggregateType())));
        record.headers().add(new RecordHeader("outbox-id", bytes(String.valueOf(event.getId()))));
        outboxKafkaTemplate.send(record).get(sendTimeoutMs, TimeUnit.MILLISECONDS);
    }

    private byte[] bytes(String s) {
        return s == null ? new byte[0] : s.getBytes(StandardCharsets.UTF_8);
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
