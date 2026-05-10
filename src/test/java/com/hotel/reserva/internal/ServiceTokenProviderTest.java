package com.hotel.reserva.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceTokenProviderTest {

    @Mock private RestTemplate restTemplate;

    @InjectMocks
    private ServiceTokenProvider serviceTokenProvider;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(serviceTokenProvider, "authServiceUrl", "http://auth-service");
        ReflectionTestUtils.setField(serviceTokenProvider, "clientId", "reserva-client");
        ReflectionTestUtils.setField(serviceTokenProvider, "clientSecret", "secret-123");
        // Reset token cache
        ReflectionTestUtils.setField(serviceTokenProvider, "current", new AtomicReference<>(null));
    }

    @Test
    void getTokenFetchesFreshTokenCuandoNoHayCache() {
        Map<String, Object> responseBody = Map.of(
                "access_token", "my-token",
                "expires_in", 300
        );
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        String token = serviceTokenProvider.getToken();

        assertThat(token).isEqualTo("my-token");
    }

    @Test
    void getTokenUsaCacheCuandoTokenEsFresh() {
        Map<String, Object> responseBody = Map.of(
                "access_token", "cached-token",
                "expires_in", 300
        );
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        // Primera llamada → fetch
        String token1 = serviceTokenProvider.getToken();
        // Segunda llamada → cache hit
        String token2 = serviceTokenProvider.getToken();

        assertThat(token1).isEqualTo("cached-token");
        assertThat(token2).isEqualTo("cached-token");
        // Solo debe llamar al endpoint una vez
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(Map.class));
    }

    @Test
    void getTokenLanzaExcepcionCuandoResponseBodyNulo() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(null));

        assertThatThrownBy(() -> serviceTokenProvider.getToken())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No se pudo obtener token tecnico");
    }

    @Test
    void getTokenLanzaExcepcionCuandoAccessTokenAusente() {
        Map<String, Object> responseBody = Map.of("token_type", "Bearer");
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        assertThatThrownBy(() -> serviceTokenProvider.getToken())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No se pudo obtener token tecnico");
    }

    @Test
    void getTokenManejaTtlComoString() {
        Map<String, Object> responseBody = Map.of(
                "access_token", "string-ttl-token",
                "expires_in", "600"
        );
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        String token = serviceTokenProvider.getToken();

        assertThat(token).isEqualTo("string-ttl-token");
    }

    @Test
    void getTokenUsaTtlDefaultCuandoExpiresInInvalido() {
        Map<String, Object> responseBody = Map.of(
                "access_token", "default-ttl-token",
                "expires_in", "not-a-number"
        );
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        // No debe lanzar excepcion
        String token = serviceTokenProvider.getToken();

        assertThat(token).isEqualTo("default-ttl-token");
    }
}
