package com.hotel.reserva.helpers.exceptions;

import com.hotel.reserva.api.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock private HttpServletRequest request;

    @InjectMocks
    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/api/v1/test");
    }

    @Test
    void handleEntityNotFoundRetorna404() {
        EntityNotFoundException ex = new EntityNotFoundException("Reserva", 1L);

        ResponseEntity<ErrorResponse> response = handler.handleEntityNotFoundException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
    }

    @Test
    void handleBusinessExceptionRetorna400() {
        BusinessException ex = new BusinessException("error de negocio", "ESTADO_INVALIDO");

        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError()).isEqualTo("ESTADO_INVALIDO");
        assertThat(response.getBody().getMessage()).isEqualTo("error de negocio");
    }

    @Test
    void handleValidationExceptionRetorna400() {
        ValidationException ex = new ValidationException("campo", "El campo es requerido");

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError()).isEqualTo("Validation Error");
    }

    @Test
    void handleConflictExceptionRetorna409() {
        ConflictException ex = new ConflictException("conflicto", "HABITACION_OCUPADA");

        ResponseEntity<ErrorResponse> response = handler.handleConflictException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getError()).isEqualTo("HABITACION_OCUPADA");
    }

    @Test
    void handleUnauthorizedExceptionRetorna401() {
        UnauthorizedException ex = new UnauthorizedException("Sin permisos");

        ResponseEntity<ErrorResponse> response = handler.handleUnauthorizedException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getError()).isEqualTo("Unauthorized");
    }

    @Test
    void handleMethodArgumentNotValidRetorna400ConErroresDeField() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objeto", "nombre", "El nombre es requerido");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValid(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError()).isEqualTo("Validation Failed");
    }

    @Test
    void handleServiceUnavailableRetorna503() {
        ServiceUnavailableException ex = new ServiceUnavailableException("hotel-service");

        ResponseEntity<ErrorResponse> response = handler.handleServiceUnavailable(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody().getMessage()).contains("hotel-service");
    }

    @Test
    void handleIllegalStateRetorna400() {
        IllegalStateException ex = new IllegalStateException("Transicion invalida");

        ResponseEntity<ErrorResponse> response = handler.handleIllegalState(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError()).isEqualTo("Invalid State Transition");
    }

    @Test
    void handleDataIntegrityRetorna409() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("constraint violation");

        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrity(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getError()).isEqualTo("Data Integrity Violation");
    }

    @Test
    void handleOptimisticLockRetorna409() {
        ObjectOptimisticLockingFailureException ex =
                new ObjectOptimisticLockingFailureException("Reserva", 1L);

        ResponseEntity<ErrorResponse> response = handler.handleOptimisticLock(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getError()).isEqualTo("Concurrent Modification");
    }

    @Test
    void handleRuntimeExceptionRetorna500() {
        RuntimeException ex = new RuntimeException("Error inesperado");

        ResponseEntity<ErrorResponse> response = handler.handleRuntimeException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
    }

    @Test
    void responseIncludeTimestampYPath() {
        EntityNotFoundException ex = new EntityNotFoundException("Reserva", 1L);

        ResponseEntity<ErrorResponse> response = handler.handleEntityNotFoundException(ex, request);

        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/test");
    }
}
