package com.hotel.reserva.infrastructure.security;

import com.hotel.reserva.internal.dto.AuthTokenValidationResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthContextFilterTest {

    @Mock private JwtDecoder jwtDecoder;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks
    private AuthContextFilter authContextFilter;

    @Test
    void doFilterInternalSetAttributeCuandoJwtValido() throws Exception {
        Jwt jwt = buildJwt(42L, "ROLE_USER");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtDecoder.decode("valid-token")).thenReturn(jwt);

        authContextFilter.doFilterInternal(request, response, filterChain);

        ArgumentCaptor<AuthTokenValidationResponse> captor =
                ArgumentCaptor.forClass(AuthTokenValidationResponse.class);
        verify(request).setAttribute(eq(AuthContextFilter.AUTH_CONTEXT_KEY), captor.capture());

        AuthTokenValidationResponse auth = captor.getValue();
        assertThat(auth.getValid()).isTrue();
        assertThat(auth.getUserId()).isEqualTo(42L);
        assertThat(auth.getRole()).isEqualTo("USER"); // sin "ROLE_" prefix
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternalNoSetAttributeCuandoNoHayAuthHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        authContextFilter.doFilterInternal(request, response, filterChain);

        verify(request, never()).setAttribute(any(), any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternalNoSetAttributeCuandoNoEsBearer() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        authContextFilter.doFilterInternal(request, response, filterChain);

        verify(request, never()).setAttribute(any(), any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternalContinuaCuandoJwtExceptionLanzada() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(jwtDecoder.decode("invalid-token")).thenThrow(new JwtException("invalid"));

        authContextFilter.doFilterInternal(request, response, filterChain);

        // No debe setear atributo pero si continuar el chain
        verify(request, never()).setAttribute(any(), any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternalNoSetAttributeCuandoUserIdYRolesNulos() throws Exception {
        // JWT sin userId ni roles
        Jwt jwt = Jwt.withTokenValue("tok")
                .header("alg", "RS256")
                .subject("user@test.com")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtDecoder.decode("valid-token")).thenReturn(jwt);

        authContextFilter.doFilterInternal(request, response, filterChain);

        verify(request, never()).setAttribute(any(), any());
        verify(filterChain).doFilter(request, response);
    }

    // ==================== helpers ====================

    private Jwt buildJwt(Long userId, String roles) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .subject("user@test.com")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .claim("userId", userId)
                .claim("roles", roles)
                .build();
    }
}
