package com.hotel.reserva.api.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.hotel.reserva.api.dto.DashboardReservaReciente;
import com.hotel.reserva.api.dto.DashboardTopHotel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * DashboardStatsResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-01-23T04:24:24.974941400-05:00[America/Lima]", comments = "Generator version: 7.6.0")
public class DashboardStatsResponse {

  private Integer totalDepartamentos;

  private Integer totalHoteles;

  private Long totalHabitaciones;

  private Integer totalReservas;

  @Valid
  private Map<String, Long> reservasPorEstado = new HashMap<>();

  private Double ingresosTotales;

  @Valid
  private Map<String, Long> hotelesPorDepartamento = new HashMap<>();

  @Valid
  private Map<String, Long> reservasPorMes = new HashMap<>();

  @Valid
  private Map<String, Double> ingresosPorMes = new HashMap<>();

  @Valid
  private List<@Valid DashboardTopHotel> topHoteles = new ArrayList<>();

  @Valid
  private List<@Valid DashboardReservaReciente> reservasRecientes = new ArrayList<>();

  public DashboardStatsResponse totalDepartamentos(Integer totalDepartamentos) {
    this.totalDepartamentos = totalDepartamentos;
    return this;
  }

  /**
   * Get totalDepartamentos
   * @return totalDepartamentos
  */
  
