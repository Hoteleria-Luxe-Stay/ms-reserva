package com.hotel.reserva.core.detalle_reserva.repository;

import com.hotel.reserva.core.detalle_reserva.model.DetalleReserva;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleReservaRepository extends JpaRepository<DetalleReserva, Long> {
}
