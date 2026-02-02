package com.hotel.reserva.helpers.mappers;

import com.hotel.reserva.api.dto.DepartamentoSimple;
import com.hotel.reserva.api.dto.HotelSimple;
import com.hotel.reserva.api.dto.MisReservasResponse;
import com.hotel.reserva.api.dto.ReservaCreatedResponse;
import com.hotel.reserva.api.dto.ReservaListResponse;
import com.hotel.reserva.api.dto.ReservaResponse;
import com.hotel.reserva.core.reserva.model.Reserva;

import java.util.List;

public class ReservaMapper {

    private ReservaMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static ReservaResponse toResponse(Reserva reserva) {
        ReservaResponse response = new ReservaResponse();
        response.setId(reserva.getId());
        response.setFechaReserva(reserva.getFechaReserva());
        response.setFechaInicio(reserva.getFechaInicio());
        response.setFechaFin(reserva.getFechaFin());
        response.setFechaCancelacion(reserva.getFechaCancelacion());
        response.setTotal(reserva.getTotal());
        response.setEstado(reserva.getEstado());
        response.setMotivoCancelacion(reserva.getMotivoCancelacion());
        response.setHotel(toHotelSimple(reserva));
        response.setCliente(ClienteMapper.toResponse(reserva.getCliente()));
        response.setDetalles(reserva.getDetalles().stream()
                .map(DetalleReservaMapper::toResponse)
                .toList());
        return response;
    }

    public static ReservaListResponse toListResponse(Reserva reserva) {
        ReservaListResponse response = new ReservaListResponse();
        response.setId(reserva.getId());
        response.setFechaReserva(reserva.getFechaReserva());
        response.setFechaInicio(reserva.getFechaInicio());
        response.setFechaFin(reserva.getFechaFin());
        response.setFechaCancelacion(reserva.getFechaCancelacion());
        response.setTotal(reserva.getTotal());
        response.setEstado(reserva.getEstado());
        response.setMotivoCancelacion(reserva.getMotivoCancelacion());
        response.setHotel(toHotelSimple(reserva));
        response.setCliente(ClienteMapper.toSimple(reserva.getCliente()));
        response.setDetalles(reserva.getDetalles().stream()
                .map(DetalleReservaMapper::toSimple)
                .toList());
        return response;
    }

    public static ReservaCreatedResponse toCreatedResponse(Reserva reserva) {
        ReservaCreatedResponse response = new ReservaCreatedResponse();
        response.setId(reserva.getId());
        response.setFechaReserva(reserva.getFechaReserva());
        response.setFechaInicio(reserva.getFechaInicio());
        response.setFechaFin(reserva.getFechaFin());
        response.setFechaCancelacion(reserva.getFechaCancelacion());
        response.setTotal(reserva.getTotal());
        response.setEstado(reserva.getEstado());
        response.setMotivoCancelacion(reserva.getMotivoCancelacion());
        response.setMensaje("Reserva creada exitosamente");
        return response;
    }

    public static MisReservasResponse toMisReservasResponse(Reserva reserva) {
        MisReservasResponse response = new MisReservasResponse();
        response.setId(reserva.getId());
        response.setFechaInicio(reserva.getFechaInicio());
        response.setFechaFin(reserva.getFechaFin());
        response.setFechaCancelacion(reserva.getFechaCancelacion());
        response.setTotal(reserva.getTotal());
        response.setEstado(reserva.getEstado());
        response.setMotivoCancelacion(reserva.getMotivoCancelacion());
        response.setHotelNombre(reserva.getHotelNombre());
        response.setCantidadHabitaciones(reserva.getDetalles() != null ? reserva.getDetalles().size() : 0);
        return response;
    }

    public static List<ReservaListResponse> toListResponseList(List<Reserva> reservas) {
        return reservas.stream().map(ReservaMapper::toListResponse).toList();
    }

    private static HotelSimple toHotelSimple(Reserva reserva) {
        HotelSimple hotel = new HotelSimple();
        hotel.setId(reserva.getHotelId());
        hotel.setNombre(reserva.getHotelNombre());
        hotel.setDireccion(reserva.getHotelDireccion());

        if (reserva.getDepartamentoId() != null || reserva.getDepartamentoNombre() != null) {
            DepartamentoSimple dep = new DepartamentoSimple();
            dep.setId(reserva.getDepartamentoId());
            dep.setNombre(reserva.getDepartamentoNombre());
            hotel.setDepartamento(dep);
        }

        return hotel;
    }
}
