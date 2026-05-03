-- =========================================================
-- ms-reserva: Baseline schema
-- =========================================================
-- Refleja el estado actual de las entidades JPA:
--   Cliente, Reserva, DetalleReserva
--
-- IMPORTANTE: este V1 es el estado PRE-Ronda 5.1.
--   - 'estado' aún es VARCHAR (todavía no es enum)
--   - todavía NO existe slot table 'habitacion_dia'
--   - todavía NO existe columna 'version' (optimistic locking)
--   - todavía NO existen estados nuevos PENDIENTE_PAGO, PAGO_EN_PROCESO, EXPIRADA, PAGO_FALLIDO
-- Todo eso se agrega en V2__estado_enum_y_slot_table.sql (Ronda 5.1).
-- =========================================================

CREATE TABLE cliente (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    dni       VARCHAR(255),
    nombre    VARCHAR(255),
    apellido  VARCHAR(255),
    email     VARCHAR(255),
    telefono  VARCHAR(255),
    user_id   BIGINT,
    CONSTRAINT pk_cliente PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_cliente_user_id ON cliente (user_id);
CREATE INDEX idx_cliente_dni     ON cliente (dni);

CREATE TABLE reserva (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    fecha_reserva         DATE,
    fecha_inicio          DATE,
    fecha_fin             DATE,
    fecha_cancelacion     DATE,
    total                 DOUBLE       NOT NULL,
    estado                VARCHAR(255),
    motivo_cancelacion    VARCHAR(255),
    hotel_id              BIGINT,
    hotel_nombre          VARCHAR(255),
    hotel_direccion       VARCHAR(255),
    departamento_id       BIGINT,
    departamento_nombre   VARCHAR(255),
    cliente_id            BIGINT,
    CONSTRAINT pk_reserva PRIMARY KEY (id),
    CONSTRAINT fk_reserva_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_reserva_cliente_id   ON reserva (cliente_id);
CREATE INDEX idx_reserva_estado       ON reserva (estado);
CREATE INDEX idx_reserva_fecha_inicio ON reserva (fecha_inicio);
CREATE INDEX idx_reserva_fecha_fin    ON reserva (fecha_fin);

CREATE TABLE detalle_reserva (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    habitacion_id BIGINT,
    precio_noche  DOUBLE       NOT NULL,
    reserva_id    BIGINT,
    CONSTRAINT pk_detalle_reserva PRIMARY KEY (id),
    CONSTRAINT fk_detalle_reserva FOREIGN KEY (reserva_id) REFERENCES reserva (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_detalle_reserva_reserva_id    ON detalle_reserva (reserva_id);
CREATE INDEX idx_detalle_reserva_habitacion_id ON detalle_reserva (habitacion_id);
