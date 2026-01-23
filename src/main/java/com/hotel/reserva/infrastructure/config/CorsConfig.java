package com.hotel.reserva.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${application.cors.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .collect(Collectors.toList());

        CorsRegistration corsRegistration = registry.addMapping("/**");
        corsRegistration.allowedOrigins((origins.isEmpty() ? List.of("http://localhost:4200") : origins)
                .toArray(String[]::new));
        corsRegistration.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH");
        corsRegistration.allowedHeaders(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        );
        corsRegistration.exposedHeaders(
                "Authorization",
                "Content-Type",
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"
        );
        corsRegistration.allowCredentials(true);
        corsRegistration.maxAge(3600L);
    }
}
