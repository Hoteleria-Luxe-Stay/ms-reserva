package com.hotel.reserva.internal;

import com.hotel.reserva.internal.dto.AuthTokenValidationRequest;
import com.hotel.reserva.internal.dto.AuthTokenValidationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
public class AuthInternalApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthInternalApi.class);

    private final RestTemplate restTemplate;

    @Value("${internal.auth-service.url}")
    private String authServiceUrl;

    public AuthInternalApi(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Optional<AuthTokenValidationResponse> validateToken(String token) {
        try {
            String url = authServiceUrl + "/api/v1/auth/validate";
            AuthTokenValidationRequest request = new AuthTokenValidationRequest();
            request.setToken(token);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<AuthTokenValidationRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<AuthTokenValidationResponse> response = restTemplate.postForEntity(
                    url,
                    entity,
                    AuthTokenValidationResponse.class
            );

            return Optional.ofNullable(response.getBody());
        } catch (Exception e) {
            LOGGER.warn("Error validating token: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
