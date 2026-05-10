package com.hotel.reserva.core.cliente.service;

import com.hotel.reserva.core.cliente.model.Cliente;
import com.hotel.reserva.core.cliente.repository.ClienteRepository;
import com.hotel.reserva.helpers.exceptions.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Juan");
        cliente.setApellido("Perez");
        cliente.setDni("12345678");
        cliente.setEmail("juan@test.com");
        cliente.setTelefono("1234567890");
        cliente.setUserId(42L);
    }

    // ==================== listar ====================

    @Test
    void listarRetornaTodosLosClientes() {
        when(clienteRepository.findAll()).thenReturn(List.of(cliente));

        List<Cliente> result = clienteService.listar();

        assertThat(result).hasSize(1);
    }

    // ==================== guardar ====================

    @Test
    void guardarDelegaAlRepository() {
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        Cliente result = clienteService.guardar(cliente);

        assertThat(result).isEqualTo(cliente);
        verify(clienteRepository).save(cliente);
    }

    // ==================== buscarPorDni ====================

    @Test
    void buscarPorDniRetornaClienteCuandoExiste() {
        when(clienteRepository.findByDni("12345678")).thenReturn(Optional.of(cliente));

        Cliente result = clienteService.buscarPorDni("12345678");

        assertThat(result.getDni()).isEqualTo("12345678");
    }

    @Test
    void buscarPorDniLanzaEntityNotFoundCuandoNoExiste() {
        when(clienteRepository.findByDni("99999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.buscarPorDni("99999999"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ==================== buscarPorDniOptional ====================

    @Test
    void buscarPorDniOptionalRetornaOptional() {
        when(clienteRepository.findByDni("12345678")).thenReturn(Optional.of(cliente));

        Optional<Cliente> result = clienteService.buscarPorDniOptional("12345678");

        assertThat(result).isPresent();
    }

    // ==================== buscarPorId ====================

    @Test
    void buscarPorIdRetornaClienteCuandoExiste() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        Cliente result = clienteService.buscarPorId(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void buscarPorIdLanzaEntityNotFoundCuandoNoExiste() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.buscarPorId(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ==================== buscarPorUserId ====================

    @Test
    void buscarPorUserIdRetornaOptional() {
        when(clienteRepository.findByUserId(42L)).thenReturn(Optional.of(cliente));

        Optional<Cliente> result = clienteService.buscarPorUserId(42L);

        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(42L);
    }

    // ==================== buscarPorEmail ====================

    @Test
    void buscarPorEmailRetornaOptional() {
        when(clienteRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(cliente));

        Optional<Cliente> result = clienteService.buscarPorEmail("juan@test.com");

        assertThat(result).isPresent();
    }

    // ==================== crearOActualizar — con userId ====================

    @Test
    void crearOActualizarActualizaClienteExistenteConUserId() {
        when(clienteRepository.findByUserId(42L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any())).thenReturn(cliente);

        com.hotel.reserva.api.dto.ClienteRequest req = buildClienteRequest("Maria", "Lopez", "12345678",
                "maria@test.com", "9876543210");

        Cliente result = clienteService.crearOActualizar(req, 42L);

        assertThat(result.getNombre()).isEqualTo("Maria");
        assertThat(result.getApellido()).isEqualTo("Lopez");
        verify(clienteRepository).save(cliente);
    }

    @Test
    void crearOActualizarActualizaTelefonoCuandoNoBlank() {
        when(clienteRepository.findByUserId(42L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any())).thenReturn(cliente);

        com.hotel.reserva.api.dto.ClienteRequest req = buildClienteRequest("Juan", "Perez", "12345678",
                "juan@test.com", "5555555");

        clienteService.crearOActualizar(req, 42L);

        assertThat(cliente.getTelefono()).isEqualTo("5555555");
    }

    @Test
    void crearOActualizarNoActualizaTelefonoCuandoBlank() {
        when(clienteRepository.findByUserId(42L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any())).thenReturn(cliente);

        com.hotel.reserva.api.dto.ClienteRequest req = buildClienteRequest("Juan", "Perez", "12345678",
                "juan@test.com", "");

        clienteService.crearOActualizar(req, 42L);

        assertThat(cliente.getTelefono()).isEqualTo("1234567890"); // no cambio
    }

    @Test
    void crearOActualizarCreaClienteNuevoConUserId() {
        when(clienteRepository.findByUserId(42L)).thenReturn(Optional.empty());
        when(clienteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        com.hotel.reserva.api.dto.ClienteRequest req = buildClienteRequest("Nuevo", "Cliente", "99999999",
                "nuevo@test.com", "0000000");

        Cliente result = clienteService.crearOActualizar(req, 42L);

        assertThat(result.getNombre()).isEqualTo("Nuevo");
        assertThat(result.getUserId()).isEqualTo(42L);
    }

    @Test
    void crearOActualizarCreaClienteNuevoConUserIdTelefonoNulo() {
        when(clienteRepository.findByUserId(42L)).thenReturn(Optional.empty());
        when(clienteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        com.hotel.reserva.api.dto.ClienteRequest req = buildClienteRequest("Nuevo", "Cliente", "99999999",
                "nuevo@test.com", null);

        Cliente result = clienteService.crearOActualizar(req, 42L);

        assertThat(result.getTelefono()).isEqualTo("");
    }

    // ==================== crearOActualizar — sin userId ====================

    @Test
    void crearOActualizarActualizaClienteExistentePorDniSinUserId() {
        when(clienteRepository.findByDni("12345678")).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any())).thenReturn(cliente);

        com.hotel.reserva.api.dto.ClienteRequest req = buildClienteRequest("Maria", "Lopez", "12345678",
                "maria@test.com", "5555");

        Cliente result = clienteService.crearOActualizar(req, null);

        assertThat(result.getNombre()).isEqualTo("Maria");
    }

    @Test
    void crearOActualizarNoActualizaTelefonoBlankSinUserId() {
        when(clienteRepository.findByDni("12345678")).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any())).thenReturn(cliente);

        com.hotel.reserva.api.dto.ClienteRequest req = buildClienteRequest("Maria", "Lopez", "12345678",
                "maria@test.com", "   ");

        clienteService.crearOActualizar(req, null);

        assertThat(cliente.getTelefono()).isEqualTo("1234567890"); // no cambio
    }

    @Test
    void crearOActualizarCreaClienteNuevoSinUserId() {
        when(clienteRepository.findByDni("88888888")).thenReturn(Optional.empty());
        when(clienteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        com.hotel.reserva.api.dto.ClienteRequest req = buildClienteRequest("Nuevo", "Cliente", "88888888",
                "nuevo@test.com", "");

        Cliente result = clienteService.crearOActualizar(req, null);

        assertThat(result.getNombre()).isEqualTo("Nuevo");
        assertThat(result.getTelefono()).isEqualTo("");
    }

    // ==================== helpers ====================

    private com.hotel.reserva.api.dto.ClienteRequest buildClienteRequest(
            String nombre, String apellido, String dni, String email, String telefono) {
        com.hotel.reserva.api.dto.ClienteRequest req = new com.hotel.reserva.api.dto.ClienteRequest();
        req.setNombre(nombre);
        req.setApellido(apellido);
        req.setDni(dni);
        req.setEmail(email);
        req.setTelefono(telefono);
        return req;
    }
}
