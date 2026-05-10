package com.hotel.reserva.infrastructure.jobs;

import com.hotel.reserva.core.reserva.model.EstadoReserva;
import com.hotel.reserva.core.reserva.model.Reserva;
import com.hotel.reserva.core.reserva.repository.ReservaRepository;
import com.hotel.reserva.core.reserva.service.ReservaSagaHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpirarReservasJobTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private ReservaSagaHandler sagaHandler;

    @InjectMocks
    private ExpirarReservasJob expirarReservasJob;

    @Test
    void runNoHaceNadaCuandoNoCandidatas() {
        when(reservaRepository.findExpiradas(any(), any(LocalDateTime.class))).thenReturn(List.of());

        expirarReservasJob.run();

        verify(sagaHandler, never()).expirarReservaPendiente(any());
    }

    @Test
    void runExpiraTodasLasCandidatas() {
        Reserva r1 = buildReserva(1L);
        Reserva r2 = buildReserva(2L);
        when(reservaRepository.findExpiradas(any(), any(LocalDateTime.class))).thenReturn(List.of(r1, r2));

        expirarReservasJob.run();

        verify(sagaHandler).expirarReservaPendiente(1L);
        verify(sagaHandler).expirarReservaPendiente(2L);
    }

    @Test
    void runContinuaConOtrasReservasCuandoUnaFalla() {
        Reserva r1 = buildReserva(1L);
        Reserva r2 = buildReserva(2L);
        when(reservaRepository.findExpiradas(any(), any(LocalDateTime.class))).thenReturn(List.of(r1, r2));
        doThrow(new RuntimeException("error al expirar"))
                .when(sagaHandler).expirarReservaPendiente(1L);

        // No debe propagar excepcion
        expirarReservasJob.run();

        verify(sagaHandler).expirarReservaPendiente(1L);
        verify(sagaHandler).expirarReservaPendiente(2L);
    }

    // ==================== helpers ====================

    private Reserva buildReserva(Long id) {
        Reserva r = new Reserva();
        r.setId(id);
        r.setEstado(EstadoReserva.PENDIENTE_PAGO);
        r.setFechaInicio(LocalDate.now().plusDays(1));
        r.setFechaFin(LocalDate.now().plusDays(3));
        r.setExpiresAt(LocalDateTime.now().minusMinutes(10));
        return r;
    }
}
