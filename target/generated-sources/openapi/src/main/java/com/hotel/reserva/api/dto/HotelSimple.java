package com.hotel.reserva.api.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.hotel.reserva.api.dto.DepartamentoSimple;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * HotelSimple
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-01-23T04:24:24.974941400-05:00[America/Lima]", comments = "Generator version: 7.6.0")
public class HotelSimple {

  private Long id;

  private String nombre;

  private String direccion;

  private DepartamentoSimple departamento;

  public HotelSimple id(Long id) {
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

  public HotelSimple nombre(String nombre) {
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

  public HotelSimple direccion(String direccion) {
    this.direccion = direccion;
    return this;
  }

  /**
   * Get direccion
   * @return direccion
  */
  
  @Schema(name = "direccion", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("direccion")
  public String getDireccion() {
    return direccion;
  }

  public void setDireccion(String direccion) {
    this.direccion = direccion;
  }

  public HotelSimple departamento(DepartamentoSimple departamento) {
    this.departamento = departamento;
    return this;
  }

  /**
   * Get departamento
   * @return departamento
  */
  @Valid 
  @Schema(name = "departamento", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("departamento")
  public DepartamentoSimple getDepartamento() {
    return departamento;
  }

  public void setDepartamento(DepartamentoSimple departamento) {
    this.departamento = departamento;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    HotelSimple hotelSimple = (HotelSimple) o;
    return Objects.equals(this.id, hotelSimple.id) &&
        Objects.equals(this.nombre, hotelSimple.nombre) &&
        Objects.equals(this.direccion, hotelSimple.direccion) &&
        Objects.equals(this.departamento, hotelSimple.departamento);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, nombre, direccion, departamento);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class HotelSimple {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    nombre: ").append(toIndentedString(nombre)).append("\n");
    sb.append("    direccion: ").append(toIndentedString(direccion)).append("\n");
    sb.append("    departamento: ").append(toIndentedString(departamento)).append("\n");
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

