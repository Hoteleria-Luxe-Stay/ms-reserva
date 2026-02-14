package com.hotel.reserva.infrastructure.security;

import com.hotel.reserva.internal.dto.AuthTokenValidationResponse;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;

import java.util.Optional;

public final class AuthUtils {

    private AuthUtils() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static AuthTokenValidationResponse getAuth(Optional<NativeWebRequest> request) {
        if (request == null || request.isEmpty()) {
            return null;
        }
        Object value = request.get().getAttribute(AuthContextFilter.AUTH_CONTEXT_KEY, RequestAttributes.SCOPE_REQUEST);
        if (value instanceof AuthTokenValidationResponse response) {
            return response;
        }
        return null;
    }

    public static boolean isAdmin(AuthTokenValidationResponse auth) {
        return auth != null && auth.getRole() != null && "ADMIN".equalsIgnoreCase(auth.getRole());
    }
}
