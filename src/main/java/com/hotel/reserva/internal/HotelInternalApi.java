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
import java.util.List;
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
     * Verifica la disponibilidad de una habitacion.
     */
    public boolean checkDisponibilidad(Long habitacionId, LocalDate fechaInicio, LocalDate fechaFin) {
        try {
            String url = String.format(
                    "%s/api/v1/habitaciones/%d/disponibilidad?fechaInicio=%s&fechaFin=%s",
                    hotelServiceUrl,
                    habitacionId,
                    fechaInicio,
                    fechaFin
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

    // DTOs internos para respuestas especificas
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