  @Schema(name = "totalDepartamentos", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("totalDepartamentos")
  public Integer getTotalDepartamentos() {
    return totalDepartamentos;
  }

  public void setTotalDepartamentos(Integer totalDepartamentos) {
    this.totalDepartamentos = totalDepartamentos;
  }

  public DashboardStatsResponse totalHoteles(Integer totalHoteles) {
    this.totalHoteles = totalHoteles;
    return this;
  }

  /**
   * Get totalHoteles
   * @return totalHoteles
  */
  
  @Schema(name = "totalHoteles", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("totalHoteles")
  public Integer getTotalHoteles() {
    return totalHoteles;
  }

  public void setTotalHoteles(Integer totalHoteles) {
    this.totalHoteles = totalHoteles;
  }

  public DashboardStatsResponse totalHabitaciones(Long totalHabitaciones) {
    this.totalHabitaciones = totalHabitaciones;
    return this;
  }

  /**
   * Get totalHabitaciones
   * @return totalHabitaciones
  */
  
  @Schema(name = "totalHabitaciones", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("totalHabitaciones")
  public Long getTotalHabitaciones() {
    return totalHabitaciones;
  }

  public void setTotalHabitaciones(Long totalHabitaciones) {
    this.totalHabitaciones = totalHabitaciones;
  }

  public DashboardStatsResponse totalReservas(Integer totalReservas) {
    this.totalReservas = totalReservas;
    return this;
  }

  /**
   * Get totalReservas
   * @return totalReservas
  */
  
  @Schema(name = "totalReservas", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("totalReservas")
  public Integer getTotalReservas() {
    return totalReservas;
  }

  public void setTotalReservas(Integer totalReservas) {
    this.totalReservas = totalReservas;
  }

  public DashboardStatsResponse reservasPorEstado(Map<String, Long> reservasPorEstado) {
    this.reservasPorEstado = reservasPorEstado;
    return this;
  }

  public DashboardStatsResponse putReservasPorEstadoItem(String key, Long reservasPorEstadoItem) {
    if (this.reservasPorEstado == null) {
      this.reservasPorEstado = new HashMap<>();
    }
    this.reservasPorEstado.put(key, reservasPorEstadoItem);
    return this;
  }

  /**
   * Get reservasPorEstado
   * @return reservasPorEstado
  */
  
  @Schema(name = "reservasPorEstado", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("reservasPorEstado")
  public Map<String, Long> getReservasPorEstado() {
    return reservasPorEstado;
  }

  public void setReservasPorEstado(Map<String, Long> reservasPorEstado) {
    this.reservasPorEstado = reservasPorEstado;
  }

  public DashboardStatsResponse ingresosTotales(Double ingresosTotales) {
    this.ingresosTotales = ingresosTotales;
    return this;
  }

  /**
   * Get ingresosTotales
   * @return ingresosTotales
  */
  
  @Schema(name = "ingresosTotales", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("ingresosTotales")
  public Double getIngresosTotales() {
    return ingresosTotales;
  }

  public void setIngresosTotales(Double ingresosTotales) {
    this.ingresosTotales = ingresosTotales;
  }

  public DashboardStatsResponse hotelesPorDepartamento(Map<String, Long> hotelesPorDepartamento) {
    this.hotelesPorDepartamento = hotelesPorDepartamento;
    return this;
  }

  public DashboardStatsResponse putHotelesPorDepartamentoItem(String key, Long hotelesPorDepartamentoItem) {
    if (this.hotelesPorDepartamento == null) {
      this.hotelesPorDepartamento = new HashMap<>();
    }
    this.hotelesPorDepartamento.put(key, hotelesPorDepartamentoItem);
    return this;
  }

  /**
   * Get hotelesPorDepartamento
   * @return hotelesPorDepartamento
  */
  
  @Schema(name = "hotelesPorDepartamento", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("hotelesPorDepartamento")
  public Map<String, Long> getHotelesPorDepartamento() {
    return hotelesPorDepartamento;
  }

  public void setHotelesPorDepartamento(Map<String, Long> hotelesPorDepartamento) {
    this.hotelesPorDepartamento = hotelesPorDepartamento;
  }

  public DashboardStatsResponse reservasPorMes(Map<String, Long> reservasPorMes) {
    this.reservasPorMes = reservasPorMes;
    return this;
  }

  public DashboardStatsResponse putReservasPorMesItem(String key, Long reservasPorMesItem) {
    if (this.reservasPorMes == null) {
      this.reservasPorMes = new HashMap<>();
    }
    this.reservasPorMes.put(key, reservasPorMesItem);
    return this;
  }

  /**
   * Get reservasPorMes
   * @return reservasPorMes
  */
  
  @Schema(name = "reservasPorMes", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("reservasPorMes")
  public Map<String, Long> getReservasPorMes() {
    return reservasPorMes;
  }

  public void setReservasPorMes(Map<String, Long> reservasPorMes) {
    this.reservasPorMes = reservasPorMes;
  }

  public DashboardStatsResponse ingresosPorMes(Map<String, Double> ingresosPorMes) {
    this.ingresosPorMes = ingresosPorMes;
    return this;
  }

  public DashboardStatsResponse putIngresosPorMesItem(String key, Double ingresosPorMesItem) {
    if (this.ingresosPorMes == null) {
      this.ingresosPorMes = new HashMap<>();
    }
    this.ingresosPorMes.put(key, ingresosPorMesItem);
    return this;
  }

  /**
   * Get ingresosPorMes
   * @return ingresosPorMes
  */
  
  @Schema(name = "ingresosPorMes", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("ingresosPorMes")
  public Map<String, Double> getIngresosPorMes() {
    return ingresosPorMes;
  }

  public void setIngresosPorMes(Map<String, Double> ingresosPorMes) {
    this.ingresosPorMes = ingresosPorMes;
  }

  public DashboardStatsResponse topHoteles(List<@Valid DashboardTopHotel> topHoteles) {
    this.topHoteles = topHoteles;
    return this;
  }

  public DashboardStatsResponse addTopHotelesItem(DashboardTopHotel topHotelesItem) {
    if (this.topHoteles == null) {
      this.topHoteles = new ArrayList<>();
    }
    this.topHoteles.add(topHotelesItem);
    return this;
  }

  /**
   * Get topHoteles
   * @return topHoteles
  */
  @Valid 
  @Schema(name = "topHoteles", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("topHoteles")
  public List<@Valid DashboardTopHotel> getTopHoteles() {
    return topHoteles;
  }

  public void setTopHoteles(List<@Valid DashboardTopHotel> topHoteles) {
    this.topHoteles = topHoteles;
  }

  public DashboardStatsResponse reservasRecientes(List<@Valid DashboardReservaReciente> reservasRecientes) {
    this.reservasRecientes = reservasRecientes;
    return this;
  }

  public DashboardStatsResponse addReservasRecientesItem(DashboardReservaReciente reservasRecientesItem) {
    if (this.reservasRecientes == null) {
      this.reservasRecientes = new ArrayList<>();
    }
    this.reservasRecientes.add(reservasRecientesItem);
    return this;
  }

  /**
   * Get reservasRecientes
   * @return reservasRecientes
  */
  @Valid 
  @Schema(name = "reservasRecientes", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("reservasRecientes")
  public List<@Valid DashboardReservaReciente> getReservasRecientes() {
    return reservasRecientes;
  }

  public void setReservasRecientes(List<@Valid DashboardReservaReciente> reservasRecientes) {
    this.reservasRecientes = reservasRecientes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DashboardStatsResponse dashboardStatsResponse = (DashboardStatsResponse) o;
    return Objects.equals(this.totalDepartamentos, dashboardStatsResponse.totalDepartamentos) &&
        Objects.equals(this.totalHoteles, dashboardStatsResponse.totalHoteles) &&
        Objects.equals(this.totalHabitaciones, dashboardStatsResponse.totalHabitaciones) &&
        Objects.equals(this.totalReservas, dashboardStatsResponse.totalReservas) &&
        Objects.equals(this.reservasPorEstado, dashboardStatsResponse.reservasPorEstado) &&
        Objects.equals(this.ingresosTotales, dashboardStatsResponse.ingresosTotales) &&
        Objects.equals(this.hotelesPorDepartamento, dashboardStatsResponse.hotelesPorDepartamento) &&
        Objects.equals(this.reservasPorMes, dashboardStatsResponse.reservasPorMes) &&
        Objects.equals(this.ingresosPorMes, dashboardStatsResponse.ingresosPorMes) &&
        Objects.equals(this.topHoteles, dashboardStatsResponse.topHoteles) &&
        Objects.equals(this.reservasRecientes, dashboardStatsResponse.reservasRecientes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(totalDepartamentos, totalHoteles, totalHabitaciones, totalReservas, reservasPorEstado, ingresosTotales, hotelesPorDepartamento, reservasPorMes, ingresosPorMes, topHoteles, reservasRecientes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DashboardStatsResponse {\n");
    sb.append("    totalDepartamentos: ").append(toIndentedString(totalDepartamentos)).append("\n");
    sb.append("    totalHoteles: ").append(toIndentedString(totalHoteles)).append("\n");
    sb.append("    totalHabitaciones: ").append(toIndentedString(totalHabitaciones)).append("\n");
    sb.append("    totalReservas: ").append(toIndentedString(totalReservas)).append("\n");
    sb.append("    reservasPorEstado: ").append(toIndentedString(reservasPorEstado)).append("\n");
    sb.append("    ingresosTotales: ").append(toIndentedString(ingresosTotales)).append("\n");
    sb.append("    hotelesPorDepartamento: ").append(toIndentedString(hotelesPorDepartamento)).append("\n");
    sb.append("    reservasPorMes: ").append(toIndentedString(reservasPorMes)).append("\n");
    sb.append("    ingresosPorMes: ").append(toIndentedString(ingresosPorMes)).append("\n");
    sb.append("    topHoteles: ").append(toIndentedString(topHoteles)).append("\n");
    sb.append("    reservasRecientes: ").append(toIndentedString(reservasRecientes)).append("\n");
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

