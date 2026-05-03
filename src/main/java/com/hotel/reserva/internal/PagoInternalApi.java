package com.hotel.reserva.internal;

import com.hotel.reserva.helpers.exceptions.ServiceUnavailableException;
import com.hotel.reserva.internal.dto.CrearPagoInternalRequest;
import com.hotel.reserva.internal.dto.CrearPagoInternalResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Cliente interno para ms-pago. Protegido por Circuit Breaker + Retry.
 *
 * El JWT que viaja en la llamada es el token tecnico (client_credentials) emitido por
 * auth-service. ms-pago acepta JWTs de service-to-service igual que ms-hotel.
 *
 * Si ms-pago esta caido o lento, el fallback lanza ServiceUnavailableException → 503.
 * Importante: NO marcamos la reserva como PAGO_FALLIDO aca — el caller (ReservaService)
 * debe revertir la transicion a PAGO_EN_PROCESO cuando recibe la excepcion.
 */
@Component
public class PagoInternalApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(PagoInternalApi.class);
    private static final String CB_NAME = "pagoService";

    private final RestTemplate restTemplate;
    private final ServiceTokenProvider serviceTokenProvider;

    @Value("${internal.pago-service.url}")
    private String pagoServiceUrl;

    public PagoInternalApi(RestTemplate restTemplate, ServiceTokenProvider serviceTokenProvider) {
        this.restTemplate = restTemplate;
        this.serviceTokenProvider = serviceTokenProvider;
    }

    @CircuitBreaker(name = CB_NAME, fallbackMethod = "fallbackCrearPago")
    @Retry(name = CB_NAME)
    public CrearPagoInternalResponse crearPago(CrearPagoInternalRequest request) {
        String url = pagoServiceUrl + "/api/v1/pagos";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(serviceTokenProvider.getToken());

        HttpEntity<CrearPagoInternalRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<CrearPagoInternalResponse> response = restTemplate.postForEntity(
                url, entity, CrearPagoInternalResponse.class
        );
        return response.getBody();
    }

    @SuppressWarnings("unused")
    private CrearPagoInternalResponse fallbackCrearPago(CrearPagoInternalRequest request, Throwable t) {
        LOGGER.error("pago-service unavailable creando pago para reserva {}: {}",
                request.getReservaId(), t.getMessage());
        throw new ServiceUnavailableException("pago-service", t);
    }
}
