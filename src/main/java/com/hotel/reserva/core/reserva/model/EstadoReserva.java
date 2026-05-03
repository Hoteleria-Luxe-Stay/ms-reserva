package com.hotel.reserva.core.reserva.model;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public enum EstadoReserva {
    PENDIENTE_PAGO,
    PAGO_EN_PROCESO,
    CONFIRMADA,
    PAGO_FALLIDO,
    EXPIRADA,
    CANCELADA;

    private static final Map<EstadoReserva, Set<EstadoReserva>> TRANSICIONES = Map.of(
            PENDIENTE_PAGO,  Set.of(PAGO_EN_PROCESO, CANCELADA, EXPIRADA),
            PAGO_EN_PROCESO, Set.of(CONFIRMADA, PAGO_FALLIDO, EXPIRADA),
            CONFIRMADA,      Set.of(CANCELADA),
            PAGO_FALLIDO,    Set.of(),
            EXPIRADA,        Set.of(),
            CANCELADA,       Set.of()
    );

    public boolean puedeTransicionarA(EstadoReserva nuevo) {
        return nuevo != null && TRANSICIONES.get(this).contains(nuevo);
    }

    public boolean esTerminal() {
        return TRANSICIONES.get(this).isEmpty();
    }

    public boolean liberaSlots() {
        return this == CANCELADA || this == EXPIRADA || this == PAGO_FALLIDO;
    }

    public static EstadoReserva fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return EstadoReserva.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Estado de reserva invalido: '" + value + "'. Valores validos: " +
                            Arrays.toString(values())
            );
        }
    }
}
