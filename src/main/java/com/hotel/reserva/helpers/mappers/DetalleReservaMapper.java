package com.hotel.reserva.helpers.mappers;

import com.hotel.reserva.api.dto.DetalleReservaResponse;
import com.hotel.reserva.api.dto.DetalleSimple;
import com.hotel.reserva.core.detalle_reserva.model.DetalleReserva;

public class DetalleReservaMapper {

    private DetalleReservaMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static DetalleReservaResponse toResponse(DetalleReserva detalle) {
        if (detalle == null) {
            return null;
        }

        DetalleReservaResponse response = new DetalleReservaResponse();
        response.setId(detalle.getId());
        response.setHabitacionId(detalle.getHabitacionId());
        response.setPrecioNoche(detalle.getPrecioNoche());
        return response;
    }

    public static DetalleSimple toSimple(DetalleReserva detalle) {
        if (detalle == null) {
            return null;
        }

        DetalleSimple simple = new DetalleSimple();
        simple.setId(detalle.getId());
        simple.setHabitacionId(detalle.getHabitacionId());
        simple.setPrecioNoche(detalle.getPrecioNoche());
        return simple;
    }
}
