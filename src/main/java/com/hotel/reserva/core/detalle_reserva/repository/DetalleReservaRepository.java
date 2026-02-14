package com.hotel.reserva.core.detalle_reserva.repository;

import com.hotel.reserva.core.detalle_reserva.model.DetalleReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface DetalleReservaRepository extends JpaRepository<DetalleReserva, Long> {

    @Query("""
            select count(d) > 0 from DetalleReserva d
            where d.habitacionId = :habitacionId
              and d.reserva.estado in ('PENDIENTE', 'CONFIRMADA')
              and d.reserva.fechaInicio < :fechaFin
              and d.reserva.fechaFin > :fechaInicio
            """)
    boolean existsReservaConflicto(@Param("habitacionId") Long habitacionId,
                                   @Param("fechaInicio") LocalDate fechaInicio,
                                   @Param("fechaFin") LocalDate fechaFin);
}
