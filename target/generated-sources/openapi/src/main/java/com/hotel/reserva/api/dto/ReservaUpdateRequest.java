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
 * ReservaUpdateRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-01-23T04:24:24.974941400-05:00[America/Lima]", comments = "Generator version: 7.6.0")
public class ReservaUpdateRequest {

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate fechaInicio;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate fechaFin;

  public ReservaUpdateRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ReservaUpdateRequest(LocalDate fechaInicio, LocalDate fechaFin) {
    this.fechaInicio = fechaInicio;
    this.fechaFin = fechaFin;
  }

  public ReservaUpdateRequest fechaInicio(LocalDate fechaInicio) {
    this.fechaInicio = fechaInicio;
    return this;
  }

  /**
   * Get fechaInicio
   * @return fechaInicio
  */
  @NotNull @Valid 
  @Schema(name = "fechaInicio", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("fechaInicio")
  public LocalDate getFechaInicio() {
    return fechaInicio;
  }

  public void setFechaInicio(LocalDate fechaInicio) {
    this.fechaInicio = fechaInicio;
  }

  public ReservaUpdateRequest fechaFin(LocalDate fechaFin) {
    this.fechaFin = fechaFin;
    return this;
  }

  /**
   * Get fechaFin
   * @return fechaFin
  */
  @NotNull @Valid 
  @Schema(name = "fechaFin", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("fechaFin")
  public LocalDate getFechaFin() {
    return fechaFin;
  }

  public void setFechaFin(LocalDate fechaFin) {
    this.fechaFin = fechaFin;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReservaUpdateRequest reservaUpdateRequest = (ReservaUpdateRequest) o;
    return Objects.equals(this.fechaInicio, reservaUpdateRequest.fechaInicio) &&
        Objects.equals(this.fechaFin, reservaUpdateRequest.fechaFin);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fechaInicio, fechaFin);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ReservaUpdateRequest {\n");
    sb.append("    fechaInicio: ").append(toIndentedString(fechaInicio)).append("\n");
    sb.append("    fechaFin: ").append(toIndentedString(fechaFin)).append("\n");
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

