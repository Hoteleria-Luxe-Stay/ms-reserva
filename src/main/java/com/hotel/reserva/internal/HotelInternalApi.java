package com.hotel.reserva.internal;

import com.hotel.reserva.helpers.exceptions.ServiceUnavailableException;
import com.hotel.reserva.internal.dto.HabitacionInternalResponse;
import com.hotel.reserva.internal.dto.HotelInternalResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Cliente interno para comunicarse con hotel-service.
 * Protegido por Circuit Breaker + Retry (Resilience4j).
 *
 * Estrategia de fallback:
 *  - Métodos críticos del flujo de reserva (getHotelById, getHabitacionById,
 *    checkDisponibilidad, getHabitacionesDisponibles) → lanzan ServiceUnavailableException
 *    para que el cliente reciba 503 explícito.
 *  - Métodos de dashboard (getTotal*, getAllHoteles, getHotelesPorDepartamentoReal) →
 *    devuelven valores por defecto (0, lista vacía, mapa vacío) para degradar graciosamente.
 *  - 404 (entidad no existe) sigue siendo Optional.empty() — no es falla del servicio.
 */
@Component
public class HotelInternalApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(HotelInternalApi.class);
    private static final String CB_NAME = "hotelService";

    private final RestTemplate restTemplate;
    private final ServiceTokenProvider serviceTokenProvider;

    @Value("${internal.hotel-service.url}")
    private String hotelServiceUrl;

    public HotelInternalApi(RestTemplate restTemplate, ServiceTokenProvider serviceTokenProvider) {
        this.restTemplate = restTemplate;
        this.serviceTokenProvider = serviceTokenProvider;
    }

    @CircuitBreaker(name = CB_NAME, fallbackMethod = "fallbackGetHotelById")
    @Retry(name = CB_NAME)
    public Optional<HotelInternalResponse> getHotelById(Long hotelId) {
        try {
            String url = hotelServiceUrl + "/api/v1/hoteles/" + hotelId;
            ResponseEntity<HotelInternalResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, authEntity(), HotelInternalResponse.class
            );
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException.NotFound e) {
            LOGGER.warn("Hotel not found: {}", hotelId);
            return Optional.empty();
        }
    }

    @SuppressWarnings("unused")
    private Optional<HotelInternalResponse> fallbackGetHotelById(Long hotelId, Throwable t) {
        LOGGER.error("hotel-service unavailable getting hotel {}: {}", hotelId, t.getMessage());
        throw new ServiceUnavailableException("hotel-service", t);
    }

    @CircuitBreaker(name = CB_NAME, fallbackMethod = "fallbackGetHabitacionById")
    @Retry(name = CB_NAME)
    public Optional<HabitacionInternalResponse> getHabitacionById(Long habitacionId) {
        try {
            String url = hotelServiceUrl + "/api/v1/habitaciones/" + habitacionId;
            ResponseEntity<HabitacionInternalResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, authEntity(), HabitacionInternalResponse.class
            );
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException.NotFound e) {
            LOGGER.warn("Habitacion not found: {}", habitacionId);
            return Optional.empty();
        }
    }

    @SuppressWarnings("unused")
    private Optional<HabitacionInternalResponse> fallbackGetHabitacionById(Long habitacionId, Throwable t) {
        LOGGER.error("hotel-service unavailable getting habitacion {}: {}", habitacionId, t.getMessage());
        throw new ServiceUnavailableException("hotel-service", t);
    }

    @CircuitBreaker(name = CB_NAME, fallbackMethod = "fallbackCheckDisponibilidad")
    @Retry(name = CB_NAME)
    public boolean checkDisponibilidad(Long habitacionId) {
        String url = String.format(
                "%s/api/v1/habitaciones/%d/disponibilidad",
                hotelServiceUrl,
                habitacionId
        );
        ResponseEntity<DisponibilidadResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, authEntity(), DisponibilidadResponse.class
        );
        return response.getBody() != null && Boolean.TRUE.equals(response.getBody().getDisponible());
    }

    @SuppressWarnings("unused")
    private boolean fallbackCheckDisponibilidad(Long habitacionId, Throwable t) {
        LOGGER.error("hotel-service unavailable checking disponibilidad for habitacion {}: {}", habitacionId, t.getMessage());
        throw new ServiceUnavailableException("hotel-service", t);
    }

    @CircuitBreaker(name = CB_NAME, fallbackMethod = "fallbackGetHabitacionesDisponibles")
    @Retry(name = CB_NAME)
    public List<HabitacionInternalResponse> getHabitacionesDisponibles(
            Long hotelId, LocalDate fechaInicio, LocalDate fechaFin) {
        String url = String.format(
                "%s/api/v1/habitaciones?hotelId=%d&fechaInicio=%s&fechaFin=%s",
                hotelServiceUrl,
                hotelId,
                fechaInicio,
                fechaFin
        );
        ResponseEntity<HabitacionesDisponiblesWrapper> response = restTemplate.exchange(
                url, HttpMethod.GET, authEntity(), HabitacionesDisponiblesWrapper.class
        );
        if (response.getBody() != null) {
            return response.getBody().getHabitaciones();
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unused")
    private List<HabitacionInternalResponse> fallbackGetHabitacionesDisponibles(
            Long hotelId, LocalDate fechaInicio, LocalDate fechaFin, Throwable t) {
        LOGGER.error("hotel-service unavailable getting habitaciones disponibles for hotel {}: {}", hotelId, t.getMessage());
        throw new ServiceUnavailableException("hotel-service", t);
    }

    // ======================================================================
    // Dashboard methods — degradación graciosa (default values en fallback)
    // ======================================================================

    @CircuitBreaker(name = CB_NAME, fallbackMethod = "fallbackGetTotalDepartamentos")
    @Retry(name = CB_NAME)
    public int getTotalDepartamentos() {
        String url = hotelServiceUrl + "/api/v1/departamentos";
        ResponseEntity<Object[]> response = restTemplate.exchange(
                url, HttpMethod.GET, authEntity(), Object[].class
        );
        return response.getBody() != null ? response.getBody().length : 0;
    }

    @SuppressWarnings("unused")
    private int fallbackGetTotalDepartamentos(Throwable t) {
        LOGGER.warn("hotel-service unavailable, returning 0 for total departamentos: {}", t.getMessage());
        return 0;
    }

    @CircuitBreaker(name = CB_NAME, fallbackMethod = "fallbackGetTotalHoteles")
    @Retry(name = CB_NAME)
    public int getTotalHoteles() {
        String url = hotelServiceUrl + "/api/v1/hoteles";
        ResponseEntity<Object[]> response = restTemplate.exchange(
                url, HttpMethod.GET, authEntity(), Object[].class
        );
        return response.getBody() != null ? response.getBody().length : 0;
    }

    @SuppressWarnings("unused")
    private int fallbackGetTotalHoteles(Throwable t) {
        LOGGER.warn("hotel-service unavailable, returning 0 for total hoteles: {}", t.getMessage());
        return 0;
    }

    @CircuitBreaker(name = CB_NAME, fallbackMethod = "fallbackGetTotalHabitacionesPorHotel")
    @Retry(name = CB_NAME)
    public int getTotalHabitacionesPorHotel(Long hotelId) {
        String url = hotelServiceUrl + "/api/v1/hoteles/" + hotelId + "/habitaciones";
        ResponseEntity<Object[]> response = restTemplate.exchange(
                url, HttpMethod.GET, authEntity(), Object[].class
        );
        return response.getBody() != null ? response.getBody().length : 0;
    }

    @SuppressWarnings("unused")
    private int fallbackGetTotalHabitacionesPorHotel(Long hotelId, Throwable t) {
        LOGGER.warn("hotel-service unavailable, returning 0 for habitaciones del hotel {}: {}", hotelId, t.getMessage());
        return 0;
    }

    @CircuitBreaker(name = CB_NAME, fallbackMethod = "fallbackGetAllHoteles")
    @Retry(name = CB_NAME)
    public List<HotelInternalResponse> getAllHoteles() {
        String url = hotelServiceUrl + "/api/v1/hoteles";
        ResponseEntity<HotelInternalResponse[]> response = restTemplate.exchange(
                url, HttpMethod.GET, authEntity(), HotelInternalResponse[].class
        );
        if (response.getBody() != null) {
            return List.of(response.getBody());
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unused")
    private List<HotelInternalResponse> fallbackGetAllHoteles(Throwable t) {
        LOGGER.warn("hotel-service unavailable, returning empty list for all hoteles: {}", t.getMessage());
        return Collections.emptyList();
    }

    @CircuitBreaker(name = CB_NAME, fallbackMethod = "fallbackGetTotalHabitaciones")
    @Retry(name = CB_NAME)
    public int getTotalHabitaciones() {
        String url = hotelServiceUrl + "/api/v1/habitaciones";
        ResponseEntity<Object[]> response = restTemplate.exchange(
                url, HttpMethod.GET, authEntity(), Object[].class
        );
        return response.getBody() != null ? response.getBody().length : 0;
    }

    @SuppressWarnings("unused")
    private int fallbackGetTotalHabitaciones(Throwable t) {
        LOGGER.warn("hotel-service unavailable, returning 0 for total habitaciones: {}", t.getMessage());
        return 0;
    }

    @CircuitBreaker(name = CB_NAME, fallbackMethod = "fallbackGetHotelesPorDepartamentoReal")
    @Retry(name = CB_NAME)
    public Map<String, Long> getHotelesPorDepartamentoReal() {
        String depUrl = hotelServiceUrl + "/api/v1/departamentos";
        ResponseEntity<DepartamentoSimple[]> depResponse = restTemplate.exchange(
                depUrl, HttpMethod.GET, authEntity(), DepartamentoSimple[].class
        );

        Map<String, Long> resultado = new LinkedHashMap<>();

        if (depResponse.getBody() != null) {
            for (DepartamentoSimple dep : depResponse.getBody()) {
                resultado.put(dep.getNombre(), 0L);
            }
        }

        List<HotelInternalResponse> hoteles = getAllHoteles();
        for (HotelInternalResponse hotel : hoteles) {
            if (hotel.getDepartamento() != null && hotel.getDepartamento().getNombre() != null) {
                String depNombre = hotel.getDepartamento().getNombre();
                resultado.merge(depNombre, 1L, Long::sum);
            }
        }

        return resultado;
    }

    @SuppressWarnings("unused")
    private Map<String, Long> fallbackGetHotelesPorDepartamentoReal(Throwable t) {
        LOGGER.warn("hotel-service unavailable, returning empty map for hoteles por departamento: {}", t.getMessage());
        return Collections.emptyMap();
    }

    // ======================================================================
    // Helpers
    // ======================================================================

    private HttpEntity<Void> authEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceTokenProvider.getToken());
        return new HttpEntity<>(headers);
    }

    // DTOs internos
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

        public Long getHabitacionId() { return habitacionId; }
        public void setHabitacionId(Long habitacionId) { this.habitacionId = habitacionId; }
        public Boolean getDisponible() { return disponible; }
        public void setDisponible(Boolean disponible) { this.disponible = disponible; }
    }

    private static class HabitacionesDisponiblesWrapper {
        private List<HabitacionInternalResponse> habitaciones;

        public List<HabitacionInternalResponse> getHabitaciones() { return habitaciones; }
        public void setHabitaciones(List<HabitacionInternalResponse> habitaciones) { this.habitaciones = habitaciones; }
    }
}
