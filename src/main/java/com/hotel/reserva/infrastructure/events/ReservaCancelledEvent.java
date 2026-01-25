package com.hotel.reserva.infrastructure.events;

import java.io.Serializable;

public class ReservaCancelledEvent implements Serializable {

    private Long reservaId;
    private String clienteNombre;
    private String clienteEmail;
    private String hotelNombre;
    private String motivo;

    public ReservaCancelledEvent() {
    }

    public ReservaCancelledEvent(Long reservaId, String clienteNombre, String clienteEmail,
                                  String hotelNombre, String motivo) {
        this.reservaId = reservaId;
        this.clienteNombre = clienteNombre;
        this.clienteEmail = clienteEmail;
        this.hotelNombre = hotelNombre;
        this.motivo = motivo;
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

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
