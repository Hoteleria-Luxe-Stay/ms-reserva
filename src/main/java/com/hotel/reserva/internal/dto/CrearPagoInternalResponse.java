package com.hotel.reserva.internal.dto;

public class CrearPagoInternalResponse {

    private Long pagoId;
    private String checkoutUrl;
    private String gatewayPaymentId;

    public CrearPagoInternalResponse() {
    }

    public Long getPagoId() { return pagoId; }
    public void setPagoId(Long pagoId) { this.pagoId = pagoId; }

    public String getCheckoutUrl() { return checkoutUrl; }
    public void setCheckoutUrl(String checkoutUrl) { this.checkoutUrl = checkoutUrl; }

    public String getGatewayPaymentId() { return gatewayPaymentId; }
    public void setGatewayPaymentId(String gatewayPaymentId) { this.gatewayPaymentId = gatewayPaymentId; }
}
