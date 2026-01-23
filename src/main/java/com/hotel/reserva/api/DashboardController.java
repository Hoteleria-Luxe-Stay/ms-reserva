package com.hotel.reserva.api;

import com.hotel.reserva.api.dto.DashboardStatsResponse;
import com.hotel.reserva.core.dashboard.service.DashboardService;
import com.hotel.reserva.internal.AuthInternalApi;
import com.hotel.reserva.internal.dto.AuthTokenValidationResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Optional;

@RestController
public class DashboardController implements DashboardApi {

    private final DashboardService dashboardService;
    private final AuthInternalApi authInternalApi;
    private final NativeWebRequest request;

    public DashboardController(DashboardService dashboardService,
                               AuthInternalApi authInternalApi,
                               NativeWebRequest request) {
        this.dashboardService = dashboardService;
        this.authInternalApi = authInternalApi;
        this.request = request;
    }

    @Override
    public ResponseEntity<DashboardStatsResponse> obtenerDashboardStats() {
        AuthTokenValidationResponse auth = resolveAuth();
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

    private AuthTokenValidationResponse resolveAuth() {
        String authorization = resolveAuthorization();
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        String token = authorization.substring(7);
        AuthTokenValidationResponse response = authInternalApi.validateToken(token).orElse(null);
        if (response == null || !Boolean.TRUE.equals(response.getValid())) {
            return null;
        }
        return response;
    }

    private String resolveAuthorization() {
        Optional<NativeWebRequest> request = getRequest();
        if (request.isEmpty()) {
            return null;
        }
        return request.get().getHeader(HttpHeaders.AUTHORIZATION);
    }

    private boolean isAdmin(AuthTokenValidationResponse auth) {
        return auth.getRole() != null && "ADMIN".equalsIgnoreCase(auth.getRole());
    }
}
