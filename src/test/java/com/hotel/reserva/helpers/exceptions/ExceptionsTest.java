package com.hotel.reserva.helpers.exceptions;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionsTest {

    // ==================== EntityNotFoundException ====================

    @Test
    void entityNotFoundExceptionConEntityNameYId() {
        EntityNotFoundException ex = new EntityNotFoundException("Reserva", 1L);

        assertThat(ex.getEntityName()).isEqualTo("Reserva");
        assertThat(ex.getIdentifier()).isEqualTo(1L);
        assertThat(ex.getMessage()).contains("Reserva").contains("1");
    }

    @Test
    void entityNotFoundExceptionConStringIdentifier() {
        EntityNotFoundException ex = new EntityNotFoundException("Cliente", "12345678");

        assertThat(ex.getEntityName()).isEqualTo("Cliente");
        assertThat(ex.getIdentifier()).isEqualTo("12345678");
    }

    @Test
    void entityNotFoundExceptionConMensajeDirecto() {
        EntityNotFoundException ex = new EntityNotFoundException("Entidad no encontrada");

        assertThat(ex.getMessage()).isEqualTo("Entidad no encontrada");
        assertThat(ex.getEntityName()).isNull();
        assertThat(ex.getIdentifier()).isNull();
    }

    // ==================== BusinessException ====================

    @Test
    void businessExceptionTieneErrorCode() {
        BusinessException ex = new BusinessException("Error de negocio", "ESTADO_INVALIDO");

        assertThat(ex.getMessage()).isEqualTo("Error de negocio");
        assertThat(ex.getErrorCode()).isEqualTo("ESTADO_INVALIDO");
    }

    // ==================== ValidationException ====================

    @Test
    void validationExceptionConFieldYMensaje() {
        ValidationException ex = new ValidationException("nombre", "El nombre es requerido");

        assertThat(ex.getMessage()).isEqualTo("El nombre es requerido");
        assertThat(ex.getFieldErrors()).containsKey("nombre");
    }

    @Test
    void validationExceptionConSoloMensaje() {
        ValidationException ex = new ValidationException("Error de validacion");

        assertThat(ex.getMessage()).isEqualTo("Error de validacion");
        assertThat(ex.getFieldErrors()).isNull();
    }

    @Test
    void validationExceptionConMapaDeErrors() {
        Map<String, String> errors = Map.of("campo1", "error1", "campo2", "error2");
        ValidationException ex = new ValidationException("Multiples errores", errors);

        assertThat(ex.getFieldErrors()).containsKey("campo1");
        assertThat(ex.getFieldErrors()).containsKey("campo2");
    }

    // ==================== ConflictException ====================

    @Test
    void conflictExceptionTieneConflictType() {
        ConflictException ex = new ConflictException("Conflicto de datos", "HABITACION_OCUPADA");

        assertThat(ex.getMessage()).isEqualTo("Conflicto de datos");
        assertThat(ex.getConflictType()).isEqualTo("HABITACION_OCUPADA");
    }

    // ==================== UnauthorizedException ====================

    @Test
    void unauthorizedExceptionTieneMensaje() {
        UnauthorizedException ex = new UnauthorizedException("Sin permisos");

        assertThat(ex.getMessage()).isEqualTo("Sin permisos");
    }

    // ==================== ServiceUnavailableException ====================

    @Test
    void serviceUnavailableExceptionConNombreServicio() {
        ServiceUnavailableException ex = new ServiceUnavailableException("hotel-service");

        assertThat(ex.getServiceName()).isEqualTo("hotel-service");
        assertThat(ex.getMessage()).contains("hotel-service");
    }

    @Test
    void serviceUnavailableExceptionConCausa() {
        RuntimeException causa = new RuntimeException("connection refused");
        ServiceUnavailableException ex = new ServiceUnavailableException("pago-service", causa);

        assertThat(ex.getServiceName()).isEqualTo("pago-service");
        assertThat(ex.getCause()).isEqualTo(causa);
    }
}
