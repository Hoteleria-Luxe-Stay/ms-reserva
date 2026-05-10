package com.hotel.reserva.core.reserva.service;

import com.hotel.reserva.core.cliente.model.Cliente;
import com.hotel.reserva.core.cliente.service.ClienteService;
import com.hotel.reserva.core.detalle_reserva.model.DetalleReserva;
import com.hotel.reserva.core.habitacion_dia.service.HabitacionDiaService;
import com.hotel.reserva.core.reserva.model.EstadoReserva;
import com.hotel.reserva.core.reserva.model.Reserva;
import com.hotel.reserva.core.reserva.repository.ReservaRepository;
import com.hotel.reserva.helpers.exceptions.BusinessException;
import com.hotel.reserva.helpers.exceptions.ConflictException;
import com.hotel.reserva.helpers.exceptions.EntityNotFoundException;
import com.hotel.reserva.helpers.exceptions.ValidationException;
import com.hotel.reserva.infrastructure.events.ReservaNotificationPublisher;
import com.hotel.reserva.internal.HotelInternalApi;
import com.hotel.reserva.internal.PagoInternalApi;
import com.hotel.reserva.internal.dto.CrearPagoInternalRequest;
import com.hotel.reserva.internal.dto.CrearPagoInternalResponse;
import com.hotel.reserva.internal.dto.HabitacionInternalResponse;
import com.hotel.reserva.internal.dto.HotelInternalResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private ClienteService clienteService;
    @Mock private HotelInternalApi hotelInternalApi;
    @Mock private HabitacionDiaService habitacionDiaService;
    @Mock private ReservaNotificationPublisher reservaNotificationPublisher;
    @Mock private PagoInternalApi pagoInternalApi;
    @Mock private PlatformTransactionManager transactionManager;

    @InjectMocks
    private ReservaService reservaService;

    private HotelInternalResponse hotel;
    private HabitacionInternalResponse habitacion;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        // Inyectar @Value fields
        ReflectionTestUtils.setField(reservaService, "defaultCurrency", "USD");
        ReflectionTestUtils.setField(reservaService, "pagoSuccessUrl", "http://localhost:4200/reservas/success");
        ReflectionTestUtils.setField(reservaService, "pagoCancelUrl", "http://localhost:4200/reservas/cancel");

        // Reemplazar TransactionTemplate con uno que ejecute el callback directamente
        TransactionTemplate realTx = new TransactionTemplate(transactionManager);
        ReflectionTestUtils.setField(reservaService, "transactionTemplate", realTx);

        hotel = new HotelInternalResponse();
        hotel.setId(1L);
        hotel.setNombre("Hotel Luxe");
        hotel.setDireccion("Calle 123");

        habitacion = new HabitacionInternalResponse();
        habitacion.setId(10L);
        habitacion.setHotelId(1L);
        habitacion.setPrecio(100.0);

        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Juan");
        cliente.setApellido("Perez");
        cliente.setEmail("juan@test.com");
        cliente.setDni("12345678");
        cliente.setUserId(42L);
    }

    // ==================== listarReservas ====================

    @Test
    void listarReservasPorDniCuandoDniNoNulo() {
        Reserva r = buildReserva(1L, EstadoReserva.CONFIRMADA);
        when(reservaRepository.findByClienteDni("12345678")).thenReturn(List.of(r));

        List<Reserva> result = reservaService.listarReservas("12345678", null);

        assertThat(result).hasSize(1);
        verify(reservaRepository).findByClienteDni("12345678");
    }

    @Test
    void listarReservasPorEstadoCuandoDniNulo() {
        Reserva r = buildReserva(1L, EstadoReserva.CONFIRMADA);
        when(reservaRepository.findByEstado(EstadoReserva.CONFIRMADA)).thenReturn(List.of(r));

        List<Reserva> result = reservaService.listarReservas(null, "CONFIRMADA");

        assertThat(result).hasSize(1);
        verify(reservaRepository).findByEstado(EstadoReserva.CONFIRMADA);
    }

    @Test
    void listarReservasRetornaTodasCuandoDniYEstadoNulos() {
        when(reservaRepository.findAll()).thenReturn(List.of(buildReserva(1L, EstadoReserva.PENDIENTE_PAGO)));

        List<Reserva> result = reservaService.listarReservas(null, null);

        assertThat(result).hasSize(1);
        verify(reservaRepository).findAll();
    }

    @Test
    void listarReservasPorDniBlankUsaEstado() {
        when(reservaRepository.findAll()).thenReturn(List.of());

        reservaService.listarReservas("   ", null);

        verify(reservaRepository, never()).findByClienteDni(any());
        verify(reservaRepository).findAll();
    }

    // ==================== buscarPorId ====================

    @Test
    void buscarPorIdRetornaReservaCuandoExiste() {
        Reserva r = buildReserva(1L, EstadoReserva.CONFIRMADA);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(r));

        Reserva result = reservaService.buscarPorId(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void buscarPorIdLanzaEntityNotFoundCuandoNoExiste() {
        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservaService.buscarPorId(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ==================== buscarReservasPorUsuarioId ====================

    @Test
    void buscarReservasPorUsuarioIdDelegaAlRepository() {
        when(reservaRepository.findByClienteUserId(42L)).thenReturn(List.of());

        List<Reserva> result = reservaService.buscarReservasPorUsuarioId(42L);

        assertThat(result).isEmpty();
        verify(reservaRepository).findByClienteUserId(42L);
    }

    @Test
    void buscarReservasPorUsuarioIdYFechasDelegaAlRepository() {
        LocalDate inicio = LocalDate.now().plusDays(2);
        LocalDate fin = LocalDate.now().plusDays(4);
        when(reservaRepository.findByUserIdAndFilters(42L, inicio, fin, null)).thenReturn(List.of());

        reservaService.buscarReservasPorUsuarioIdYFechas(42L, inicio, fin, null);

        verify(reservaRepository).findByUserIdAndFilters(42L, inicio, fin, null);
    }

    // ==================== crearReserva ====================

    @Test
    void crearReservaExitosoRetornaReservaGuardada() {
        com.hotel.reserva.api.dto.ReservaRequest request = buildReservaRequest();

        when(hotelInternalApi.getHotelById(1L)).thenReturn(Optional.of(hotel));
        when(hotelInternalApi.getHabitacionById(10L)).thenReturn(Optional.of(habitacion));
        when(hotelInternalApi.checkDisponibilidad(10L)).thenReturn(true);
        when(clienteService.crearOActualizar(any(), eq(42L))).thenReturn(cliente);

        Reserva savedReserva = buildReserva(1L, EstadoReserva.PENDIENTE_PAGO);
        savedReserva.setCliente(cliente);
        savedReserva.setHotelId(1L);
        savedReserva.setTotal(200.0);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(savedReserva);

        Reserva result = reservaService.crearReserva(request, 42L);

        assertThat(result.getId()).isEqualTo(1L);
        verify(habitacionDiaService).reservarSlots(any(), any(), any(), any());
        verify(reservaNotificationPublisher).publish(any());
    }

    @Test
    void crearReservaLanzaValidationExceptionCuandoHotelIdNulo() {
        com.hotel.reserva.api.dto.ReservaRequest request = buildReservaRequest();
        request.setHotelId(null);

        assertThatThrownBy(() -> reservaService.crearReserva(request, 42L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("hotel");
    }

    @Test
    void crearReservaLanzaValidationExceptionCuandoFechaInicioNula() {
        com.hotel.reserva.api.dto.ReservaRequest request = buildReservaRequest();
        request.setFechaInicio(null);

        assertThatThrownBy(() -> reservaService.crearReserva(request, 42L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("inicio es requerida");
    }

    @Test
    void crearReservaLanzaValidationExceptionCuandoFechaFinNula() {
        com.hotel.reserva.api.dto.ReservaRequest request = buildReservaRequest();
        request.setFechaFin(null);

        assertThatThrownBy(() -> reservaService.crearReserva(request, 42L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("fin es requerida");
    }

    @Test
    void crearReservaLanzaValidationExceptionCuandoHabitacionesVacías() {
        com.hotel.reserva.api.dto.ReservaRequest request = buildReservaRequest();
        request.setHabitacionesIds(List.of());

        assertThatThrownBy(() -> reservaService.crearReserva(request, 42L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("habitacion");
    }

    @Test
    void crearReservaLanzaValidationExceptionCuandoClienteNulo() {
        com.hotel.reserva.api.dto.ReservaRequest request = buildReservaRequest();
        request.setCliente(null);

        assertThatThrownBy(() -> reservaService.crearReserva(request, 42L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("cliente");
    }

    @Test
    void crearReservaLanzaBusinessExceptionCuandoFechaFinIgualAInicio() {
        com.hotel.reserva.api.dto.ReservaRequest request = buildReservaRequest();
        LocalDate fecha = LocalDate.now().plusDays(2);
        request.setFechaInicio(fecha);
        request.setFechaFin(fecha); // igual, invalido

        assertThatThrownBy(() -> reservaService.crearReserva(request, 42L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("fecha de salida debe ser posterior");
    }

    @Test
    void crearReservaLanzaEntityNotFoundCuandoHotelNoExiste() {
        com.hotel.reserva.api.dto.ReservaRequest request = buildReservaRequest();
        when(hotelInternalApi.getHotelById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservaService.crearReserva(request, 42L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Hotel");
    }

    @Test
    void crearReservaLanzaEntityNotFoundCuandoHabitacionNoExiste() {
        com.hotel.reserva.api.dto.ReservaRequest request = buildReservaRequest();
        when(hotelInternalApi.getHotelById(1L)).thenReturn(Optional.of(hotel));
        when(hotelInternalApi.getHabitacionById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservaService.crearReserva(request, 42L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Habitacion");
    }

    @Test
    void crearReservaLanzaBusinessExceptionCuandoHabitacionNoPertenecealHotel() {
        com.hotel.reserva.api.dto.ReservaRequest request = buildReservaRequest();
        HabitacionInternalResponse habitacionOtroHotel = new HabitacionInternalResponse();
        habitacionOtroHotel.setId(10L);
        habitacionOtroHotel.setHotelId(99L); // diferente hotel
        habitacionOtroHotel.setPrecio(100.0);

        when(hotelInternalApi.getHotelById(1L)).thenReturn(Optional.of(hotel));
        when(hotelInternalApi.getHabitacionById(10L)).thenReturn(Optional.of(habitacionOtroHotel));

        assertThatThrownBy(() -> reservaService.crearReserva(request, 42L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no pertenece al hotel");
    }

    @Test
    void crearReservaLanzaConflictExceptionCuandoHabitacionNoDisponible() {
        com.hotel.reserva.api.dto.ReservaRequest request = buildReservaRequest();
        when(hotelInternalApi.getHotelById(1L)).thenReturn(Optional.of(hotel));
        when(hotelInternalApi.getHabitacionById(10L)).thenReturn(Optional.of(habitacion));
        when(hotelInternalApi.checkDisponibilidad(10L)).thenReturn(false);

        assertThatThrownBy(() -> reservaService.crearReserva(request, 42L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("no esta disponible");
    }

    @Test
    void crearReservaLanzaConflictExceptionCuandoDataIntegrityViolation() {
        com.hotel.reserva.api.dto.ReservaRequest request = buildReservaRequest();
        when(hotelInternalApi.getHotelById(1L)).thenReturn(Optional.of(hotel));
        when(hotelInternalApi.getHabitacionById(10L)).thenReturn(Optional.of(habitacion));
        when(hotelInternalApi.checkDisponibilidad(10L)).thenReturn(true);
        when(clienteService.crearOActualizar(any(), eq(42L))).thenReturn(cliente);

        Reserva savedReserva = buildReserva(1L, EstadoReserva.PENDIENTE_PAGO);
        savedReserva.setCliente(cliente);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(savedReserva);
        doThrow(new DataIntegrityViolationException("duplicate"))
                .when(habitacionDiaService).reservarSlots(any(), any(), any(), any());

        assertThatThrownBy(() -> reservaService.crearReserva(request, 42L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("ya estan reservadas");
    }

    // ==================== cancelarReserva ====================

    @Test
    void cancelarReservaExitosoTransicionaACancelada() {
        Reserva reserva = buildReserva(1L, EstadoReserva.CONFIRMADA);
        reserva.setCliente(cliente);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any())).thenReturn(reserva);

        reservaService.cancelarReserva(1L);

        assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.CANCELADA);
        assertThat(reserva.getFechaCancelacion()).isNotNull();
        verify(habitacionDiaService).liberarSlots(1L);
        verify(reservaNotificationPublisher).publish(any());
    }

    @Test
    void cancelarReservaLanzaBusinessExceptionCuandoEstadoTerminal() {
        Reserva reserva = buildReserva(1L, EstadoReserva.PAGO_FALLIDO);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> reservaService.cancelarReserva(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No se puede cancelar");
    }

    // ==================== confirmar ====================

    @Test
    void confirmarExitosoTransicionaAConfirmada() {
        Reserva reserva = buildReserva(1L, EstadoReserva.PAGO_EN_PROCESO);
        reserva.setCliente(cliente);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any())).thenReturn(reserva);

        Reserva result = reservaService.confirmar(1L);

        assertThat(result.getEstado()).isEqualTo(EstadoReserva.CONFIRMADA);
        assertThat(result.getExpiresAt()).isNull();
        verify(reservaNotificationPublisher).publish(any());
    }

    @Test
    void confirmarLanzaBusinessExceptionCuandoEstadoInvalido() {
        Reserva reserva = buildReserva(1L, EstadoReserva.PENDIENTE_PAGO);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> reservaService.confirmar(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No se puede confirmar");
    }

    // ==================== eliminar ====================

    @Test
    void eliminarEliminaReservaExistente() {
        when(reservaRepository.existsById(1L)).thenReturn(true);

        reservaService.eliminar(1L);

        verify(reservaRepository).deleteById(1L);
    }

    @Test
    void eliminarLanzaEntityNotFoundCuandoNoExiste() {
        when(reservaRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> reservaService.eliminar(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ==================== actualizarFechas ====================

    @Test
    void actualizarFechasActualizaYRecalculaTotal() {
        Reserva reserva = buildReserva(1L, EstadoReserva.PENDIENTE_PAGO);
        DetalleReserva detalle = new DetalleReserva();
        detalle.setPrecioNoche(100.0);
        reserva.getDetalles().add(detalle);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        com.hotel.reserva.api.dto.ReservaUpdateRequest req = new com.hotel.reserva.api.dto.ReservaUpdateRequest();
        req.setFechaInicio(LocalDate.now().plusDays(2));
        req.setFechaFin(LocalDate.now().plusDays(4));

        Reserva result = reservaService.actualizarFechas(1L, req);

        assertThat(result.getTotal()).isEqualTo(200.0); // 100 * 2 noches
    }

    @Test
    void actualizarFechasLanzaBusinessExceptionCuandoFechaInicioEnPasado() {
        Reserva reserva = buildReserva(1L, EstadoReserva.PENDIENTE_PAGO);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        com.hotel.reserva.api.dto.ReservaUpdateRequest req = new com.hotel.reserva.api.dto.ReservaUpdateRequest();
        req.setFechaInicio(LocalDate.now()); // hoy no es valido (debe ser al menos manana)
        req.setFechaFin(LocalDate.now().plusDays(2));

        assertThatThrownBy(() -> reservaService.actualizarFechas(1L, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("24 horas");
    }

    // ==================== iniciarPago ====================

    @Test
    void iniciarPagoExitosoRetornaCheckoutUrl() {
        Reserva reserva = buildReserva(1L, EstadoReserva.PENDIENTE_PAGO);
        reserva.setCliente(cliente);
        reserva.setTotal(200.0);
        reserva.setHotelNombre("Hotel Luxe");

        org.springframework.transaction.TransactionStatus txStatus =
                org.mockito.Mockito.mock(org.springframework.transaction.TransactionStatus.class);
        when(transactionManager.getTransaction(any())).thenReturn(txStatus);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any())).thenReturn(reserva);

        CrearPagoInternalResponse pagoResponse = new CrearPagoInternalResponse();
        pagoResponse.setCheckoutUrl("https://mp.com/checkout/123");
        when(pagoInternalApi.crearPago(any(CrearPagoInternalRequest.class))).thenReturn(pagoResponse);

        CrearPagoInternalResponse result = reservaService.iniciarPago(1L);

        assertThat(result.getCheckoutUrl()).isEqualTo("https://mp.com/checkout/123");
    }

    @Test
    void iniciarPagoRevierteAPendienteCuandoPagoFalla() {
        Reserva reserva = buildReserva(1L, EstadoReserva.PENDIENTE_PAGO);
        reserva.setCliente(cliente);
        reserva.setTotal(200.0);
        reserva.setHotelNombre("Hotel Luxe");

        org.springframework.transaction.TransactionStatus txStatus =
                org.mockito.Mockito.mock(org.springframework.transaction.TransactionStatus.class);
        when(transactionManager.getTransaction(any())).thenReturn(txStatus);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any())).thenReturn(reserva);
        when(pagoInternalApi.crearPago(any())).thenThrow(new RuntimeException("pago-service down"));

        assertThatThrownBy(() -> reservaService.iniciarPago(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("pago-service down");

        // Se llamo a findById varias veces: una para obtener la reserva, otra para revert
        verify(reservaRepository, atLeastOnce()).findById(1L);
    }

    @Test
    void iniciarPagoLanzaBusinessExceptionCuandoEstadoNoPermiteIniciar() {
        Reserva reserva = buildReserva(1L, EstadoReserva.CONFIRMADA);
        reserva.setCliente(cliente);

        org.springframework.transaction.TransactionStatus txStatus =
                org.mockito.Mockito.mock(org.springframework.transaction.TransactionStatus.class);
        when(transactionManager.getTransaction(any())).thenReturn(txStatus);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> reservaService.iniciarPago(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No se puede iniciar pago");
    }

    // ==================== helpers privados ====================

    private Reserva buildReserva(Long id, EstadoReserva estado) {
        Reserva r = new Reserva();
        r.setId(id);
        r.setEstado(estado);
        r.setFechaInicio(LocalDate.now().plusDays(2));
        r.setFechaFin(LocalDate.now().plusDays(4));
        r.setTotal(200.0);
        return r;
    }

    private com.hotel.reserva.api.dto.ReservaRequest buildReservaRequest() {
        com.hotel.reserva.api.dto.ReservaRequest req = new com.hotel.reserva.api.dto.ReservaRequest();
        req.setHotelId(1L);
        req.setFechaInicio(LocalDate.now().plusDays(2));
        req.setFechaFin(LocalDate.now().plusDays(4));
        req.setHabitacionesIds(List.of(10L));
        com.hotel.reserva.api.dto.ClienteRequest clienteReq = new com.hotel.reserva.api.dto.ClienteRequest();
        clienteReq.setNombre("Juan");
        clienteReq.setApellido("Perez");
        clienteReq.setDni("12345678");
        clienteReq.setEmail("juan@test.com");
        req.setCliente(clienteReq);
        return req;
    }
}
