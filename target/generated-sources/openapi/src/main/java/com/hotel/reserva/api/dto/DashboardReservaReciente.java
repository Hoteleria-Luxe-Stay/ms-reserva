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
 * DashboardReservaReciente
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-01-23T04:24:24.974941400-05:00[America/Lima]", comments = "Generator version: 7.6.0")
public class DashboardReservaReciente {

  private Long id;

  private String cliente;

  private String hotel;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate fechaInicio;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate fechaFin;

  private Double total;

  private String estado;

  public DashboardReservaReciente id(Long id) {
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

  public DashboardReservaReciente cliente(String cliente) {
    this.cliente = cliente;
    return this;
  }

  /**
   * Get cliente
   * @return cliente
  */
  
  @Schema(name = "cliente", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("cliente")
  public String getCliente() {
    return cliente;
  }

  public void setCliente(String cliente) {
    this.cliente = cliente;
  }

  public DashboardReservaReciente hotel(String hotel) {
    this.hotel = hotel;
    return this;
  }

  /**
   * Get hotel
   * @return hotel
  */
  
  @Schema(name = "hotel", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("hotel")
  public String getHotel() {
    return hotel;
  }

  public void setHotel(String hotel) {
    this.hotel = hotel;
  }

  public DashboardReservaReciente fechaInicio(LocalDate fechaInicio) {
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

  public DashboardReservaReciente fechaFin(LocalDate fechaFin) {
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

  public DashboardReservaReciente total(Double total) {
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

  public DashboardReservaReciente estado(String estado) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DashboardReservaReciente dashboardReservaReciente = (DashboardReservaReciente) o;
    return Objects.equals(this.id, dashboardReservaReciente.id) &&
        Objects.equals(this.cliente, dashboardReservaReciente.cliente) &&
        Objects.equals(this.hotel, dashboardReservaReciente.hotel) &&
        Objects.equals(this.fechaInicio, dashboardReservaReciente.fechaInicio) &&
        Objects.equals(this.fechaFin, dashboardReservaReciente.fechaFin) &&
        Objects.equals(this.total, dashboardReservaReciente.total) &&
        Objects.equals(this.estado, dashboardReservaReciente.estado);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, cliente, hotel, fechaInicio, fechaFin, total, estado);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DashboardReservaReciente {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    cliente: ").append(toIndentedString(cliente)).append("\n");
    sb.append("    hotel: ").append(toIndentedString(hotel)).append("\n");
    sb.append("    fechaInicio: ").append(toIndentedString(fechaInicio)).append("\n");
    sb.append("    fechaFin: ").append(toIndentedString(fechaFin)).append("\n");
    sb.append("    total: ").append(toIndentedString(total)).append("\n");
    sb.append("    estado: ").append(toIndentedString(estado)).append("\n");
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

