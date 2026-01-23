package com.hotel.reserva.api;

import com.hotel.reserva.api.dto.MessageResponse;
import com.hotel.reserva.api.dto.ReservaAdminUpdateRequest;
import com.hotel.reserva.api.dto.ReservaCreatedResponse;
import com.hotel.reserva.api.dto.ReservaListResponse;
import com.hotel.reserva.api.dto.ReservaRequest;
import com.hotel.reserva.api.dto.ReservaResponse;
import com.hotel.reserva.core.reserva.model.Reserva;
import com.hotel.reserva.core.reserva.service.ReservaService;
import com.hotel.reserva.helpers.mappers.ReservaMapper;
import com.hotel.reserva.internal.AuthInternalApi;
import com.hotel.reserva.internal.dto.AuthTokenValidationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;
import java.util.Optional;

@RestController
public class ReservasController implements ReservasApi {

    private final ReservaService reservaService;
    private final AuthInternalApi authInternalApi;
    private final NativeWebRequest request;

    public ReservasController(ReservaService reservaService,
                              AuthInternalApi authInternalApi,
                              NativeWebRequest request) {
        this.reservaService = reservaService;
        this.authInternalApi = authInternalApi;
        this.request = request;
    }

    @Override
    public ResponseEntity<ReservaCreatedResponse> crearReserva(ReservaRequest request) {
        Reserva reserva = reservaService.crearReserva(request, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(ReservaMapper.toCreatedResponse(reserva));
    }

    @Override
    public ResponseEntity<ReservaResponse> obtenerReserva(Long id) {
        Reserva reserva = reservaService.buscarPorId(id);
        return ResponseEntity.ok(ReservaMapper.toResponse(reserva));
    }

    @Override
    public ResponseEntity<ReservaResponse> confirmarPago(Long id) {
        Reserva reserva = reservaService.confirmarPago(id);
        return ResponseEntity.ok(ReservaMapper.toResponse(reserva));
    }

    @Override
    public ResponseEntity<ReservaResponse> cancelarReserva(Long id) {
        Reserva reserva = reservaService.cancelarReserva(id);
        return ResponseEntity.ok(ReservaMapper.toResponse(reserva));
    }

    @Override
    public ResponseEntity<List<ReservaListResponse>> listarReservasAdmin(String dni, String estado) {
        AuthTokenValidationResponse auth = resolveAuth();
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!isAdmin(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Reserva> reservas = reservaService.listarReservas(dni, estado);
        return ResponseEntity.ok(ReservaMapper.toListResponseList(reservas));
    }

    @Override
    public ResponseEntity<ReservaListResponse> actualizarReservaAdmin(Long id, ReservaAdminUpdateRequest request) {
        AuthTokenValidationResponse auth = resolveAuth();
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!isAdmin(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Reserva reserva = reservaService.actualizarReservaAdmin(id, request);
        return ResponseEntity.ok(ReservaMapper.toListResponse(reserva));
    }

    @Override
    public ResponseEntity<MessageResponse> eliminarReserva(Long id) {
        AuthTokenValidationResponse auth = resolveAuth();
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!isAdmin(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        reservaService.eliminar(id);

        MessageResponse response = new MessageResponse();
        response.setMessage("Reserva eliminada correctamente");
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
        return request.get().getHeader("Authorization");
    }

    private boolean isAdmin(AuthTokenValidationResponse auth) {
        return auth.getRole() != null && "ADMIN".equalsIgnoreCase(auth.getRole());
    }
}
