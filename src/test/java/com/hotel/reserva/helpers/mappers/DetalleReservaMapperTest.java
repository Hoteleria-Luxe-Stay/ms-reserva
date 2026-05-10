package com.hotel.reserva.helpers.mappers;

import com.hotel.reserva.api.dto.DetalleReservaResponse;
import com.hotel.reserva.api.dto.DetalleSimple;
import com.hotel.reserva.core.detalle_reserva.model.DetalleReserva;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DetalleReservaMapperTest {

    // ==================== toResponse ====================

    @Test
    void toResponseMapaCorrectamente() {
        DetalleReserva detalle = buildDetalle();

        DetalleReservaResponse result = DetalleReservaMapper.toResponse(detalle);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getHabitacionId()).isEqualTo(10L);
        assertThat(result.getPrecioNoche()).isEqualTo(150.0);
    }

    @Test
    void toResponseConDetalleNuloDevuelveNull() {
        assertThat(DetalleReservaMapper.toResponse(null)).isNull();
    }

    // ==================== toSimple ====================

    @Test
    void toSimpleMapaCorrectamente() {
        DetalleReserva detalle = buildDetalle();

        DetalleSimple result = DetalleReservaMapper.toSimple(detalle);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getHabitacionId()).isEqualTo(10L);
        assertThat(result.getPrecioNoche()).isEqualTo(150.0);
    }

    @Test
    void toSimpleConDetalleNuloDevuelveNull() {
        assertThat(DetalleReservaMapper.toSimple(null)).isNull();
    }

    // ==================== constructor ====================

    @Test
    void constructorLanzaUnsupportedOperationException() {
        assertThatThrownBy(() -> {
            java.lang.reflect.Constructor<DetalleReservaMapper> ctor =
                    DetalleReservaMapper.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            ctor.newInstance();
        }).hasCauseInstanceOf(UnsupportedOperationException.class);
    }

    // ==================== helpers ====================

    private DetalleReserva buildDetalle() {
        DetalleReserva d = new DetalleReserva();
        d.setId(1L);
        d.setHabitacionId(10L);
        d.setPrecioNoche(150.0);
        return d;
    }
}
