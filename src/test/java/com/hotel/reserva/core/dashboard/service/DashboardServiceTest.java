package com.hotel.reserva.core.dashboard.service;

import com.hotel.reserva.api.dto.DashboardStatsResponse;
import com.hotel.reserva.core.cliente.model.Cliente;
import com.hotel.reserva.core.reserva.model.EstadoReserva;
import com.hotel.reserva.core.reserva.model.Reserva;
import com.hotel.reserva.core.reserva.repository.ReservaRepository;
import com.hotel.reserva.internal.HotelInternalApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private HotelInternalApi hotelInternalApi;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void obtenerEstadisticasRetornaStatsCompletas() {
        Reserva r1 = buildReserva(1L, EstadoReserva.CONFIRMADA, "Hotel A", 500.0);
        Reserva r2 = buildReserva(2L, EstadoReserva.PENDIENTE_PAGO, "Hotel B", 200.0);

        when(reservaRepository.findAll()).thenReturn(List.of(r1, r2));
        when(hotelInternalApi.getTotalDepartamentos()).thenReturn(3);
        when(hotelInternalApi.getTotalHoteles()).thenReturn(10);
        when(hotelInternalApi.getTotalHabitaciones()).thenReturn(50);
        when(hotelInternalApi.getHotelesPorDepartamentoReal()).thenReturn(Map.of("Montevideo", 5L));

        DashboardStatsResponse result = dashboardService.obtenerEstadisticas();

        assertThat(result).isNotNull();
        assertThat(result.getTotalDepartamentos()).isEqualTo(3);
        assertThat(result.getTotalHoteles()).isEqualTo(10);
        assertThat(result.getTotalHabitaciones()).isEqualTo(50L);
        assertThat(result.getTotalReservas()).isEqualTo(2);
        assertThat(result.getIngresosTotales()).isEqualTo(500.0); // solo CONFIRMADA
    }

    @Test
    void obtenerEstadisticasConListaVacia() {
        when(reservaRepository.findAll()).thenReturn(Collections.emptyList());
        when(hotelInternalApi.getTotalDepartamentos()).thenReturn(0);
        when(hotelInternalApi.getTotalHoteles()).thenReturn(0);
        when(hotelInternalApi.getTotalHabitaciones()).thenReturn(0);
        when(hotelInternalApi.getHotelesPorDepartamentoReal()).thenReturn(Map.of());

        DashboardStatsResponse result = dashboardService.obtenerEstadisticas();

        assertThat(result.getTotalReservas()).isEqualTo(0);
        assertThat(result.getIngresosTotales()).isEqualTo(0.0);
        assertThat(result.getTopHoteles()).isEmpty();
        assertThat(result.getReservasRecientes()).isEmpty();
    }

    @Test
    void obtenerEstadisticasCalculaReservasPorEstado() {
        Reserva r1 = buildReserva(1L, EstadoReserva.CONFIRMADA, "Hotel A", 500.0);
        Reserva r2 = buildReserva(2L, EstadoReserva.CANCELADA, "Hotel A", 0.0);

        when(reservaRepository.findAll()).thenReturn(List.of(r1, r2));
        when(hotelInternalApi.getTotalDepartamentos()).thenReturn(0);
        when(hotelInternalApi.getTotalHoteles()).thenReturn(0);
        when(hotelInternalApi.getTotalHabitaciones()).thenReturn(0);
        when(hotelInternalApi.getHotelesPorDepartamentoReal()).thenReturn(Map.of());

        DashboardStatsResponse result = dashboardService.obtenerEstadisticas();

        Map<String, Long> porEstado = result.getReservasPorEstado();
        assertThat(porEstado.get("CONFIRMADA")).isEqualTo(1L);
        assertThat(porEstado.get("CANCELADA")).isEqualTo(1L);
        assertThat(porEstado.get("PENDIENTE_PAGO")).isEqualTo(0L);
    }

    @Test
    void obtenerEstadisticasCalculaTopHotelesOrdenados() {
        Reserva r1 = buildReserva(1L, EstadoReserva.CONFIRMADA, "Hotel A", 100.0);
        Reserva r2 = buildReserva(2L, EstadoReserva.CONFIRMADA, "Hotel A", 200.0);
        Reserva r3 = buildReserva(3L, EstadoReserva.CONFIRMADA, "Hotel B", 150.0);

        when(reservaRepository.findAll()).thenReturn(List.of(r1, r2, r3));
        when(hotelInternalApi.getTotalDepartamentos()).thenReturn(0);
        when(hotelInternalApi.getTotalHoteles()).thenReturn(0);
        when(hotelInternalApi.getTotalHabitaciones()).thenReturn(0);
        when(hotelInternalApi.getHotelesPorDepartamentoReal()).thenReturn(Map.of());

        DashboardStatsResponse result = dashboardService.obtenerEstadisticas();

        assertThat(result.getTopHoteles()).hasSize(2);
        assertThat(result.getTopHoteles().get(0).getNombre()).isEqualTo("Hotel A");
        assertThat(result.getTopHoteles().get(0).getReservas()).isEqualTo(2L);
    }

    @Test
    void obtenerEstadisticasReservaRecienteConClienteNulo() {
        Reserva r = buildReserva(1L, EstadoReserva.PENDIENTE_PAGO, null, 100.0);
        r.setCliente(null); // sin cliente

        when(reservaRepository.findAll()).thenReturn(List.of(r));
        when(hotelInternalApi.getTotalDepartamentos()).thenReturn(0);
        when(hotelInternalApi.getTotalHoteles()).thenReturn(0);
        when(hotelInternalApi.getTotalHabitaciones()).thenReturn(0);
        when(hotelInternalApi.getHotelesPorDepartamentoReal()).thenReturn(Map.of());

        DashboardStatsResponse result = dashboardService.obtenerEstadisticas();

        assertThat(result.getReservasRecientes()).hasSize(1);
        assertThat(result.getReservasRecientes().get(0).getCliente()).isEqualTo("N/A");
        assertThat(result.getReservasRecientes().get(0).getHotel()).isEqualTo("N/A");
    }

    @Test
    void obtenerEstadisticasReservaRecienteConClienteSinApellido() {
        Reserva r = buildReserva(1L, EstadoReserva.CONFIRMADA, "Hotel A", 100.0);
        Cliente cliente = new Cliente();
        cliente.setNombre("Juan");
        cliente.setApellido(null);
        r.setCliente(cliente);

        when(reservaRepository.findAll()).thenReturn(List.of(r));
        when(hotelInternalApi.getTotalDepartamentos()).thenReturn(0);
        when(hotelInternalApi.getTotalHoteles()).thenReturn(0);
        when(hotelInternalApi.getTotalHabitaciones()).thenReturn(0);
        when(hotelInternalApi.getHotelesPorDepartamentoReal()).thenReturn(Map.of());

        DashboardStatsResponse result = dashboardService.obtenerEstadisticas();

        assertThat(result.getReservasRecientes().get(0).getCliente()).isEqualTo("Juan");
    }

    @Test
    void obtenerEstadisticasReservaRecienteConClienteSinNombre() {
        Reserva r = buildReserva(1L, EstadoReserva.CONFIRMADA, "Hotel A", 100.0);
        Cliente cliente = new Cliente();
        cliente.setNombre(null);
        cliente.setApellido("Perez");
        r.setCliente(cliente);

        when(reservaRepository.findAll()).thenReturn(List.of(r));
        when(hotelInternalApi.getTotalDepartamentos()).thenReturn(0);
        when(hotelInternalApi.getTotalHoteles()).thenReturn(0);
        when(hotelInternalApi.getTotalHabitaciones()).thenReturn(0);
        when(hotelInternalApi.getHotelesPorDepartamentoReal()).thenReturn(Map.of());

        DashboardStatsResponse result = dashboardService.obtenerEstadisticas();

        assertThat(result.getReservasRecientes().get(0).getCliente()).isEqualTo("Perez");
    }

    @Test
    void obtenerEstadisticasReservaConHotelNombreNulo() {
        Reserva r = buildReserva(1L, EstadoReserva.CONFIRMADA, null, 100.0);

        when(reservaRepository.findAll()).thenReturn(List.of(r));
        when(hotelInternalApi.getTotalDepartamentos()).thenReturn(0);
        when(hotelInternalApi.getTotalHoteles()).thenReturn(0);
        when(hotelInternalApi.getTotalHabitaciones()).thenReturn(0);
        when(hotelInternalApi.getHotelesPorDepartamentoReal()).thenReturn(Map.of());

        DashboardStatsResponse result = dashboardService.obtenerEstadisticas();

        // Con hotel nulo, no se cuenta en top hoteles
        assertThat(result.getTopHoteles()).isEmpty();
    }

    // ==================== helpers ====================

    private Reserva buildReserva(Long id, EstadoReserva estado, String hotelNombre, double total) {
        Reserva r = new Reserva();
        r.setId(id);
        r.setEstado(estado);
        r.setHotelNombre(hotelNombre);
        r.setTotal(total);
        r.setFechaReserva(LocalDate.now());
        r.setFechaInicio(LocalDate.now().plusDays(2));
        r.setFechaFin(LocalDate.now().plusDays(4));

        Cliente cliente = new Cliente();
        cliente.setNombre("Juan");
        cliente.setApellido("Perez");
        r.setCliente(cliente);

        return r;
    }
}
