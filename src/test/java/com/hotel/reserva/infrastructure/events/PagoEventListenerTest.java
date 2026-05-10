package com.hotel.reserva.infrastructure.events;

import com.hotel.reserva.core.reserva.service.ReservaSagaHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoEventListenerTest {

    @Mock private ReservaSagaHandler sagaHandler;

    @InjectMocks
    private PagoEventListener pagoEventListener;

    @Test
    void onPagoEventLlamaPagoAprobadoCuandoEventTypePagoAprobado() {
        PagoEvent event = buildEvent("PagoAprobado", 1L);

        pagoEventListener.onPagoEvent(event);

        verify(sagaHandler).aplicarPagoAprobado(1L);
        verify(sagaHandler, never()).aplicarPagoRechazado(any(), any());
    }

    @Test
    void onPagoEventLlamaPagoRechazadoCuandoEventTypePagoRechazado() {
        PagoEvent event = buildEvent("PagoRechazado", 1L);
        event.setErrorMessage("Fondos insuficientes");

        pagoEventListener.onPagoEvent(event);

        verify(sagaHandler).aplicarPagoRechazado(1L, "Fondos insuficientes");
        verify(sagaHandler, never()).aplicarPagoAprobado(any());
    }

    @Test
    void onPagoEventIgnoraTipoDesconocido() {
        PagoEvent event = buildEvent("PagoExpirado", 1L);

        pagoEventListener.onPagoEvent(event);

        verify(sagaHandler, never()).aplicarPagoAprobado(any());
        verify(sagaHandler, never()).aplicarPagoRechazado(any(), any());
    }

    @Test
    void onPagoEventIgnoraEventoNulo() {
        pagoEventListener.onPagoEvent(null);

        verify(sagaHandler, never()).aplicarPagoAprobado(any());
        verify(sagaHandler, never()).aplicarPagoRechazado(any(), any());
    }

    @Test
    void onPagoEventIgnoraEventoSinEventType() {
        PagoEvent event = new PagoEvent();
        event.setReservaId(1L);
        // eventType es null

        pagoEventListener.onPagoEvent(event);

        verify(sagaHandler, never()).aplicarPagoAprobado(any());
    }

    @Test
    void onPagoEventIgnoraEventoSinReservaId() {
        PagoEvent event = new PagoEvent();
        event.setEventType("PagoAprobado");
        // reservaId es null

        pagoEventListener.onPagoEvent(event);

        verify(sagaHandler, never()).aplicarPagoAprobado(any());
    }

    // ==================== helpers ====================

    private PagoEvent buildEvent(String eventType, Long reservaId) {
        PagoEvent event = new PagoEvent();
        event.setEventType(eventType);
        event.setReservaId(reservaId);
        event.setPagoId(100L);
        return event;
    }
}
