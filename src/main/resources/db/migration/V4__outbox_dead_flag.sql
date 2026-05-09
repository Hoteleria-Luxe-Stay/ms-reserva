-- ============================================================================
-- Outbox dead-letter flag.
--
-- Cuando un evento supera app.outbox.max-attempts intentos fallidos de
-- publicacion a Kafka, se marca dead=1 y se deja de reintentar. Esto evita
-- que un outage prolongado de Kafka llene el outbox indefinidamente.
--
-- Operacion: monitorear filas con dead=1 — son eventos que requieren
-- intervencion manual (republicar, descartar, alerta).
-- ============================================================================

ALTER TABLE outbox_event
    ADD COLUMN dead BIT(1) NOT NULL DEFAULT b'0' AFTER last_error;

ALTER TABLE outbox_event
    DROP INDEX idx_outbox_unsent,
    ADD KEY idx_outbox_unsent_alive (sent, dead, id);
