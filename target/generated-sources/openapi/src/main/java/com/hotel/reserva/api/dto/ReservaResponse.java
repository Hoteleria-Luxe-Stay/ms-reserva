package com.hotel.reserva.api.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.hotel.reserva.api.dto.ClienteResponse;
import com.hotel.reserva.api.dto.DetalleReservaResponse;
import com.hotel.reserva.api.dto.HotelSimple;
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
 * ReservaResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-01-23T04:24:24.974941400-05:00[America/Lima]", comments = "Generator version: 7.6.0")
public class ReservaResponse {

  private Long id;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate fechaReserva;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate fechaInicio;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate fechaFin;

  private Double total;

  private String estado;

  private HotelSimple hotel;

  private ClienteResponse cliente;

  @Valid
  private List<@Valid DetalleReservaResponse> detalles = new ArrayList<>();

  public ReservaResponse id(Long id) {
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

  public ReservaResponse fechaReserva(LocalDate fechaReserva) {
    this.fechaReserva = fechaReserva;
    return this;
  }

  /**
   * Get fechaReserva
   * @return fechaReserva
  */
  @Valid 
  @Schema(name = "fechaReserva", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("fechaReserva")
  public LocalDate getFechaReserva() {
    return fechaReserva;
  }

  public void setFechaReserva(LocalDate fechaReserva) {
    this.fechaReserva = fechaReserva;
  }

  public ReservaResponse fechaInicio(LocalDate fechaInicio) {
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

  public ReservaResponse fechaFin(LocalDate fechaFin) {
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

  public ReservaResponse total(Double total) {
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

  public ReservaResponse estado(String estado) {
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

  public ReservaResponse hotel(HotelSimple hotel) {
    this.hotel = hotel;
    return this;
  }

  /**
   * Get hotel
   * @return hotel
  */
  @Valid 
  @Schema(name = "hotel", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("hotel")
  public HotelSimple getHotel() {
    return hotel;
  }

  public void setHotel(HotelSimple hotel) {
    this.hotel = hotel;
  }

  public ReservaResponse cliente(ClienteResponse cliente) {
    this.cliente = cliente;
    return this;
  }

  /**
   * Get cliente
   * @return cliente
  */
  @Valid 
  @Schema(name = "cliente", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("cliente")
  public ClienteResponse getCliente() {
    return cliente;
  }

  public void setCliente(ClienteResponse cliente) {
    this.cliente = cliente;
  }

  public ReservaResponse detalles(List<@Valid DetalleReservaResponse> detalles) {
    this.detalles = detalles;
    return this;
  }

  public ReservaResponse addDetallesItem(DetalleReservaResponse detallesItem) {
    if (this.detalles == null) {
      this.detalles = new ArrayList<>();
    }
    this.detalles.add(detallesItem);
    return this;
  }

  /**
   * Get detalles
   * @return detalles
  */
  @Valid 
  @Schema(name = "detalles", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("detalles")
  public List<@Valid DetalleReservaResponse> getDetalles() {
    return detalles;
  }

  public void setDetalles(List<@Valid DetalleReservaResponse> detalles) {
    this.detalles = detalles;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReservaResponse reservaResponse = (ReservaResponse) o;
    return Objects.equals(this.id, reservaResponse.id) &&
        Objects.equals(this.fechaReserva, reservaResponse.fechaReserva) &&
        Objects.equals(this.fechaInicio, reservaResponse.fechaInicio) &&
        Objects.equals(this.fechaFin, reservaResponse.fechaFin) &&
        Objects.equals(this.total, reservaResponse.total) &&
        Objects.equals(this.estado, reservaResponse.estado) &&
        Objects.equals(this.hotel, reservaResponse.hotel) &&
        Objects.equals(this.cliente, reservaResponse.cliente) &&
        Objects.equals(this.detalles, reservaResponse.detalles);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, fechaReserva, fechaInicio, fechaFin, total, estado, hotel, cliente, detalles);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ReservaResponse {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    fechaReserva: ").append(toIndentedString(fechaReserva)).append("\n");
    sb.append("    fechaInicio: ").append(toIndentedString(fechaInicio)).append("\n");
    sb.append("    fechaFin: ").append(toIndentedString(fechaFin)).append("\n");
    sb.append("    total: ").append(toIndentedString(total)).append("\n");
    sb.append("    estado: ").append(toIndentedString(estado)).append("\n");
    sb.append("    hotel: ").append(toIndentedString(hotel)).append("\n");
    sb.append("    cliente: ").append(toIndentedString(cliente)).append("\n");
    sb.append("    detalles: ").append(toIndentedString(detalles)).append("\n");
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

