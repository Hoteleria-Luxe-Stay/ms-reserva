package com.hotel.reserva.core.detalle_reserva.repository;

import com.hotel.reserva.core.detalle_reserva.model.DetalleReserva;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleReservaRepository extends JpaRepository<DetalleReserva, Long> {
    // existsReservaConflicto eliminado: la slot table 'habitacion_dia' con
    // UNIQUE(habitacion_id, fecha) es la fuente de verdad para disponibilidad.
    // El INSERT atomico garantiza la exclusion mutua (anti-TOCTOU) sin race conditions.
}
