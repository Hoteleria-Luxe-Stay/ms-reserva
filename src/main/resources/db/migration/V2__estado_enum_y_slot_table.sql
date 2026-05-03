-- =========================================================
-- ms-reserva V2: estado como enum string (string),
--                @Version (optimistic lock),
--                expires_at (timeout PENDIENTE_PAGO 5 min),
--                slot table 'habitacion_dia' con UNIQUE (anti-TOCTOU).
-- =========================================================

-- ---------------------------------------------------------
-- Slot table: una fila por (habitacion, fecha-noche). El UNIQUE
-- compuesto rechaza atomicamente cualquier doble reserva.
-- ---------------------------------------------------------
CREATE TABLE habitacion_dia (
    id            BIGINT NOT NULL AUTO_INCREMENT,
    habitacion_id BIGINT NOT NULL,
    fecha         DATE   NOT NULL,
    reserva_id    BIGINT NOT NULL,
    CONSTRAINT pk_habitacion_dia PRIMARY KEY (id),
    CONSTRAINT uk_habitacion_dia UNIQUE (habitacion_id, fecha),
    CONSTRAINT fk_habitacion_dia_reserva FOREIGN KEY (reserva_id)
        REFERENCES reserva (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_habitacion_dia_reserva_id ON habitacion_dia (reserva_id);
CREATE INDEX idx_habitacion_dia_habitacion ON habitacion_dia (habitacion_id);
CREATE INDEX idx_habitacion_dia_fecha      ON habitacion_dia (fecha);

-- ---------------------------------------------------------
-- Reserva: agregar columnas para optimistic lock + timeout.
-- ---------------------------------------------------------
ALTER TABLE reserva
    ADD COLUMN version    BIGINT      NOT NULL DEFAULT 0,
    ADD COLUMN expires_at DATETIME(6) NULL;

CREATE INDEX idx_reserva_expires_at ON reserva (expires_at);

-- ---------------------------------------------------------
-- Migracion de valores antiguos. En DB limpia no afecta nada,
-- pero queda documentado para futuras aplicaciones sobre datos.
-- ---------------------------------------------------------
UPDATE reserva SET estado = 'PENDIENTE_PAGO' WHERE estado = 'PENDIENTE';
-- 'CONFIRMADA' y 'CANCELADA' se mantienen.

-- ---------------------------------------------------------
-- Ajustar tipo y obligatoriedad para alinear con el enum.
-- ---------------------------------------------------------
ALTER TABLE reserva
    MODIFY COLUMN estado VARCHAR(32) NOT NULL;
