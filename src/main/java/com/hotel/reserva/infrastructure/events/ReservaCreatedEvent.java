package com.hotel.reserva.infrastructure.events;

import java.io.Serializable;

public class ReservaCreatedEvent implements Serializable {

    private Long reservaId;
    private String clienteNombre;
    private String clienteEmail;
    private String hotelNombre;
    private String fechaInicio;
    private String fechaFin;

    public ReservaCreatedEvent() {
    }

    public ReservaCreatedEvent(Long reservaId, String clienteNombre, String clienteEmail, 
                                String hotelNombre, String fechaInicio, String fechaFin) {
        this.reservaId = reservaId;
        this.clienteNombre = clienteNombre;
        this.clienteEmail = clienteEmail;
        this.hotelNombre = hotelNombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public Long getReservaId() {
        return reservaId;
    }

    public void setReservaId(Long reservaId) {
        this.reservaId = reservaId;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getClienteEmail() {
        return clienteEmail;
    }

    public void setClienteEmail(String clienteEmail) {
        this.clienteEmail = clienteEmail;
    }

    public String getHotelNombre() {
        return hotelNombre;
    }

    public void setHotelNombre(String hotelNombre) {
        this.hotelNombre = hotelNombre;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }
}
