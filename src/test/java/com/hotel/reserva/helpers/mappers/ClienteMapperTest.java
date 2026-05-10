package com.hotel.reserva.helpers.mappers;

import com.hotel.reserva.api.dto.ClienteResponse;
import com.hotel.reserva.api.dto.ClienteSimple;
import com.hotel.reserva.core.cliente.model.Cliente;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClienteMapperTest {

    // ==================== toResponse ====================

    @Test
    void toResponseMapaCorrectamente() {
        Cliente cliente = buildCliente();

        ClienteResponse result = ClienteMapper.toResponse(cliente);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Juan");
        assertThat(result.getApellido()).isEqualTo("Perez");
        assertThat(result.getEmail()).isEqualTo("juan@test.com");
        assertThat(result.getDni()).isEqualTo("12345678");
        assertThat(result.getTelefono()).isEqualTo("1234567890");
    }

    @Test
    void toResponseConClienteNuloDevuelveNull() {
        assertThat(ClienteMapper.toResponse(null)).isNull();
    }

    // ==================== toSimple ====================

    @Test
    void toSimpleMapaCorrectamente() {
        Cliente cliente = buildCliente();

        ClienteSimple result = ClienteMapper.toSimple(cliente);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Juan");
    }

    @Test
    void toSimpleConClienteNuloDevuelveNull() {
        assertThat(ClienteMapper.toSimple(null)).isNull();
    }

    // ==================== constructor ====================

    @Test
    void constructorLanzaUnsupportedOperationException() {
        assertThatThrownBy(() -> {
            java.lang.reflect.Constructor<ClienteMapper> ctor =
                    ClienteMapper.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            ctor.newInstance();
        }).hasCauseInstanceOf(UnsupportedOperationException.class);
    }

    // ==================== helpers ====================

    private Cliente buildCliente() {
        Cliente c = new Cliente();
        c.setId(1L);
        c.setNombre("Juan");
        c.setApellido("Perez");
        c.setEmail("juan@test.com");
        c.setDni("12345678");
        c.setTelefono("1234567890");
        return c;
    }
}
