package com.hotel.reserva.infrastructure.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento que ms-pago publica en topic 'pago.events'.
 *
 * Wire format definido por el publisher (ms-pago/PagoEvent) — DEBE matchear 1:1
 * con esa clase para evitar perdida de datos en deserializacion.
 *
 *  - eventType: "PagoCreado" | "PagoAprobado" | "PagoRechazado"
 *  - PascalCase, camelCase fields.
 *  - monto: BigDecimal (NO Double — perderia precision en montos grandes).
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
    private BigDecimal monto;
    private String errorMessage;
    private LocalDateTime timestamp;

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

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
