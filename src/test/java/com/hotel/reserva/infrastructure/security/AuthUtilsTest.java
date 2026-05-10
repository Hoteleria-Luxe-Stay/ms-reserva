package com.hotel.reserva.infrastructure.security;

import com.hotel.reserva.internal.dto.AuthTokenValidationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthUtilsTest {

    // ==================== getAuth ====================

    @Test
    void getAuthRetornaAuthCuandoEstaEnRequest() {
        NativeWebRequest nativeRequest = mock(NativeWebRequest.class);
        AuthTokenValidationResponse auth = new AuthTokenValidationResponse();
        auth.setValid(true);
        auth.setRole("ADMIN");

        when(nativeRequest.getAttribute(
                eq(AuthContextFilter.AUTH_CONTEXT_KEY),
                eq(RequestAttributes.SCOPE_REQUEST)
        )).thenReturn(auth);

        AuthTokenValidationResponse result = AuthUtils.getAuth(Optional.of(nativeRequest));

        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void getAuthRetornaNullCuandoRequestNulo() {
        assertThat(AuthUtils.getAuth(null)).isNull();
    }

    @Test
    void getAuthRetornaNullCuandoRequestVacio() {
        assertThat(AuthUtils.getAuth(Optional.empty())).isNull();
    }

    @Test
    void getAuthRetornaNullCuandoAtributoNoEsAuthTokenValidationResponse() {
        NativeWebRequest nativeRequest = mock(NativeWebRequest.class);
        when(nativeRequest.getAttribute(
                eq(AuthContextFilter.AUTH_CONTEXT_KEY),
                eq(RequestAttributes.SCOPE_REQUEST)
        )).thenReturn("not-the-right-type");

        AuthTokenValidationResponse result = AuthUtils.getAuth(Optional.of(nativeRequest));

        assertThat(result).isNull();
    }

    @Test
    void getAuthRetornaNullCuandoAtributoNulo() {
        NativeWebRequest nativeRequest = mock(NativeWebRequest.class);
        when(nativeRequest.getAttribute(
                eq(AuthContextFilter.AUTH_CONTEXT_KEY),
                eq(RequestAttributes.SCOPE_REQUEST)
        )).thenReturn(null);

        AuthTokenValidationResponse result = AuthUtils.getAuth(Optional.of(nativeRequest));

        assertThat(result).isNull();
    }

    // ==================== isAdmin ====================

    @Test
    void isAdminRetornaTrueCuandoRoleAdmin() {
        AuthTokenValidationResponse auth = new AuthTokenValidationResponse();
        auth.setRole("ADMIN");

        assertThat(AuthUtils.isAdmin(auth)).isTrue();
    }

    @Test
    void isAdminRetornaTrueCuandoRoleAdminMinusculas() {
        AuthTokenValidationResponse auth = new AuthTokenValidationResponse();
        auth.setRole("admin");

        assertThat(AuthUtils.isAdmin(auth)).isTrue();
    }

    @Test
    void isAdminRetornaFalseCuandoRoleNoAdmin() {
        AuthTokenValidationResponse auth = new AuthTokenValidationResponse();
        auth.setRole("USER");

        assertThat(AuthUtils.isAdmin(auth)).isFalse();
    }

    @Test
    void isAdminRetornaFalseCuandoAuthNulo() {
        assertThat(AuthUtils.isAdmin(null)).isFalse();
    }

    @Test
    void isAdminRetornaFalseCuandoRoleNulo() {
        AuthTokenValidationResponse auth = new AuthTokenValidationResponse();
        auth.setRole(null);

        assertThat(AuthUtils.isAdmin(auth)).isFalse();
    }

    @Test
    void constructorLanzaUnsupportedOperationException() {
        assertThatThrownBy(() -> {
            java.lang.reflect.Constructor<AuthUtils> constructor =
                    AuthUtils.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        }).hasCauseInstanceOf(UnsupportedOperationException.class);
    }
}
