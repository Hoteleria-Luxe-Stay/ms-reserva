package com.hotel.reserva.api;

import com.hotel.reserva.api.dto.DashboardStatsResponse;
import com.hotel.reserva.core.dashboard.service.DashboardService;
import com.hotel.reserva.internal.dto.AuthTokenValidationResponse;
import com.hotel.reserva.infrastructure.security.AuthUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

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
        AuthTokenValidationResponse auth = AuthUtils.getAuth(getRequest());
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!AuthUtils.isAdmin(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        DashboardStatsResponse response = dashboardService.obtenerEstadisticas();
        return ResponseEntity.ok(response);
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
    }

}
