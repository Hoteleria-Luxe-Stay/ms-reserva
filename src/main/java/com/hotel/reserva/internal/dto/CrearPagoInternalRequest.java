package com.hotel.reserva.internal.dto;

import java.math.BigDecimal;

/**
 * Payload para invocar a ms-pago desde ms-reserva al iniciar el flujo SAGA.
 */
public class CrearPagoInternalRequest {

    private Long reservaId;
    private BigDecimal monto;
    private String moneda;
    private String descripcion;
    private String successUrl;
    private String cancelUrl;

    public CrearPagoInternalRequest() {
    }

    public CrearPagoInternalRequest(Long reservaId, BigDecimal monto, String moneda,
                                    String descripcion, String successUrl, String cancelUrl) {
        this.reservaId = reservaId;
        this.monto = monto;
        this.moneda = moneda;
        this.descripcion = descripcion;
        this.successUrl = successUrl;
        this.cancelUrl = cancelUrl;
    }

    public Long getReservaId() { return reservaId; }
    public void setReservaId(Long reservaId) { this.reservaId = reservaId; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getSuccessUrl() { return successUrl; }
    public void setSuccessUrl(String successUrl) { this.successUrl = successUrl; }

    public String getCancelUrl() { return cancelUrl; }
    public void setCancelUrl(String cancelUrl) { this.cancelUrl = cancelUrl; }
}
