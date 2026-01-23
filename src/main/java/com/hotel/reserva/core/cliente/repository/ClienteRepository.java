package com.hotel.reserva.core.cliente.repository;

import com.hotel.reserva.core.cliente.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByDni(String dni);

    Optional<Cliente> findByUserId(Long userId);

    Optional<Cliente> findByEmail(String email);
}
