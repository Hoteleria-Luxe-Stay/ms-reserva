package com.hotel.reserva.api.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
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
 * ReservaAdminUpdateRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-01-23T04:24:24.974941400-05:00[America/Lima]", comments = "Generator version: 7.6.0")
public class ReservaAdminUpdateRequest {

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate fechaInicio;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate fechaFin;

  /**
   * Gets or Sets estado
   */
  public enum EstadoEnum {
    PENDIENTE("PENDIENTE"),
    
    CONFIRMADA("CONFIRMADA"),
    
    CANCELADA("CANCELADA");

    private String value;

    EstadoEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static EstadoEnum fromValue(String value) {
      for (EstadoEnum b : EstadoEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private EstadoEnum estado;

  private Long hotelId;

  private ClienteRequest cliente;

  @Valid
  private List<Long> habitaciones = new ArrayList<>();

  public ReservaAdminUpdateRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ReservaAdminUpdateRequest(LocalDate fechaInicio, LocalDate fechaFin, EstadoEnum estado, ClienteRequest cliente, List<Long> habitaciones) {
    this.fechaInicio = fechaInicio;
    this.fechaFin = fechaFin;
    this.estado = estado;
    this.cliente = cliente;
    this.habitaciones = habitaciones;
  }

  public ReservaAdminUpdateRequest fechaInicio(LocalDate fechaInicio) {
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

  public ReservaAdminUpdateRequest fechaFin(LocalDate fechaFin) {
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

  public ReservaAdminUpdateRequest estado(EstadoEnum estado) {
    this.estado = estado;
    return this;
  }

  /**
   * Get estado
   * @return estado
  */
  @NotNull 
  @Schema(name = "estado", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("estado")
  public EstadoEnum getEstado() {
    return estado;
  }

  public void setEstado(EstadoEnum estado) {
    this.estado = estado;
  }

  public ReservaAdminUpdateRequest hotelId(Long hotelId) {
    this.hotelId = hotelId;
    return this;
  }

  /**
   * Get hotelId
   * @return hotelId
  */
  
  @Schema(name = "hotelId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("hotelId")
  public Long getHotelId() {
    return hotelId;
  }

  public void setHotelId(Long hotelId) {
    this.hotelId = hotelId;
  }

  public ReservaAdminUpdateRequest cliente(ClienteRequest cliente) {
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

  public ReservaAdminUpdateRequest habitaciones(List<Long> habitaciones) {
    this.habitaciones = habitaciones;
    return this;
  }

  public ReservaAdminUpdateRequest addHabitacionesItem(Long habitacionesItem) {
    if (this.habitaciones == null) {
      this.habitaciones = new ArrayList<>();
    }
    this.habitaciones.add(habitacionesItem);
    return this;
  }

  /**
   * Get habitaciones
   * @return habitaciones
  */
  @NotNull 
  @Schema(name = "habitaciones", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("habitaciones")
  public List<Long> getHabitaciones() {
    return habitaciones;
  }

  public void setHabitaciones(List<Long> habitaciones) {
    this.habitaciones = habitaciones;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReservaAdminUpdateRequest reservaAdminUpdateRequest = (ReservaAdminUpdateRequest) o;
    return Objects.equals(this.fechaInicio, reservaAdminUpdateRequest.fechaInicio) &&
        Objects.equals(this.fechaFin, reservaAdminUpdateRequest.fechaFin) &&
        Objects.equals(this.estado, reservaAdminUpdateRequest.estado) &&
        Objects.equals(this.hotelId, reservaAdminUpdateRequest.hotelId) &&
        Objects.equals(this.cliente, reservaAdminUpdateRequest.cliente) &&
        Objects.equals(this.habitaciones, reservaAdminUpdateRequest.habitaciones);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fechaInicio, fechaFin, estado, hotelId, cliente, habitaciones);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ReservaAdminUpdateRequest {\n");
    sb.append("    fechaInicio: ").append(toIndentedString(fechaInicio)).append("\n");
    sb.append("    fechaFin: ").append(toIndentedString(fechaFin)).append("\n");
    sb.append("    estado: ").append(toIndentedString(estado)).append("\n");
    sb.append("    hotelId: ").append(toIndentedString(hotelId)).append("\n");
    sb.append("    cliente: ").append(toIndentedString(cliente)).append("\n");
    sb.append("    habitaciones: ").append(toIndentedString(habitaciones)).append("\n");
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

