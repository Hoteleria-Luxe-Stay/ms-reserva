package com.hotel.reserva.infrastructure.security;

import com.hotel.reserva.internal.AuthInternalApi;
import com.hotel.reserva.internal.dto.AuthTokenValidationResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class AuthContextFilter extends OncePerRequestFilter {

    public static final String AUTH_CONTEXT_KEY = "AUTH_CONTEXT";

    private final AuthInternalApi authInternalApi;

    public AuthContextFilter(AuthInternalApi authInternalApi) {
        this.authInternalApi = authInternalApi;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            Optional<AuthTokenValidationResponse> validation = authInternalApi.validateToken(token);
            if (validation.isPresent() && Boolean.TRUE.equals(validation.get().getValid())) {
                request.setAttribute(AUTH_CONTEXT_KEY, validation.get());
            }
        }

        filterChain.doFilter(request, response);
    }
}
