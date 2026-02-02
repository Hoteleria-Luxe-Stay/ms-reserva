package com.hotel.reserva.api;

import com.hotel.reserva.api.dto.DashboardStatsResponse;
import com.hotel.reserva.core.dashboard.service.DashboardService;
import com.hotel.reserva.internal.dto.AuthTokenValidationResponse;
import com.hotel.reserva.infrastructure.security.AuthContextFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;

import java.util.Optional;

@RestController
public class DashboardController implements DashboardApi {

    private final DashboardService dashboardService;
    private final NativeWebRequest request;

    public DashboardController(DashboardService dashboardService,
                               NativeWebRequest request) {
        this.dashboardService = dashboardService;
        this.request = request;
    }

    @Override
    public ResponseEntity<DashboardStatsResponse> obtenerDashboardStats() {
        AuthTokenValidationResponse auth = getAuth();
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!isAdmin(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        DashboardStatsResponse response = dashboardService.obtenerEstadisticas();
        return ResponseEntity.ok(response);
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
    }

    private AuthTokenValidationResponse getAuth() {
        Optional<NativeWebRequest> request = getRequest();
        if (request.isEmpty()) {
            return null;
        }
        Object value = request.get().getAttribute(AuthContextFilter.AUTH_CONTEXT_KEY, RequestAttributes.SCOPE_REQUEST);
        if (value instanceof AuthTokenValidationResponse response) {
            return response;
        }
        return null;
    }

    private boolean isAdmin(AuthTokenValidationResponse auth) {
        return auth.getRole() != null && "ADMIN".equalsIgnoreCase(auth.getRole());
    }
}
