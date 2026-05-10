package com.hotel.reserva.core.reserva.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EstadoReservaTest {

    // ==================== puedeTransicionarA ====================

    @Test
    void pendientePagoPuedeTransicionarAPagoEnProceso() {
        assertThat(EstadoReserva.PENDIENTE_PAGO.puedeTransicionarA(EstadoReserva.PAGO_EN_PROCESO)).isTrue();
    }

    @Test
    void pendientePagoPuedeTransicionarACancelada() {
        assertThat(EstadoReserva.PENDIENTE_PAGO.puedeTransicionarA(EstadoReserva.CANCELADA)).isTrue();
    }

    @Test
    void pendientePagoPuedeTransicionarAExpirada() {
        assertThat(EstadoReserva.PENDIENTE_PAGO.puedeTransicionarA(EstadoReserva.EXPIRADA)).isTrue();
    }

    @Test
    void pendientePagoNoPuedeTransicionarAConfirmada() {
        assertThat(EstadoReserva.PENDIENTE_PAGO.puedeTransicionarA(EstadoReserva.CONFIRMADA)).isFalse();
    }

    @Test
    void pagoEnProcesoPuedeTransicionarAConfirmada() {
        assertThat(EstadoReserva.PAGO_EN_PROCESO.puedeTransicionarA(EstadoReserva.CONFIRMADA)).isTrue();
    }

    @Test
    void pagoEnProcesoPuedeTransicionarAPagoFallido() {
        assertThat(EstadoReserva.PAGO_EN_PROCESO.puedeTransicionarA(EstadoReserva.PAGO_FALLIDO)).isTrue();
    }

    @Test
    void confirmdaPuedeTransicionarACancelada() {
        assertThat(EstadoReserva.CONFIRMADA.puedeTransicionarA(EstadoReserva.CANCELADA)).isTrue();
    }

    @Test
    void puedeTransicionarADevuelveFalseCuandoNuevoEsNull() {
        assertThat(EstadoReserva.PENDIENTE_PAGO.puedeTransicionarA(null)).isFalse();
    }

    // ==================== esTerminal ====================

    @Test
    void pagoFallidoEsTerminal() {
        assertThat(EstadoReserva.PAGO_FALLIDO.esTerminal()).isTrue();
    }

    @Test
    void expiradaEsTerminal() {
        assertThat(EstadoReserva.EXPIRADA.esTerminal()).isTrue();
    }

    @Test
    void canceladaEsTerminal() {
        assertThat(EstadoReserva.CANCELADA.esTerminal()).isTrue();
    }

    @Test
    void pendientePagoNoEsTerminal() {
        assertThat(EstadoReserva.PENDIENTE_PAGO.esTerminal()).isFalse();
    }

    @Test
    void pagoEnProcesoNoEsTerminal() {
        assertThat(EstadoReserva.PAGO_EN_PROCESO.esTerminal()).isFalse();
    }

    @Test
    void confirmadaNoEsTerminal() {
        assertThat(EstadoReserva.CONFIRMADA.esTerminal()).isFalse();
    }

    // ==================== liberaSlots ====================

    @Test
    void canceladaLiberaSlots() {
        assertThat(EstadoReserva.CANCELADA.liberaSlots()).isTrue();
    }

    @Test
    void expiradaLiberaSlots() {
        assertThat(EstadoReserva.EXPIRADA.liberaSlots()).isTrue();
    }

    @Test
    void pagoFallidoLiberaSlots() {
        assertThat(EstadoReserva.PAGO_FALLIDO.liberaSlots()).isTrue();
    }

    @Test
    void pendientePagoNoLiberaSlots() {
        assertThat(EstadoReserva.PENDIENTE_PAGO.liberaSlots()).isFalse();
    }

    @Test
    void confirmadaNoLiberaSlots() {
        assertThat(EstadoReserva.CONFIRMADA.liberaSlots()).isFalse();
    }

    // ==================== fromString ====================

    @Test
    void fromStringPendientePago() {
        assertThat(EstadoReserva.fromString("PENDIENTE_PAGO")).isEqualTo(EstadoReserva.PENDIENTE_PAGO);
    }

    @Test
    void fromStringCaseInsensitive() {
        assertThat(EstadoReserva.fromString("confirmada")).isEqualTo(EstadoReserva.CONFIRMADA);
    }

    @Test
    void fromStringConEspacios() {
        assertThat(EstadoReserva.fromString("  CANCELADA  ")).isEqualTo(EstadoReserva.CANCELADA);
    }

    @Test
    void fromStringNullDevuelveNull() {
        assertThat(EstadoReserva.fromString(null)).isNull();
    }

    @Test
    void fromStringBlankDevuelveNull() {
        assertThat(EstadoReserva.fromString("   ")).isNull();
    }

    @Test
    void fromStringInvalidoLanzaIllegalArgumentException() {
        assertThatThrownBy(() -> EstadoReserva.fromString("INVALIDO"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Estado de reserva invalido");
    }
}
