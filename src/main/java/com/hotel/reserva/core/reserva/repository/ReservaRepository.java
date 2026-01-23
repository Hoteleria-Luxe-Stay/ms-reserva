package com.hotel.reserva.core.reserva.repository;

import com.hotel.reserva.core.reserva.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByClienteDni(String dni);

    List<Reserva> findByClienteUserId(Long userId);

    List<Reserva> findByEstado(String estado);
}
