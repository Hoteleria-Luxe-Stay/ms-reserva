package com.hotel.reserva.infrastructure.events;

import java.util.List;

public class ReservaNotificationEvent {

    private String eventType;
    private Long reservaId;
    private String clienteNombre;
    private String clienteEmail;
    private String hotelNombre;
    private String hotelDireccion;
    private String fechaInicio;
    private String fechaFin;
    private String fechaCancelacion;
    private Double total;
    private String estado;
    private String motivoCancelacion;
    private List<HabitacionDetalle> habitaciones;

    public ReservaNotificationEvent() {
    }

    public ReservaNotificationEvent(String eventType,
                                    Long reservaId,
                                    String clienteNombre,
                                    String clienteEmail,
                                    String hotelNombre,
                                    String hotelDireccion,
                                    String fechaInicio,
                                    String fechaFin,
                                    String fechaCancelacion,
                                    Double total,
                                    String estado,
                                    String motivoCancelacion,
                                    List<HabitacionDetalle> habitaciones) {
        this.eventType = eventType;
        this.reservaId = reservaId;
        this.clienteNombre = clienteNombre;
        this.clienteEmail = clienteEmail;
        this.hotelNombre = hotelNombre;
        this.hotelDireccion = hotelDireccion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.fechaCancelacion = fechaCancelacion;
        this.total = total;
        this.estado = estado;
        this.motivoCancelacion = motivoCancelacion;
        this.habitaciones = habitaciones;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
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

    public String getHotelDireccion() {
        return hotelDireccion;
    }

    public void setHotelDireccion(String hotelDireccion) {
        this.hotelDireccion = hotelDireccion;
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

    public String getFechaCancelacion() {
        return fechaCancelacion;
    }

    public void setFechaCancelacion(String fechaCancelacion) {
        this.fechaCancelacion = fechaCancelacion;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }

    public List<HabitacionDetalle> getHabitaciones() {
        return habitaciones;
    }

    public void setHabitaciones(List<HabitacionDetalle> habitaciones) {
        this.habitaciones = habitaciones;
    }

    public static class HabitacionDetalle {
        private Long habitacionId;
        private Double precioNoche;

        public HabitacionDetalle() {
        }

        public HabitacionDetalle(Long habitacionId, Double precioNoche) {
            this.habitacionId = habitacionId;
            this.precioNoche = precioNoche;
        }

        public Long getHabitacionId() {
            return habitacionId;
        }

        public void setHabitacionId(Long habitacionId) {
            this.habitacionId = habitacionId;
        }

        public Double getPrecioNoche() {
            return precioNoche;
        }

        public void setPrecioNoche(Double precioNoche) {
            this.precioNoche = precioNoche;
        }
    }
}
