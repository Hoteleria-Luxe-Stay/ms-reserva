package com.hotel.reserva.core.outbox.repository;

import com.hotel.reserva.core.outbox.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * Lee un lote de eventos no enviados con FOR UPDATE SKIP LOCKED.
     * Requiere MySQL 8.0+.
     */
    @Query(value = """
            SELECT * FROM outbox_event
            WHERE sent = 0 AND dead = 0
            ORDER BY id ASC
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEvent> findUnsentBatchForUpdate(@Param("batchSize") int batchSize);

    @Modifying
    @Query("DELETE FROM OutboxEvent o WHERE o.sent = true AND o.sentAt < :cutoff")
    int deleteSentBefore(@Param("cutoff") LocalDateTime cutoff);
}
