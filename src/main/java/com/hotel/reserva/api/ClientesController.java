package com.hotel.reserva.api;

import com.hotel.reserva.api.dto.ClienteResponse;
import com.hotel.reserva.core.cliente.model.Cliente;
import com.hotel.reserva.core.cliente.service.ClienteService;
import com.hotel.reserva.helpers.mappers.ClienteMapper;
import com.hotel.reserva.internal.AuthInternalApi;
import com.hotel.reserva.internal.dto.AuthTokenValidationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;
import java.util.Optional;

@RestController
public class ClientesController implements ClientesApi {

    private final ClienteService clienteService;
    private final AuthInternalApi authInternalApi;

    public ClientesController(ClienteService clienteService, AuthInternalApi authInternalApi) {
        this.clienteService = clienteService;
        this.authInternalApi = authInternalApi;
    }

    @Override
    public ResponseEntity<List<ClienteResponse>> listarClientes() {
        AuthTokenValidationResponse auth = resolveAuth();
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!isAdmin(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Cliente> clientes = clienteService.listar();
        List<ClienteResponse> response = clientes.stream()
                .map(ClienteMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ClienteResponse> obtenerCliente(Long id) {
        AuthTokenValidationResponse auth = resolveAuth();
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!isAdmin(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Cliente cliente = clienteService.buscarPorId(id);
        return ResponseEntity.ok(ClienteMapper.toResponse(cliente));
    }

    @Override
    public ResponseEntity<ClienteResponse> buscarClientePorDni(String dni) {
        AuthTokenValidationResponse auth = resolveAuth();
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!isAdmin(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Cliente cliente = clienteService.buscarPorDni(dni);
        return ResponseEntity.ok(ClienteMapper.toResponse(cliente));
    }

    private AuthTokenValidationResponse resolveAuth() {
        String authorization = resolveAuthorization();
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        String token = authorization.substring(7);
        return authInternalApi.validateToken(token).orElse(null);
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
