package com.hotel.reserva.api.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * MisReservasResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-01-23T04:24:24.974941400-05:00[America/Lima]", comments = "Generator version: 7.6.0")
public class MisReservasResponse {

  private Long id;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate fechaInicio;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate fechaFin;

  private Double total;

  private String estado;

  private String hotelNombre;

  private Integer cantidadHabitaciones;

  public MisReservasResponse id(Long id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   * @return id
  */
  
  @Schema(name = "id", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("id")
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public MisReservasResponse fechaInicio(LocalDate fechaInicio) {
    this.fechaInicio = fechaInicio;
    return this;
  }

  /**
   * Get fechaInicio
   * @return fechaInicio
  */
  @Valid 
  @Schema(name = "fechaInicio", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("fechaInicio")
  public LocalDate getFechaInicio() {
    return fechaInicio;
  }

  public void setFechaInicio(LocalDate fechaInicio) {
    this.fechaInicio = fechaInicio;
  }

  public MisReservasResponse fechaFin(LocalDate fechaFin) {
    this.fechaFin = fechaFin;
    return this;
  }

  /**
   * Get fechaFin
   * @return fechaFin
  */
  @Valid 
  @Schema(name = "fechaFin", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("fechaFin")
  public LocalDate getFechaFin() {
    return fechaFin;
  }

  public void setFechaFin(LocalDate fechaFin) {
    this.fechaFin = fechaFin;
  }

  public MisReservasResponse total(Double total) {
    this.total = total;
    return this;
  }

  /**
   * Get total
   * @return total
  */
  
  @Schema(name = "total", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("total")
  public Double getTotal() {
    return total;
  }

  public void setTotal(Double total) {
    this.total = total;
  }

  public MisReservasResponse estado(String estado) {
    this.estado = estado;
    return this;
  }

  /**
   * Get estado
   * @return estado
  */
  
  @Schema(name = "estado", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("estado")
  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

  public MisReservasResponse hotelNombre(String hotelNombre) {
    this.hotelNombre = hotelNombre;
    return this;
  }

  /**
   * Get hotelNombre
   * @return hotelNombre
  */
  
  @Schema(name = "hotelNombre", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("hotelNombre")
  public String getHotelNombre() {
    return hotelNombre;
  }

  public void setHotelNombre(String hotelNombre) {
    this.hotelNombre = hotelNombre;
  }

  public MisReservasResponse cantidadHabitaciones(Integer cantidadHabitaciones) {
    this.cantidadHabitaciones = cantidadHabitaciones;
    return this;
  }

  /**
   * Get cantidadHabitaciones
   * @return cantidadHabitaciones
  */
  
  @Schema(name = "cantidadHabitaciones", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("cantidadHabitaciones")
  public Integer getCantidadHabitaciones() {
    return cantidadHabitaciones;
  }

  public void setCantidadHabitaciones(Integer cantidadHabitaciones) {
    this.cantidadHabitaciones = cantidadHabitaciones;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MisReservasResponse misReservasResponse = (MisReservasResponse) o;
    return Objects.equals(this.id, misReservasResponse.id) &&
        Objects.equals(this.fechaInicio, misReservasResponse.fechaInicio) &&
        Objects.equals(this.fechaFin, misReservasResponse.fechaFin) &&
        Objects.equals(this.total, misReservasResponse.total) &&
        Objects.equals(this.estado, misReservasResponse.estado) &&
        Objects.equals(this.hotelNombre, misReservasResponse.hotelNombre) &&
        Objects.equals(this.cantidadHabitaciones, misReservasResponse.cantidadHabitaciones);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, fechaInicio, fechaFin, total, estado, hotelNombre, cantidadHabitaciones);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MisReservasResponse {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    fechaInicio: ").append(toIndentedString(fechaInicio)).append("\n");
    sb.append("    fechaFin: ").append(toIndentedString(fechaFin)).append("\n");
    sb.append("    total: ").append(toIndentedString(total)).append("\n");
    sb.append("    estado: ").append(toIndentedString(estado)).append("\n");
    sb.append("    hotelNombre: ").append(toIndentedString(hotelNombre)).append("\n");
    sb.append("    cantidadHabitaciones: ").append(toIndentedString(cantidadHabitaciones)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

