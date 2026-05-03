package com.hotel.reserva.core.habitacion_dia.repository;

import com.hotel.reserva.core.habitacion_dia.model.HabitacionDia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HabitacionDiaRepository extends JpaRepository<HabitacionDia, Long> {

    @Modifying
    @Query("delete from HabitacionDia h where h.reserva.id = :reservaId")
    int deleteByReservaId(@Param("reservaId") Long reservaId);
}
