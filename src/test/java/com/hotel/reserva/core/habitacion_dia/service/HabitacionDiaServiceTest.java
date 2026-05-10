package com.hotel.reserva.core.habitacion_dia.service;

import com.hotel.reserva.core.habitacion_dia.model.HabitacionDia;
import com.hotel.reserva.core.habitacion_dia.repository.HabitacionDiaRepository;
import com.hotel.reserva.core.reserva.model.EstadoReserva;
import com.hotel.reserva.core.reserva.model.Reserva;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HabitacionDiaServiceTest {

    @Mock private HabitacionDiaRepository habitacionDiaRepository;

    @InjectMocks
    private HabitacionDiaService habitacionDiaService;

    @Test
    void reservarSlotsCreaUnSlotPorNochePorHabitacion() {
        Reserva reserva = buildReserva(1L);
        List<Long> habitacionesIds = List.of(10L);
        LocalDate inicio = LocalDate.of(2026, 6, 1);
        LocalDate fin = LocalDate.of(2026, 6, 3); // 2 noches

        habitacionDiaService.reservarSlots(reserva, habitacionesIds, inicio, fin);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<HabitacionDia>> captor = ArgumentCaptor.forClass(List.class);
        verify(habitacionDiaRepository).saveAllAndFlush(captor.capture());

        List<HabitacionDia> slots = captor.getValue();
        assertThat(slots).hasSize(2); // 2 noches × 1 habitacion
        assertThat(slots.get(0).getHabitacionId()).isEqualTo(10L);
        assertThat(slots.get(0).getFecha()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(slots.get(1).getFecha()).isEqualTo(LocalDate.of(2026, 6, 2));
    }

    @Test
    void reservarSlotsConMultiplesHabitaciones() {
        Reserva reserva = buildReserva(1L);
        List<Long> habitacionesIds = List.of(10L, 20L);
        LocalDate inicio = LocalDate.of(2026, 6, 1);
        LocalDate fin = LocalDate.of(2026, 6, 2); // 1 noche

        habitacionDiaService.reservarSlots(reserva, habitacionesIds, inicio, fin);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<HabitacionDia>> captor = ArgumentCaptor.forClass(List.class);
        verify(habitacionDiaRepository).saveAllAndFlush(captor.capture());

        List<HabitacionDia> slots = captor.getValue();
        assertThat(slots).hasSize(2); // 1 noche × 2 habitaciones
    }

    @Test
    void liberarSlotsDelegaAlRepository() {
        when(habitacionDiaRepository.deleteByReservaId(1L)).thenReturn(3);

        int result = habitacionDiaService.liberarSlots(1L);

        assertThat(result).isEqualTo(3);
        verify(habitacionDiaRepository).deleteByReservaId(1L);
    }

    // ==================== helpers ====================

    private Reserva buildReserva(Long id) {
        Reserva r = new Reserva();
        r.setId(id);
        r.setEstado(EstadoReserva.PENDIENTE_PAGO);
        return r;
    }
}
