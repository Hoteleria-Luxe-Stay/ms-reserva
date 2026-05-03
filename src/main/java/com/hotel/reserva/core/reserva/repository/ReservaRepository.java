package com.hotel.reserva.core.reserva.repository;

import com.hotel.reserva.core.reserva.model.EstadoReserva;
import com.hotel.reserva.core.reserva.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByClienteDni(String dni);

    List<Reserva> findByClienteUserId(Long userId);

    List<Reserva> findByEstado(EstadoReserva estado);

    @Query("""
            select r from Reserva r
            where r.cliente.userId = :userId
              and (:estado is null or r.estado = :estado)
              and (:fechaInicio is null or r.fechaInicio >= :fechaInicio)
              and (:fechaFin is null or r.fechaFin <= :fechaFin)
            """)
    List<Reserva> findByUserIdAndFilters(@Param("userId") Long userId,
                                         @Param("fechaInicio") LocalDate fechaInicio,
                                         @Param("fechaFin") LocalDate fechaFin,
                                         @Param("estado") EstadoReserva estado);

    /**
     * Reservas expiradas: estados "activos" cuyo expiresAt ya paso. Lo usa el job
     * de Ronda 5.3 (timeout PENDIENTE_PAGO 5 min).
     */
    @Query("""
            select r from Reserva r
            where r.estado in :estados
              and r.expiresAt is not null
              and r.expiresAt < :ahora
            """)
    List<Reserva> findExpiradas(@Param("estados") List<EstadoReserva> estados,
                                @Param("ahora") LocalDateTime ahora);
}
