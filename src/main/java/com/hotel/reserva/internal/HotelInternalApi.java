package com.hotel.reserva.internal;

import com.hotel.reserva.internal.dto.HabitacionInternalResponse;
import com.hotel.reserva.internal.dto.HotelInternalResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Cliente interno para comunicarse con hotel-service.
 * Usar esta clase cuando necesites consultar hoteles o habitaciones.
 */
@Component
public class HotelInternalApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(HotelInternalApi.class);

    private final RestTemplate restTemplate;

    @Value("${internal.hotel-service.url}")
    private String hotelServiceUrl;

    public HotelInternalApi(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Obtiene un hotel por su ID.
     */
    public Optional<HotelInternalResponse> getHotelById(Long hotelId) {
        try {
            String url = hotelServiceUrl + "/api/v1/hoteles/" + hotelId;

            ResponseEntity<HotelInternalResponse> response = restTemplate.getForEntity(
                    url,
                    HotelInternalResponse.class
            );

            return Optional.ofNullable(response.getBody());

        } catch (HttpClientErrorException.NotFound e) {
            LOGGER.warn("Hotel not found: {}", hotelId);
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.error("Error fetching hotel from hotel-service: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Obtiene una habitacion por su ID.
     */
    public Optional<HabitacionInternalResponse> getHabitacionById(Long habitacionId) {
        try {
            String url = hotelServiceUrl + "/api/v1/habitaciones/" + habitacionId;

            ResponseEntity<HabitacionInternalResponse> response = restTemplate.getForEntity(
                    url,
                    HabitacionInternalResponse.class
            );

            return Optional.ofNullable(response.getBody());

        } catch (HttpClientErrorException.NotFound e) {
            LOGGER.warn("Habitacion not found: {}", habitacionId);
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.error("Error fetching habitacion from hotel-service: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Verifica si la habitación está físicamente disponible (estado = DISPONIBLE).
     * La verificación de solapamiento de fechas se hace localmente en ReservaService.
     */
    public boolean checkDisponibilidad(Long habitacionId) {
        try {
            String url = String.format(
                    "%s/api/v1/habitaciones/%d/disponibilidad",
                    hotelServiceUrl,
                    habitacionId
            );

            ResponseEntity<DisponibilidadResponse> response = restTemplate.getForEntity(
                    url,
                    DisponibilidadResponse.class
            );

            return response.getBody() != null && Boolean.TRUE.equals(response.getBody().getDisponible());

        } catch (Exception e) {
            LOGGER.error("Error checking disponibilidad: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene habitaciones disponibles para un hotel en un rango de fechas.
     */
    public List<HabitacionInternalResponse> getHabitacionesDisponibles(
            Long hotelId, LocalDate fechaInicio, LocalDate fechaFin) {
        try {
            String url = String.format(
                    "%s/api/v1/habitaciones?hotelId=%d&fechaInicio=%s&fechaFin=%s",
                    hotelServiceUrl,
                    hotelId,
                    fechaInicio,
                    fechaFin
            );

            ResponseEntity<HabitacionesDisponiblesWrapper> response = restTemplate.getForEntity(
                    url,
                    HabitacionesDisponiblesWrapper.class
            );

            if (response.getBody() != null) {
                return response.getBody().getHabitaciones();
            }
            return Collections.emptyList();

        } catch (Exception e) {
            LOGGER.error("Error fetching habitaciones disponibles: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Obtiene el total de departamentos desde hotel-service.
     */
    public int getTotalDepartamentos() {
        try {
            String url = hotelServiceUrl + "/api/v1/departamentos";
            ResponseEntity<Object[]> response = restTemplate.getForEntity(url, Object[].class);
            return response.getBody() != null ? response.getBody().length : 0;
        } catch (Exception e) {
            LOGGER.error("Error fetching departamentos count: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Obtiene el total de hoteles desde hotel-service.
     */
    public int getTotalHoteles() {
        try {
            String url = hotelServiceUrl + "/api/v1/hoteles";
            ResponseEntity<Object[]> response = restTemplate.getForEntity(url, Object[].class);
            return response.getBody() != null ? response.getBody().length : 0;
        } catch (Exception e) {
            LOGGER.error("Error fetching hoteles count: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Obtiene el total de habitaciones de un hotel desde hotel-service.
     */
    public int getTotalHabitacionesPorHotel(Long hotelId) {
        try {
            String url = hotelServiceUrl + "/api/v1/hoteles/" + hotelId + "/habitaciones";
            ResponseEntity<Object[]> response = restTemplate.getForEntity(url, Object[].class);
            return response.getBody() != null ? response.getBody().length : 0;
        } catch (Exception e) {
            LOGGER.error("Error fetching habitaciones count for hotel {}: {}", hotelId, e.getMessage());
            return 0;
        }
    }

    /**
     * Obtiene todos los hoteles como lista para calcular totales de habitaciones.
     */
    public List<HotelInternalResponse> getAllHoteles() {
        try {
            String url = hotelServiceUrl + "/api/v1/hoteles";
            ResponseEntity<HotelInternalResponse[]> response = restTemplate.getForEntity(
                    url, HotelInternalResponse[].class);
            if (response.getBody() != null) {
                return List.of(response.getBody());
            }
            return Collections.emptyList();
        } catch (Exception e) {
            LOGGER.error("Error fetching all hoteles: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Obtiene el total de habitaciones sumando las de todos los hoteles.
     */
    public int getTotalHabitaciones() {
        try {
            String url = hotelServiceUrl + "/api/v1/habitaciones";
            ResponseEntity<Object[]> response = restTemplate.getForEntity(url, Object[].class);
            return response.getBody() != null ? response.getBody().length : 0;
        } catch (Exception e) {
            LOGGER.error("Error fetching habitaciones count: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Obtiene todos los departamentos con la cantidad real de hoteles que tiene cada uno.
     * Incluye departamentos sin hoteles (con valor 0).
     */
    public Map<String, Long> getHotelesPorDepartamentoReal() {
        try {
            // Obtener todos los departamentos
            String depUrl = hotelServiceUrl + "/api/v1/departamentos";
            ResponseEntity<DepartamentoSimple[]> depResponse = restTemplate.getForEntity(
                    depUrl, DepartamentoSimple[].class);

            Map<String, Long> resultado = new LinkedHashMap<>();

            if (depResponse.getBody() != null) {
                for (DepartamentoSimple dep : depResponse.getBody()) {
                    resultado.put(dep.getNombre(), 0L);
                }
            }

            // Obtener todos los hoteles y agrupar por departamento
            List<HotelInternalResponse> hoteles = getAllHoteles();
            for (HotelInternalResponse hotel : hoteles) {
                if (hotel.getDepartamento() != null && hotel.getDepartamento().getNombre() != null) {
                    String depNombre = hotel.getDepartamento().getNombre();
                    resultado.merge(depNombre, 1L, Long::sum);
                }
            }

            return resultado;
        } catch (Exception e) {
            LOGGER.error("Error fetching hoteles por departamento: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    // DTOs internos para respuestas especificas
    private static class DepartamentoSimple {
        private Long id;
        private String nombre;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
    }

    private static class DisponibilidadResponse {
        private Long habitacionId;
        private Boolean disponible;

        public Long getHabitacionId() {
            return habitacionId;
        }

        public void setHabitacionId(Long habitacionId) {
            this.habitacionId = habitacionId;
        }

        public Boolean getDisponible() {
            return disponible;
        }

        public void setDisponible(Boolean disponible) {
            this.disponible = disponible;
        }
    }

    private static class HabitacionesDisponiblesWrapper {
        private List<HabitacionInternalResponse> habitaciones;

        public List<HabitacionInternalResponse> getHabitaciones() {
            return habitaciones;
        }

        public void setHabitaciones(List<HabitacionInternalResponse> habitaciones) {
            this.habitaciones = habitaciones;
        }
    }
}
