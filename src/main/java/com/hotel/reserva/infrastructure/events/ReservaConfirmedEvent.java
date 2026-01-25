package com.hotel.reserva.infrastructure.events;

import java.io.Serializable;

public class ReservaConfirmedEvent implements Serializable {

    private Long reservaId;
    private String clienteNombre;
    private String clienteEmail;
    private String hotelNombre;
    private String codigoConfirmacion;

    public ReservaConfirmedEvent() {
    }

    public ReservaConfirmedEvent(Long reservaId, String clienteNombre, String clienteEmail,
                                  String hotelNombre, String codigoConfirmacion) {
        this.reservaId = reservaId;
        this.clienteNombre = clienteNombre;
        this.clienteEmail = clienteEmail;
        this.hotelNombre = hotelNombre;
        this.codigoConfirmacion = codigoConfirmacion;
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

    public String getCodigoConfirmacion() {
        return codigoConfirmacion;
    }

    public void setCodigoConfirmacion(String codigoConfirmacion) {
        this.codigoConfirmacion = codigoConfirmacion;
    }
}
