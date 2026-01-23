package com.hotel.reserva.api;

import com.hotel.reserva.api.dto.MisReservasResponse;
import com.hotel.reserva.api.dto.ReservaResponse;
import com.hotel.reserva.api.dto.ReservaUpdateRequest;
import com.hotel.reserva.core.reserva.model.Reserva;
import com.hotel.reserva.core.reserva.service.ReservaService;
import com.hotel.reserva.helpers.exceptions.UnauthorizedException;
import com.hotel.reserva.helpers.mappers.ReservaMapper;
import com.hotel.reserva.internal.AuthInternalApi;
import com.hotel.reserva.internal.dto.AuthTokenValidationResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
public class MisReservasController implements MisReservasApi {

    private final ReservaService reservaService;
    private final AuthInternalApi authInternalApi;
    private final NativeWebRequest request;

    public MisReservasController(ReservaService reservaService,
                                 AuthInternalApi authInternalApi,
                                 NativeWebRequest request) {
        this.reservaService = reservaService;
        this.authInternalApi = authInternalApi;
        this.request = request;
    }

    @Override
    public ResponseEntity<List<MisReservasResponse>> listarMisReservas(
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String estado) {
        Long userId = resolveUserId(resolveAuthorization());
        List<Reserva> reservas = reservaService.buscarReservasPorUsuarioIdYFechas(userId, fechaInicio, fechaFin, estado);
        List<MisReservasResponse> response = reservas.stream()
                .map(ReservaMapper::toMisReservasResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ReservaResponse> obtenerMiReserva(
            Long id) {
        Long userId = resolveUserId(resolveAuthorization());
        Reserva reserva = reservaService.buscarPorId(id);
        validarReservaPerteneceUsuario(reserva, userId);
        return ResponseEntity.ok(ReservaMapper.toResponse(reserva));
    }

    @Override
    public ResponseEntity<ReservaResponse> actualizarMiReserva(
            Long id,
            ReservaUpdateRequest request) {
        Long userId = resolveUserId(resolveAuthorization());
        Reserva reserva = reservaService.buscarPorId(id);
        validarReservaPerteneceUsuario(reserva, userId);

        Reserva actualizada = reservaService.actualizarFechas(id, request);
        return ResponseEntity.ok(ReservaMapper.toResponse(actualizada));
    }

    private Long resolveUserId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new UnauthorizedException("Token inválido o ausente");
        }

        String token = authorization.substring(7);
        AuthTokenValidationResponse response = authInternalApi.validateToken(token)
                .orElseThrow(() -> new UnauthorizedException("Token inválido o expirado"));

        if (!Boolean.TRUE.equals(response.getValid())) {
            throw new UnauthorizedException("Token inválido o expirado");
        }

        if (response.getUserId() == null) {
            throw new UnauthorizedException("Token inválido o expirado");
        }

        return response.getUserId();
    }

    private String resolveAuthorization() {
        Optional<NativeWebRequest> request = getRequest();
        if (request.isEmpty()) {
            return null;
        }
        return request.get().getHeader(HttpHeaders.AUTHORIZATION);
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
    }

    private void validarReservaPerteneceUsuario(Reserva reserva, Long userId) {
        if (reserva.getCliente() == null || reserva.getCliente().getUserId() == null) {
            throw new UnauthorizedException("No autorizado para ver esta reserva");
        }
        if (!reserva.getCliente().getUserId().equals(userId)) {
            throw new UnauthorizedException("No autorizado para ver esta reserva");
        }
    }
}
