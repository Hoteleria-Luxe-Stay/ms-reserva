package com.hotel.reserva.internal;

import com.hotel.reserva.helpers.exceptions.ServiceUnavailableException;
import com.hotel.reserva.internal.dto.CrearPagoInternalRequest;
import com.hotel.reserva.internal.dto.CrearPagoInternalResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PagoInternalApiTest {

    @Mock private RestTemplate restTemplate;
    @Mock private ServiceTokenProvider serviceTokenProvider;

    @InjectMocks
    private PagoInternalApi pagoInternalApi;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pagoInternalApi, "pagoServiceUrl", "http://pago-service");
        when(serviceTokenProvider.getToken()).thenReturn("test-token");
    }

    // ==================== crearPago — happy path ====================

    @Test
    void crearPagoRetornaResponseCuandoExito() {
        CrearPagoInternalRequest request = buildRequest();
        CrearPagoInternalResponse expectedResponse = new CrearPagoInternalResponse();
        expectedResponse.setCheckoutUrl("https://mp.com/checkout/123");

        when(restTemplate.postForEntity(anyString(), any(), eq(CrearPagoInternalResponse.class)))
                .thenReturn(ResponseEntity.ok(expectedResponse));

        CrearPagoInternalResponse result = pagoInternalApi.crearPago(request);

        assertThat(result).isNotNull();
        assertThat(result.getCheckoutUrl()).isEqualTo("https://mp.com/checkout/123");
    }

    // ==================== crearPago — fallback ====================

    @Test
    void fallbackCrearPagoLanzaServiceUnavailableException() {
        CrearPagoInternalRequest request = buildRequest();
        Throwable causa = new RuntimeException("pago-service down");

        assertThatThrownBy(() ->
                ReflectionTestUtils.invokeMethod(pagoInternalApi, "fallbackCrearPago", request, causa)
        ).isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("pago-service");
    }

    @Test
    void fallbackCrearPagoIncluyCausaOriginal() {
        CrearPagoInternalRequest request = buildRequest();
        RuntimeException causa = new RuntimeException("connection refused");

        ServiceUnavailableException ex = null;
        try {
            ReflectionTestUtils.invokeMethod(pagoInternalApi, "fallbackCrearPago", request, (Throwable) causa);
        } catch (ServiceUnavailableException e) {
            ex = e;
        }

        assertThat(ex).isNotNull();
        assertThat(ex.getCause()).isEqualTo(causa);
        assertThat(ex.getServiceName()).isEqualTo("pago-service");
    }

    // ==================== helpers ====================

    private CrearPagoInternalRequest buildRequest() {
        return new CrearPagoInternalRequest(
                1L,
                new BigDecimal("200.00"),
                "USD",
                "Reserva #1 - Hotel Luxe",
                "http://localhost:4200/success",
                "http://localhost:4200/cancel"
        );
    }
}
