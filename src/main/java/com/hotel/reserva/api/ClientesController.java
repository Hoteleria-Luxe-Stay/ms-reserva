package com.hotel.reserva.api;

import com.hotel.reserva.api.dto.ClienteResponse;
import com.hotel.reserva.core.cliente.model.Cliente;
import com.hotel.reserva.core.cliente.service.ClienteService;
import com.hotel.reserva.helpers.mappers.ClienteMapper;
import com.hotel.reserva.internal.dto.AuthTokenValidationResponse;
import com.hotel.reserva.infrastructure.security.AuthContextFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;

import java.util.List;
import java.util.Optional;

@RestController
public class ClientesController implements ClientesApi {

    private final ClienteService clienteService;
    private final NativeWebRequest request;

    public ClientesController(ClienteService clienteService, NativeWebRequest request) {
        this.clienteService = clienteService;
        this.request = request;
    }

    @Override
    public ResponseEntity<List<ClienteResponse>> listarClientes() {
        AuthTokenValidationResponse auth = getAuth();
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
        AuthTokenValidationResponse auth = getAuth();
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
        AuthTokenValidationResponse auth = getAuth();
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!isAdmin(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Cliente cliente = clienteService.buscarPorDni(dni);
        return ResponseEntity.ok(ClienteMapper.toResponse(cliente));
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
