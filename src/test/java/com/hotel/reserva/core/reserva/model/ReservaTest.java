package com.hotel.reserva.core.reserva.model;

import com.hotel.reserva.core.detalle_reserva.model.DetalleReserva;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservaTest {

    @Test
    void transicionarAExitosoActualizaEstado() {
        Reserva reserva = new Reserva();
        reserva.setEstado(EstadoReserva.PENDIENTE_PAGO);

        reserva.transicionarA(EstadoReserva.PAGO_EN_PROCESO);

        assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.PAGO_EN_PROCESO);
    }

    @Test
    void transicionarALanzaIllegalStateExceptionCuandoTransicionInvalida() {
        Reserva reserva = new Reserva();
        reserva.setEstado(EstadoReserva.CONFIRMADA);

        assertThatThrownBy(() -> reserva.transicionarA(EstadoReserva.PAGO_EN_PROCESO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Transicion invalida");
    }

    @Test
    void transicionarALanzaIllegalStateExceptionCuandoEstadoNulo() {
        Reserva reserva = new Reserva();
        // estado == null

        assertThatThrownBy(() -> reserva.transicionarA(EstadoReserva.CONFIRMADA))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Reserva sin estado");
    }

    @Test
    void addDetalleAgregaDetalleYSetReserva() {
        Reserva reserva = new Reserva();
        reserva.setId(1L);

        DetalleReserva detalle = new DetalleReserva();
        detalle.setHabitacionId(10L);

        reserva.addDetalle(detalle);

        assertThat(reserva.getDetalles()).hasSize(1);
        assertThat(reserva.getDetalles().get(0).getReserva()).isEqualTo(reserva);
    }
}
