package com.hotel.reserva.helpers.mappers;

import com.hotel.reserva.api.dto.ClienteResponse;
import com.hotel.reserva.api.dto.ClienteSimple;
import com.hotel.reserva.core.cliente.model.Cliente;

public class ClienteMapper {

    private ClienteMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static ClienteResponse toResponse(Cliente cliente) {
        if (cliente == null) {
            return null;
        }

        ClienteResponse response = new ClienteResponse();
        response.setId(cliente.getId());
        response.setNombre(cliente.getNombre());
        response.setApellido(cliente.getApellido());
        response.setEmail(cliente.getEmail());
        response.setTelefono(cliente.getTelefono());
        response.setDni(cliente.getDni());
        return response;
    }

    public static ClienteSimple toSimple(Cliente cliente) {
        if (cliente == null) {
            return null;
        }

        ClienteSimple simple = new ClienteSimple();
        simple.setId(cliente.getId());
        simple.setNombre(cliente.getNombre());
        simple.setApellido(cliente.getApellido());
        simple.setEmail(cliente.getEmail());
        simple.setTelefono(cliente.getTelefono());
        simple.setDni(cliente.getDni());
        return simple;
    }
}
