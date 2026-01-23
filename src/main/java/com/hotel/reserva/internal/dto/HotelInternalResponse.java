package com.hotel.reserva.internal.dto;

public class HotelInternalResponse {
    private Long id;
    private String nombre;
    private String direccion;
    private DepartamentoInternalResponse departamento;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public DepartamentoInternalResponse getDepartamento() {
        return departamento;
    }

    public void setDepartamento(DepartamentoInternalResponse departamento) {
        this.departamento = departamento;
    }

    public static class DepartamentoInternalResponse {
        private Long id;
        private String nombre;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }
    }
}
