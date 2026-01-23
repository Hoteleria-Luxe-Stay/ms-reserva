package com.hotel.reserva.api.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * DashboardTopHotel
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-01-23T04:24:24.974941400-05:00[America/Lima]", comments = "Generator version: 7.6.0")
public class DashboardTopHotel {

  private String nombre;

  private Long reservas;

  public DashboardTopHotel nombre(String nombre) {
    this.nombre = nombre;
    return this;
  }

  /**
   * Get nombre
   * @return nombre
  */
  
  @Schema(name = "nombre", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("nombre")
  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public DashboardTopHotel reservas(Long reservas) {
    this.reservas = reservas;
    return this;
  }

  /**
   * Get reservas
   * @return reservas
  */
  
  @Schema(name = "reservas", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("reservas")
  public Long getReservas() {
    return reservas;
  }

  public void setReservas(Long reservas) {
    this.reservas = reservas;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DashboardTopHotel dashboardTopHotel = (DashboardTopHotel) o;
    return Objects.equals(this.nombre, dashboardTopHotel.nombre) &&
        Objects.equals(this.reservas, dashboardTopHotel.reservas);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nombre, reservas);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DashboardTopHotel {\n");
    sb.append("    nombre: ").append(toIndentedString(nombre)).append("\n");
    sb.append("    reservas: ").append(toIndentedString(reservas)).append("\n");
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

