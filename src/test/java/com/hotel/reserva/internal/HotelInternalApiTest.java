package com.hotel.reserva.internal;

import com.hotel.reserva.helpers.exceptions.ServiceUnavailableException;
import com.hotel.reserva.internal.dto.HabitacionInternalResponse;
import com.hotel.reserva.internal.dto.HotelInternalResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HotelInternalApiTest {

    @Mock private RestTemplate restTemplate;
    @Mock private ServiceTokenProvider serviceTokenProvider;

    @InjectMocks
    private HotelInternalApi hotelInternalApi;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(hotelInternalApi, "hotelServiceUrl", "http://hotel-service");
        when(serviceTokenProvider.getToken()).thenReturn("test-token");
    }

    // ==================== getHotelById — happy path ====================

    @Test
    void getHotelByIdRetornaHotelCuandoExiste() {
        HotelInternalResponse hotel = new HotelInternalResponse();
        hotel.setId(1L);
        hotel.setNombre("Hotel Luxe");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(HotelInternalResponse.class)))
                .thenReturn(ResponseEntity.ok(hotel));

        Optional<HotelInternalResponse> result = hotelInternalApi.getHotelById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("Hotel Luxe");
    }

    @Test
    void getHotelByIdRetornaEmptyCuandoHotelNoExiste() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(HotelInternalResponse.class)))
                .thenThrow(HttpClientErrorException.NotFound.create(HttpStatus.NOT_FOUND, "Not Found",
                        null, null, null));

        Optional<HotelInternalResponse> result = hotelInternalApi.getHotelById(99L);

        assertThat(result).isEmpty();
    }

    // ==================== getHotelById — fallback ====================

    @Test
    void fallbackGetHotelByIdLanzaServiceUnavailableException() {
        Throwable causa = new RuntimeException("simulated downstream failure");

        assertThatThrownBy(() ->
                ReflectionTestUtils.invokeMethod(hotelInternalApi, "fallbackGetHotelById", 1L, causa)
        ).isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("hotel-service");
    }

    // ==================== getHabitacionById ====================

    @Test
    void getHabitacionByIdRetornaHabitacionCuandoExiste() {
        HabitacionInternalResponse habitacion = new HabitacionInternalResponse();
        habitacion.setId(10L);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(HabitacionInternalResponse.class)))
                .thenReturn(ResponseEntity.ok(habitacion));

        Optional<HabitacionInternalResponse> result = hotelInternalApi.getHabitacionById(10L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(10L);
    }

    @Test
    void getHabitacionByIdRetornaEmptyCuando404() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(HabitacionInternalResponse.class)))
                .thenThrow(HttpClientErrorException.NotFound.create(HttpStatus.NOT_FOUND, "Not Found",
                        null, null, null));

        Optional<HabitacionInternalResponse> result = hotelInternalApi.getHabitacionById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void fallbackGetHabitacionByIdLanzaServiceUnavailableException() {
        Throwable causa = new RuntimeException("hotel-service down");

        assertThatThrownBy(() ->
                ReflectionTestUtils.invokeMethod(hotelInternalApi, "fallbackGetHabitacionById", 1L, causa)
        ).isInstanceOf(ServiceUnavailableException.class);
    }

    // ==================== checkDisponibilidad ====================

    @Test
    void checkDisponibilidadRetornaTrueCuandoDisponible() throws Exception {
        Object dispResponse = crearDisponibilidadResponse(true);
        @SuppressWarnings("unchecked")
        ResponseEntity<Object> response = (ResponseEntity<Object>) (Object) ResponseEntity.ok(dispResponse);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(Class.class)))
                .thenReturn(response);

        boolean result = hotelInternalApi.checkDisponibilidad(10L);

        assertThat(result).isTrue();
    }

    @Test
    void checkDisponibilidadRetornaFalseCuandoNoDisponible() throws Exception {
        Object dispResponse = crearDisponibilidadResponse(false);
        @SuppressWarnings("unchecked")
        ResponseEntity<Object> response = (ResponseEntity<Object>) (Object) ResponseEntity.ok(dispResponse);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(Class.class)))
                .thenReturn(response);

        boolean result = hotelInternalApi.checkDisponibilidad(10L);

        assertThat(result).isFalse();
    }

    @Test
    void checkDisponibilidadRetornaFalseCuandoBodyNulo() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(Class.class)))
                .thenReturn(ResponseEntity.ok(null));

        boolean result = hotelInternalApi.checkDisponibilidad(10L);

        assertThat(result).isFalse();
    }

    @Test
    void fallbackCheckDisponibilidadLanzaServiceUnavailableException() {
        Throwable causa = new RuntimeException("timeout");

        assertThatThrownBy(() ->
                ReflectionTestUtils.invokeMethod(hotelInternalApi, "fallbackCheckDisponibilidad", 10L, causa)
        ).isInstanceOf(ServiceUnavailableException.class);
    }

    // ==================== getHabitacionesDisponibles ====================

    @Test
    void fallbackGetHabitacionesDisponiblesLanzaServiceUnavailableException() {
        Throwable causa = new RuntimeException("hotel-service unavailable");

        assertThatThrownBy(() ->
                ReflectionTestUtils.invokeMethod(hotelInternalApi, "fallbackGetHabitacionesDisponibles",
                        1L, LocalDate.now(), LocalDate.now().plusDays(2), causa)
        ).isInstanceOf(ServiceUnavailableException.class);
    }

    // ==================== Dashboard fallbacks — degradación graciosa ====================

    @Test
    void fallbackGetTotalDepartamentosRetornaCero() {
        Throwable causa = new RuntimeException("hotel-service down");

        Object result = ReflectionTestUtils.invokeMethod(
                hotelInternalApi, "fallbackGetTotalDepartamentos", causa);

        assertThat(result).isEqualTo(0);
    }

    @Test
    void fallbackGetTotalHotelesRetornaCero() {
        Throwable causa = new RuntimeException("hotel-service down");

        Object result = ReflectionTestUtils.invokeMethod(
                hotelInternalApi, "fallbackGetTotalHoteles", causa);

        assertThat(result).isEqualTo(0);
    }

    @Test
    void fallbackGetTotalHabitacionesPorHotelRetornaCero() {
        Throwable causa = new RuntimeException("hotel-service down");

        Object result = ReflectionTestUtils.invokeMethod(
                hotelInternalApi, "fallbackGetTotalHabitacionesPorHotel", 1L, causa);

        assertThat(result).isEqualTo(0);
    }

    @Test
    void fallbackGetTotalHabitacionesRetornaCero() {
        Throwable causa = new RuntimeException("hotel-service down");

        Object result = ReflectionTestUtils.invokeMethod(
                hotelInternalApi, "fallbackGetTotalHabitaciones", causa);

        assertThat(result).isEqualTo(0);
    }

    @Test
    void fallbackGetAllHotelesRetornaListaVacia() {
        Throwable causa = new RuntimeException("hotel-service down");

        Object result = ReflectionTestUtils.invokeMethod(
                hotelInternalApi, "fallbackGetAllHoteles", causa);

        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void fallbackGetHotelesPorDepartamentoRealRetornaMapaVacio() {
        Throwable causa = new RuntimeException("hotel-service down");

        Object result = ReflectionTestUtils.invokeMethod(
                hotelInternalApi, "fallbackGetHotelesPorDepartamentoReal", causa);

        assertThat(result).isEqualTo(Collections.emptyMap());
    }

    // ==================== getTotalDepartamentos — happy path ====================

    @Test
    void getTotalDepartamentosRetornaCuenta() {
        Object[] deps = new Object[]{new Object(), new Object()};
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Object[].class)))
                .thenReturn(ResponseEntity.ok(deps));

        int result = hotelInternalApi.getTotalDepartamentos();

        assertThat(result).isEqualTo(2);
    }

    @Test
    void getTotalDepartamentosRetornaCeroCuandoBodyNulo() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Object[].class)))
                .thenReturn(ResponseEntity.ok(null));

        int result = hotelInternalApi.getTotalDepartamentos();

        assertThat(result).isEqualTo(0);
    }

    @Test
    void getTotalHotelesRetornaCuenta() {
        Object[] hoteles = new Object[]{new Object()};
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Object[].class)))
                .thenReturn(ResponseEntity.ok(hoteles));

        int result = hotelInternalApi.getTotalHoteles();

        assertThat(result).isEqualTo(1);
    }

    @Test
    void getTotalHabitacionesPorHotelRetornaCuenta() {
        Object[] habs = new Object[]{new Object(), new Object(), new Object()};
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Object[].class)))
                .thenReturn(ResponseEntity.ok(habs));

        int result = hotelInternalApi.getTotalHabitacionesPorHotel(1L);

        assertThat(result).isEqualTo(3);
    }

    @Test
    void getTotalHabitacionesRetornaCuenta() {
        Object[] habs = new Object[]{new Object()};
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Object[].class)))
                .thenReturn(ResponseEntity.ok(habs));

        int result = hotelInternalApi.getTotalHabitaciones();

        assertThat(result).isEqualTo(1);
    }

    @Test
    void getAllHotelesRetornaListaCuandoBodyNoNulo() {
        HotelInternalResponse hotel = new HotelInternalResponse();
        hotel.setId(1L);
        HotelInternalResponse[] arr = {hotel};
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(HotelInternalResponse[].class)))
                .thenReturn(ResponseEntity.ok(arr));

        List<HotelInternalResponse> result = hotelInternalApi.getAllHoteles();

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllHotelesRetornaListaVaciaCuandoBodyNulo() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(HotelInternalResponse[].class)))
                .thenReturn(ResponseEntity.ok(null));

        List<HotelInternalResponse> result = hotelInternalApi.getAllHoteles();

        assertThat(result).isEmpty();
    }

    // ==================== helpers ====================

    /**
     * Crea una instancia de la clase interna privada DisponibilidadResponse usando reflection.
     * Necesitamos setAccessible en el constructor porque la clase es private static.
     */
    private Object crearDisponibilidadResponse(boolean disponible) throws Exception {
        Class<?> clazz = Class.forName(
                "com.hotel.reserva.internal.HotelInternalApi$DisponibilidadResponse");
        java.lang.reflect.Constructor<?> ctor = clazz.getDeclaredConstructor();
        ctor.setAccessible(true);
        Object obj = ctor.newInstance();
        java.lang.reflect.Method setDisponible = clazz.getDeclaredMethod("setDisponible", Boolean.class);
        setDisponible.setAccessible(true);
        setDisponible.invoke(obj, disponible);
        return obj;
    }
}
