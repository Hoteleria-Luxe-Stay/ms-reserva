package com.hotel.reserva.infrastructure.events;

/**
 * Evento que ms-pago publica en topic 'pago.events'.
 *
 * Wire format definido por el publisher (ms-pago/PagoEvent):
 *  - eventType: "PagoCreado" | "PagoAprobado" | "PagoRechazado"
 *  - PascalCase, camelCase fields.
 *
 * Tipos relevantes para el SAGA orchestrator de reservas:
 *  - PagoAprobado  → la reserva debe pasar a CONFIRMADA
 *  - PagoRechazado → la reserva debe pasar a PAGO_FALLIDO + liberar slots
 *
 * PagoCreado lo ignoramos: la reserva ya esta en PAGO_EN_PROCESO desde antes de
 * que ms-pago lo emita.
 */
public class PagoEvent {

    private String eventType;
    private Long pagoId;
    private Long reservaId;
    private String estado;
    private String gatewayPaymentId;
    private String moneda;
    private Double monto;
    private String errorMessage;

    public PagoEvent() {
    }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public Long getPagoId() { return pagoId; }
    public void setPagoId(Long pagoId) { this.pagoId = pagoId; }

    public Long getReservaId() { return reservaId; }
    public void setReservaId(Long reservaId) { this.reservaId = reservaId; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getGatewayPaymentId() { return gatewayPaymentId; }
    public void setGatewayPaymentId(String gatewayPaymentId) { this.gatewayPaymentId = gatewayPaymentId; }

    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
