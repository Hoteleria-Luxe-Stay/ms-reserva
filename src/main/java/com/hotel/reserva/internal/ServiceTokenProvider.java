package com.hotel.reserva.internal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;

@Component
public class ServiceTokenProvider {

    private final RestTemplate restTemplate;

    @Value("${internal.auth-service.url}")
    private String authServiceUrl;

    @Value("${internal.auth-service.client-id}")
    private String clientId;

    @Value("${internal.auth-service.client-secret}")
    private String clientSecret;

    private String cachedToken;
    private Instant expiresAt;

    public ServiceTokenProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public synchronized String getToken() {
        if (isTokenValid()) {
            return cachedToken;
        }
        requestToken();
        return cachedToken;
    }

    private boolean isTokenValid() {
        return cachedToken != null
                && expiresAt != null
                && Instant.now().isBefore(expiresAt.minusSeconds(30));
    }

    private void requestToken() {
        String url = authServiceUrl + "/api/v1/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        Map responseBody = response.getBody();
        if (responseBody == null || responseBody.get("access_token") == null) {
            throw new IllegalStateException("No se pudo obtener token tecnico");
        }

        cachedToken = responseBody.get("access_token").toString();
        long expiresInSeconds = parseExpiresIn(responseBody.get("expires_in"));
        expiresAt = Instant.now().plusSeconds(expiresInSeconds);
    }

    private long parseExpiresIn(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ex) {
                return 300L;
            }
        }
        return 300L;
    }
}
