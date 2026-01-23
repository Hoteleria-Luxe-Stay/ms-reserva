package com.hotel.reserva.api.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.hotel.reserva.api.dto.ClienteRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * ReservaRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-01-23T04:24:24.974941400-05:00[America/Lima]", comments = "Generator version: 7.6.0")
public class ReservaRequest {

  private Long hotelId;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate fechaInicio;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate fechaFin;

  @Valid
  private List<Long> habitacionesIds = new ArrayList<>();

  private ClienteRequest cliente;

  public ReservaRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ReservaRequest(Long hotelId, LocalDate fechaInicio, LocalDate fechaFin, List<Long> habitacionesIds, ClienteRequest cliente) {
    this.hotelId = hotelId;
    this.fechaInicio = fechaInicio;
    this.fechaFin = fechaFin;
    this.habitacionesIds = habitacionesIds;
    this.cliente = cliente;
  }

  public ReservaRequest hotelId(Long hotelId) {
    this.hotelId = hotelId;
    return this;
  }

  /**
   * Get hotelId
   * @return hotelId
  */
  @NotNull 
  @Schema(name = "hotelId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("hotelId")
  public Long getHotelId() {
    return hotelId;
  }

  public void setHotelId(Long hotelId) {
    this.hotelId = hotelId;
  }

  public ReservaRequest fechaInicio(LocalDate fechaInicio) {
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

  public ReservaRequest fechaFin(LocalDate fechaFin) {
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

  public ReservaRequest habitacionesIds(List<Long> habitacionesIds) {
    this.habitacionesIds = habitacionesIds;
    return this;
  }

  public ReservaRequest addHabitacionesIdsItem(Long habitacionesIdsItem) {
    if (this.habitacionesIds == null) {
      this.habitacionesIds = new ArrayList<>();
    }
    this.habitacionesIds.add(habitacionesIdsItem);
    return this;
  }

  /**
   * Get habitacionesIds
   * @return habitacionesIds
  */
  @NotNull @Size(min = 1) 
  @Schema(name = "habitacionesIds", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("habitacionesIds")
  public List<Long> getHabitacionesIds() {
    return habitacionesIds;
  }

  public void setHabitacionesIds(List<Long> habitacionesIds) {
    this.habitacionesIds = habitacionesIds;
  }

  public ReservaRequest cliente(ClienteRequest cliente) {
    this.cliente = cliente;
    return this;
  }

  /**
   * Get cliente
   * @return cliente
  */
  @NotNull @Valid 
  @Schema(name = "cliente", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("cliente")
  public ClienteRequest getCliente() {
    return cliente;
  }

  public void setCliente(ClienteRequest cliente) {
    this.cliente = cliente;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReservaRequest reservaRequest = (ReservaRequest) o;
    return Objects.equals(this.hotelId, reservaRequest.hotelId) &&
        Objects.equals(this.fechaInicio, reservaRequest.fechaInicio) &&
        Objects.equals(this.fechaFin, reservaRequest.fechaFin) &&
        Objects.equals(this.habitacionesIds, reservaRequest.habitacionesIds) &&
        Objects.equals(this.cliente, reservaRequest.cliente);
  }

  @Override
  public int hashCode() {
    return Objects.hash(hotelId, fechaInicio, fechaFin, habitacionesIds, cliente);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ReservaRequest {\n");
    sb.append("    hotelId: ").append(toIndentedString(hotelId)).append("\n");
    sb.append("    fechaInicio: ").append(toIndentedString(fechaInicio)).append("\n");
    sb.append("    fechaFin: ").append(toIndentedString(fechaFin)).append("\n");
    sb.append("    habitacionesIds: ").append(toIndentedString(habitacionesIds)).append("\n");
    sb.append("    cliente: ").append(toIndentedString(cliente)).append("\n");
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

