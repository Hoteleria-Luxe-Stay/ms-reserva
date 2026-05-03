package com.hotel.reserva.core.habitacion_dia.service;

import com.hotel.reserva.core.habitacion_dia.model.HabitacionDia;
import com.hotel.reserva.core.habitacion_dia.repository.HabitacionDiaRepository;
import com.hotel.reserva.core.reserva.model.Reserva;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class HabitacionDiaService {

    private final HabitacionDiaRepository habitacionDiaRepository;

    public HabitacionDiaService(HabitacionDiaRepository habitacionDiaRepository) {
        this.habitacionDiaRepository = habitacionDiaRepository;
    }

    /**
     * Reserva slots por habitacion y fecha. Cada noche entre fechaInicio (inclusive)
     * y fechaFin (exclusive) genera una fila por habitacion. La UNIQUE constraint en
     * (habitacion_id, fecha) impide doble reserva: si dos transacciones intentan
     * insertar el mismo slot, la segunda recibe DataIntegrityViolationException.
     *
     * Se llama saveAllAndFlush para forzar el INSERT sincronico y asi capturar la
     * violacion de constraint dentro del @Transactional caller (no en el commit).
     *
     * Propagation MANDATORY: este metodo SIEMPRE debe ejecutarse dentro de una
     * transaccion ya iniciada por el caller (ReservaService.crearReserva).
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void reservarSlots(Reserva reserva, List<Long> habitacionesIds,
                              LocalDate fechaInicio, LocalDate fechaFin) {
        List<HabitacionDia> slots = new ArrayList<>();
        for (Long habitacionId : habitacionesIds) {
            LocalDate fecha = fechaInicio;
            while (fecha.isBefore(fechaFin)) {
                slots.add(new HabitacionDia(habitacionId, fecha, reserva));
                fecha = fecha.plusDays(1);
            }
        }
        habitacionDiaRepository.saveAllAndFlush(slots);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public int liberarSlots(Long reservaId) {
        return habitacionDiaRepository.deleteByReservaId(reservaId);
    }
}
