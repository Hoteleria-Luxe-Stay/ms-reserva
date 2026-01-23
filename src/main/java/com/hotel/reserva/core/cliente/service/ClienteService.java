package com.hotel.reserva.core.cliente.service;

import com.hotel.reserva.api.dto.ClienteRequest;
import com.hotel.reserva.core.cliente.model.Cliente;
import com.hotel.reserva.core.cliente.repository.ClienteRepository;
import com.hotel.reserva.helpers.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<Cliente> listar() {
        return clienteRepository.findAll();
    }

    public Cliente guardar(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public Cliente buscarPorDni(String dni) {
        return clienteRepository.findByDni(dni)
                .orElseThrow(() -> new EntityNotFoundException("Cliente", dni));
    }

    public Optional<Cliente> buscarPorDniOptional(String dni) {
        return clienteRepository.findByDni(dni);
    }

    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente", id));
    }

    public Optional<Cliente> buscarPorUserId(Long userId) {
        return clienteRepository.findByUserId(userId);
    }

    public Optional<Cliente> buscarPorEmail(String email) {
        return clienteRepository.findByEmail(email);
    }

    public Cliente crearOActualizar(ClienteRequest dto, Long userId) {
        if (userId != null) {
            Optional<Cliente> clienteDelUsuario = clienteRepository.findByUserId(userId);

            if (clienteDelUsuario.isPresent()) {
                Cliente cliente = clienteDelUsuario.get();
                cliente.setNombre(dto.getNombre());
                cliente.setApellido(dto.getApellido());
                cliente.setDni(dto.getDni());
                cliente.setEmail(dto.getEmail());
                if (dto.getTelefono() != null && !dto.getTelefono().isBlank()) {
                    cliente.setTelefono(dto.getTelefono());
                }
                return clienteRepository.save(cliente);
            }

            Cliente nuevo = new Cliente();
            nuevo.setNombre(dto.getNombre());
            nuevo.setApellido(dto.getApellido());
            nuevo.setDni(dto.getDni());
            nuevo.setEmail(dto.getEmail());
            nuevo.setTelefono(dto.getTelefono() != null ? dto.getTelefono() : "");
            nuevo.setUserId(userId);
            return clienteRepository.save(nuevo);
        }

        Optional<Cliente> existente = clienteRepository.findByDni(dto.getDni());
        if (existente.isPresent()) {
            Cliente cliente = existente.get();
            cliente.setNombre(dto.getNombre());
            cliente.setApellido(dto.getApellido());
            cliente.setEmail(dto.getEmail());
            if (dto.getTelefono() != null && !dto.getTelefono().isBlank()) {
                cliente.setTelefono(dto.getTelefono());
            }
            return clienteRepository.save(cliente);
        }

        Cliente nuevo = new Cliente();
        nuevo.setNombre(dto.getNombre());
        nuevo.setApellido(dto.getApellido());
        nuevo.setDni(dto.getDni());
        nuevo.setEmail(dto.getEmail());
        nuevo.setTelefono(dto.getTelefono() != null ? dto.getTelefono() : "");
        return clienteRepository.save(nuevo);
    }
}
