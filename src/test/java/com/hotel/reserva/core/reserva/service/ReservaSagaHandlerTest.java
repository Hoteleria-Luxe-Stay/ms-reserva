package com.hotel.reserva.core.reserva.service;

import com.hotel.reserva.core.cliente.model.Cliente;
import com.hotel.reserva.core.habitacion_dia.service.HabitacionDiaService;
import com.hotel.reserva.core.reserva.model.EstadoReserva;
import com.hotel.reserva.core.reserva.model.Reserva;
import com.hotel.reserva.core.reserva.repository.ReservaRepository;
import com.hotel.reserva.infrastructure.events.ReservaNotificationPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaSagaHandlerTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private HabitacionDiaService habitacionDiaService;
    @Mock private ReservaNotificationPublisher reservaNotificationPublisher;

    @InjectMocks
    private ReservaSagaHandler sagaHandler;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Juan");
        cliente.setApellido("Perez");
        cliente.setEmail("juan@test.com");
        cliente.setUserId(42L);
    }

    // ==================== aplicarPagoAprobado ====================

    @Test
    void aplicarPagoAprobadoConfirmaReservaEnPagoEnProceso() {
        Reserva reserva = buildReserva(1L, EstadoReserva.PAGO_EN_PROCESO);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any())).thenReturn(reserva);

        sagaHandler.aplicarPagoAprobado(1L);

        assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.CONFIRMADA);
        assertThat(reserva.getExpiresAt()).isNull();
        verify(reservaRepository).save(reserva);
        verify(reservaNotificationPublisher).publish(any());
    }

    @Test
    void aplicarPagoAprobadoIdempotenteCuandoYaConfirmada() {
        Reserva reserva = buildReserva(1L, EstadoReserva.CONFIRMADA);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        sagaHandler.aplicarPagoAprobado(1L);

        verify(reservaRepository, never()).save(any());
        verify(reservaNotificationPublisher, never()).publish(any());
    }

    @Test
    void aplicarPagoAprobadoIgnoradoCuandoReservaInexistente() {
        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        sagaHandler.aplicarPagoAprobado(99L);

        verify(reservaRepository, never()).save(any());
    }

    @Test
    void aplicarPagoAprobadoIgnoradoCuandoEstadoTerminalDistintoDeConfirmada() {
        Reserva reserva = buildReserva(1L, EstadoReserva.CANCELADA);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        sagaHandler.aplicarPagoAprobado(1L);

        verify(reservaRepository, never()).save(any());
    }

    @Test
    void aplicarPagoAprobadoCuandoTransicionFallida() {
        // PENDIENTE_PAGO no puede transicionar a CONFIRMADA directamente
        Reserva reserva = buildReserva(1L, EstadoReserva.PENDIENTE_PAGO);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        sagaHandler.aplicarPagoAprobado(1L);

        // No debe lanzar excepcion, solo logea el error
        verify(reservaRepository, never()).save(any());
    }

    // ==================== aplicarPagoRechazado ====================

    @Test
    void aplicarPagoRechazadoMarcaPagoFallidoYLiberaSlots() {
        Reserva reserva = buildReserva(1L, EstadoReserva.PAGO_EN_PROCESO);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any())).thenReturn(reserva);

        sagaHandler.aplicarPagoRechazado(1L, "Fondos insuficientes");

        assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.PAGO_FALLIDO);
        assertThat(reserva.getMotivoCancelacion()).isEqualTo("Fondos insuficientes");
        assertThat(reserva.getExpiresAt()).isNull();
        verify(habitacionDiaService).liberarSlots(1L);
        verify(reservaNotificationPublisher).publish(any());
    }

    @Test
    void aplicarPagoRechazadoIdempotenteCuandoYaEsPagoFallido() {
        Reserva reserva = buildReserva(1L, EstadoReserva.PAGO_FALLIDO);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        sagaHandler.aplicarPagoRechazado(1L, "error");

        verify(reservaRepository, never()).save(any());
        verify(habitacionDiaService, never()).liberarSlots(anyLong());
    }

    @Test
    void aplicarPagoRechazadoIgnoradoCuandoReservaInexistente() {
        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        sagaHandler.aplicarPagoRechazado(99L, "error");

        verify(reservaRepository, never()).save(any());
    }

    @Test
    void aplicarPagoRechazadoConErrorMessageNuloNoSetMotivoNulo() {
        Reserva reserva = buildReserva(1L, EstadoReserva.PAGO_EN_PROCESO);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any())).thenReturn(reserva);

        sagaHandler.aplicarPagoRechazado(1L, null);

        assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.PAGO_FALLIDO);
        assertThat(reserva.getMotivoCancelacion()).isNull();
    }

    @Test
    void aplicarPagoRechazadoIgnoradoCuandoEstadoTerminal() {
        Reserva reserva = buildReserva(1L, EstadoReserva.CANCELADA);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        sagaHandler.aplicarPagoRechazado(1L, "error");

        verify(reservaRepository, never()).save(any());
    }

    // ==================== expirarReservaPendiente ====================

    @Test
    void expirarReservaPendienteExitoso() {
        Reserva reserva = buildReserva(1L, EstadoReserva.PENDIENTE_PAGO);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any())).thenReturn(reserva);

        sagaHandler.expirarReservaPendiente(1L);

        assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.EXPIRADA);
        assertThat(reserva.getExpiresAt()).isNull();
        verify(habitacionDiaService).liberarSlots(1L);
        verify(reservaNotificationPublisher).publish(any());
    }

    @Test
    void expirarReservaPendienteIgnoradaCuandoReservaInexistente() {
        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        sagaHandler.expirarReservaPendiente(99L);

        verify(reservaRepository, never()).save(any());
    }

    @Test
    void expirarReservaPendienteIgnoradaCuandoEstadoNoEsPendiente() {
        Reserva reserva = buildReserva(1L, EstadoReserva.PAGO_EN_PROCESO);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        sagaHandler.expirarReservaPendiente(1L);

        verify(reservaRepository, never()).save(any());
        verify(habitacionDiaService, never()).liberarSlots(anyLong());
    }

    // ==================== helpers ====================

    private Reserva buildReserva(Long id, EstadoReserva estado) {
        Reserva r = new Reserva();
        r.setId(id);
        r.setEstado(estado);
        r.setCliente(cliente);
        r.setFechaInicio(LocalDate.now().plusDays(2));
        r.setFechaFin(LocalDate.now().plusDays(4));
        r.setTotal(200.0);
        r.setHotelNombre("Hotel Luxe");
        r.setHotelDireccion("Calle 123");
        return r;
    }
}
