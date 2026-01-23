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
 * DetalleSimple
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-01-23T04:24:24.974941400-05:00[America/Lima]", comments = "Generator version: 7.6.0")
public class DetalleSimple {

  private Long id;

  private Long habitacionId;

  private Double precioNoche;

  public DetalleSimple id(Long id) {
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

  public DetalleSimple habitacionId(Long habitacionId) {
    this.habitacionId = habitacionId;
    return this;
  }

  /**
   * Get habitacionId
   * @return habitacionId
  */
  
  @Schema(name = "habitacionId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("habitacionId")
  public Long getHabitacionId() {
    return habitacionId;
  }

  public void setHabitacionId(Long habitacionId) {
    this.habitacionId = habitacionId;
  }

  public DetalleSimple precioNoche(Double precioNoche) {
    this.precioNoche = precioNoche;
    return this;
  }

  /**
   * Get precioNoche
   * @return precioNoche
  */
  
  @Schema(name = "precioNoche", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("precioNoche")
  public Double getPrecioNoche() {
    return precioNoche;
  }

  public void setPrecioNoche(Double precioNoche) {
    this.precioNoche = precioNoche;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DetalleSimple detalleSimple = (DetalleSimple) o;
    return Objects.equals(this.id, detalleSimple.id) &&
        Objects.equals(this.habitacionId, detalleSimple.habitacionId) &&
        Objects.equals(this.precioNoche, detalleSimple.precioNoche);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, habitacionId, precioNoche);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DetalleSimple {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    habitacionId: ").append(toIndentedString(habitacionId)).append("\n");
    sb.append("    precioNoche: ").append(toIndentedString(precioNoche)).append("\n");
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

