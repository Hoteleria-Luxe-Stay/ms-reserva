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
 * ClienteResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-01-23T04:24:24.974941400-05:00[America/Lima]", comments = "Generator version: 7.6.0")
public class ClienteResponse {

  private Long id;

  private String dni;

  private String nombre;

  private String apellido;

  private String email;

  private String telefono;

  public ClienteResponse id(Long id) {
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

  public ClienteResponse dni(String dni) {
    this.dni = dni;
    return this;
  }

  /**
   * Get dni
   * @return dni
  */
  
  @Schema(name = "dni", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("dni")
  public String getDni() {
    return dni;
  }

  public void setDni(String dni) {
    this.dni = dni;
  }

  public ClienteResponse nombre(String nombre) {
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

  public ClienteResponse apellido(String apellido) {
    this.apellido = apellido;
    return this;
  }

  /**
   * Get apellido
   * @return apellido
  */
  
  @Schema(name = "apellido", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("apellido")
  public String getApellido() {
    return apellido;
  }

  public void setApellido(String apellido) {
    this.apellido = apellido;
  }

  public ClienteResponse email(String email) {
    this.email = email;
    return this;
  }

  /**
   * Get email
   * @return email
  */
  
  @Schema(name = "email", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("email")
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public ClienteResponse telefono(String telefono) {
    this.telefono = telefono;
    return this;
  }

  /**
   * Get telefono
   * @return telefono
  */
  
  @Schema(name = "telefono", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("telefono")
  public String getTelefono() {
    return telefono;
  }

  public void setTelefono(String telefono) {
    this.telefono = telefono;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ClienteResponse clienteResponse = (ClienteResponse) o;
    return Objects.equals(this.id, clienteResponse.id) &&
        Objects.equals(this.dni, clienteResponse.dni) &&
        Objects.equals(this.nombre, clienteResponse.nombre) &&
        Objects.equals(this.apellido, clienteResponse.apellido) &&
        Objects.equals(this.email, clienteResponse.email) &&
        Objects.equals(this.telefono, clienteResponse.telefono);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, dni, nombre, apellido, email, telefono);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ClienteResponse {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    dni: ").append(toIndentedString(dni)).append("\n");
    sb.append("    nombre: ").append(toIndentedString(nombre)).append("\n");
    sb.append("    apellido: ").append(toIndentedString(apellido)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    telefono: ").append(toIndentedString(telefono)).append("\n");
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

