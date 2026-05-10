package com.hotel.reserva.helpers.mappers;

import com.hotel.reserva.api.dto.MisReservasResponse;
import com.hotel.reserva.api.dto.ReservaCreatedResponse;
import com.hotel.reserva.api.dto.ReservaListResponse;
import com.hotel.reserva.api.dto.ReservaResponse;
import com.hotel.reserva.core.cliente.model.Cliente;
import com.hotel.reserva.core.detalle_reserva.model.DetalleReserva;
import com.hotel.reserva.core.reserva.model.EstadoReserva;
import com.hotel.reserva.core.reserva.model.Reserva;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservaMapperTest {

    private Reserva reserva;
    private Cliente cliente;
    private DetalleReserva detalle;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Juan");
        cliente.setApellido("Perez");
        cliente.setEmail("juan@test.com");
        cliente.setDni("12345678");
        cliente.setTelefono("1234567890");

        detalle = new DetalleReserva();
        detalle.setId(1L);
        detalle.setHabitacionId(10L);
        detalle.setPrecioNoche(100.0);

        reserva = new Reserva();
        reserva.setId(1L);
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva.setFechaReserva(LocalDate.of(2026, 1, 1));
        reserva.setFechaInicio(LocalDate.of(2026, 6, 1));
        reserva.setFechaFin(LocalDate.of(2026, 6, 5));
        reserva.setTotal(400.0);
        reserva.setHotelId(5L);
        reserva.setHotelNombre("Hotel Luxe");
        reserva.setHotelDireccion("Calle 123");
        reserva.setDepartamentoId(2L);
        reserva.setDepartamentoNombre("Montevideo");
        reserva.setCliente(cliente);
        reserva.getDetalles().add(detalle);
    }

    // ==================== toResponse ====================

    @Test
    void toResponseMapaCorrectamente() {
        ReservaResponse result = ReservaMapper.toResponse(reserva);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEstado()).isEqualTo("CONFIRMADA");
        assertThat(result.getTotal()).isEqualTo(400.0);
        assertThat(result.getHotel()).isNotNull();
        assertThat(result.getHotel().getNombre()).isEqualTo("Hotel Luxe");
        assertThat(result.getHotel().getDepartamento()).isNotNull();
        assertThat(result.getHotel().getDepartamento().getNombre()).isEqualTo("Montevideo");
        assertThat(result.getCliente()).isNotNull();
        assertThat(result.getDetalles()).hasSize(1);
    }

    @Test
    void toResponseConEstadoNuloDevuelveNullEstado() {
        reserva.setEstado(null);

        ReservaResponse result = ReservaMapper.toResponse(reserva);

        assertThat(result.getEstado()).isNull();
    }

    @Test
    void toResponseConClienteNuloDevuelveNullCliente() {
        reserva.setCliente(null);

        ReservaResponse result = ReservaMapper.toResponse(reserva);

        assertThat(result.getCliente()).isNull();
    }

    @Test
    void toResponseSinDepartamentoNoSetDepartamento() {
        reserva.setDepartamentoId(null);
        reserva.setDepartamentoNombre(null);

        ReservaResponse result = ReservaMapper.toResponse(reserva);

        assertThat(result.getHotel().getDepartamento()).isNull();
    }

    // ==================== toListResponse ====================

    @Test
    void toListResponseMapaCorrectamente() {
        ReservaListResponse result = ReservaMapper.toListResponse(reserva);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEstado()).isEqualTo("CONFIRMADA");
        assertThat(result.getDetalles()).hasSize(1);
    }

    @Test
    void toListResponseListMapaLista() {
        List<ReservaListResponse> result = ReservaMapper.toListResponseList(List.of(reserva));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    // ==================== toCreatedResponse ====================

    @Test
    void toCreatedResponseMapaCorrectamente() {
        ReservaCreatedResponse result = ReservaMapper.toCreatedResponse(reserva);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEstado()).isEqualTo("CONFIRMADA");
        assertThat(result.getMensaje()).isEqualTo("Reserva creada exitosamente");
    }

    @Test
    void toCreatedResponseConEstadoNulo() {
        reserva.setEstado(null);

        ReservaCreatedResponse result = ReservaMapper.toCreatedResponse(reserva);

        assertThat(result.getEstado()).isNull();
    }

    // ==================== toMisReservasResponse ====================

    @Test
    void toMisReservasResponseMapaCorrectamente() {
        MisReservasResponse result = ReservaMapper.toMisReservasResponse(reserva);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getHotelNombre()).isEqualTo("Hotel Luxe");
        assertThat(result.getCantidadHabitaciones()).isEqualTo(1);
    }

    @Test
    void toMisReservasResponseConDetallesNulos() {
        reserva.setDetalles(null);

        MisReservasResponse result = ReservaMapper.toMisReservasResponse(reserva);

        assertThat(result.getCantidadHabitaciones()).isEqualTo(0);
    }

    // ==================== constructor ====================

    @Test
    void constructorLanzaUnsupportedOperationException() {
        assertThatThrownBy(() -> {
            java.lang.reflect.Constructor<ReservaMapper> ctor =
                    ReservaMapper.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            ctor.newInstance();
        }).hasCauseInstanceOf(UnsupportedOperationException.class);
    }
}
