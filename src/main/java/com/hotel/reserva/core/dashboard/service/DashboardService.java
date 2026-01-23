package com.hotel.reserva.core.dashboard.service;

import com.hotel.reserva.api.dto.DashboardReservaReciente;
import com.hotel.reserva.api.dto.DashboardStatsResponse;
import com.hotel.reserva.api.dto.DashboardTopHotel;
import com.hotel.reserva.core.detalle_reserva.model.DetalleReserva;
import com.hotel.reserva.core.reserva.model.Reserva;
import com.hotel.reserva.core.reserva.repository.ReservaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final String ESTADO_CONFIRMADA = "CONFIRMADA";
    private static final String ESTADO_CANCELADA = "CANCELADA";
    private static final String ESTADO_PENDIENTE = "PENDIENTE";

    private final ReservaRepository reservaRepository;

    public DashboardService(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    public DashboardStatsResponse obtenerEstadisticas() {
        List<Reserva> reservas = reservaRepository.findAll();

        DashboardStatsResponse response = new DashboardStatsResponse();
        response.setTotalDepartamentos(contarDepartamentos(reservas));
        response.setTotalHoteles(contarHoteles(reservas));
        response.setTotalHabitaciones(contarHabitaciones(reservas));
        response.setTotalReservas(reservas.size());
        response.setReservasPorEstado(calcularReservasPorEstado(reservas));
        response.setIngresosTotales(calcularIngresosTotales(reservas));
        response.setHotelesPorDepartamento(calcularHotelesPorDepartamento(reservas));
        response.setReservasPorMes(calcularReservasPorMes(reservas));
        response.setIngresosPorMes(calcularIngresosPorMes(reservas));
        response.setTopHoteles(calcularTopHoteles(reservas));
        response.setReservasRecientes(calcularReservasRecientes(reservas));

        return response;
    }

    private int contarDepartamentos(List<Reserva> reservas) {
        return (int) reservas.stream()
                .map(Reserva::getDepartamentoId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
    }

    private int contarHoteles(List<Reserva> reservas) {
        return (int) reservas.stream()
                .map(Reserva::getHotelId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
    }

    private long contarHabitaciones(List<Reserva> reservas) {
        return reservas.stream()
                .flatMap(reserva -> reserva.getDetalles().stream())
                .map(DetalleReserva::getHabitacionId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
    }

    private Map<String, Long> calcularReservasPorEstado(List<Reserva> reservas) {
        Map<String, Long> resultado = new HashMap<>();
        resultado.put(ESTADO_CONFIRMADA, reservas.stream()
                .filter(r -> ESTADO_CONFIRMADA.equals(r.getEstado()))
                .count());
        resultado.put(ESTADO_CANCELADA, reservas.stream()
                .filter(r -> ESTADO_CANCELADA.equals(r.getEstado()))
                .count());
        resultado.put(ESTADO_PENDIENTE, reservas.stream()
                .filter(r -> ESTADO_PENDIENTE.equals(r.getEstado()))
                .count());
        return resultado;
    }

    private double calcularIngresosTotales(List<Reserva> reservas) {
        return reservas.stream()
                .filter(r -> ESTADO_CONFIRMADA.equals(r.getEstado()))
                .mapToDouble(Reserva::getTotal)
                .sum();
    }

    private Map<String, Long> calcularHotelesPorDepartamento(List<Reserva> reservas) {
        Map<Long, Set<Long>> hotelesPorDepartamento = new LinkedHashMap<>();
        Map<Long, String> nombresDepartamento = new HashMap<>();

        for (Reserva reserva : reservas) {
            if (reserva.getDepartamentoId() == null || reserva.getHotelId() == null) {
                continue;
            }
            hotelesPorDepartamento
                    .computeIfAbsent(reserva.getDepartamentoId(), key -> new LinkedHashSet<>())
                    .add(reserva.getHotelId());

            if (reserva.getDepartamentoNombre() != null) {
                nombresDepartamento.putIfAbsent(reserva.getDepartamentoId(), reserva.getDepartamentoNombre());
            }
        }

        Map<String, Long> resultado = new LinkedHashMap<>();
        for (Map.Entry<Long, Set<Long>> entry : hotelesPorDepartamento.entrySet()) {
            String nombre = nombresDepartamento.getOrDefault(entry.getKey(), "Departamento " + entry.getKey());
            resultado.put(nombre, (long) entry.getValue().size());
        }

        return resultado;
    }

    private Map<String, Long> calcularReservasPorMes(List<Reserva> reservas) {
        Map<String, Long> resultado = new LinkedHashMap<>();
        LocalDate ahora = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            YearMonth mes = YearMonth.from(ahora.minusMonths(i));
            String nombreMes = mes.getMonth().toString().substring(0, 3) + " " + mes.getYear();

            long count = reservas.stream()
                    .filter(r -> r.getFechaReserva() != null)
                    .filter(r -> YearMonth.from(r.getFechaReserva()).equals(mes))
                    .count();

            resultado.put(nombreMes, count);
        }

        return resultado;
    }

    private Map<String, Double> calcularIngresosPorMes(List<Reserva> reservas) {
        Map<String, Double> resultado = new LinkedHashMap<>();
        LocalDate ahora = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            YearMonth mes = YearMonth.from(ahora.minusMonths(i));
            String nombreMes = mes.getMonth().toString().substring(0, 3) + " " + mes.getYear();

            double total = reservas.stream()
                    .filter(r -> ESTADO_CONFIRMADA.equals(r.getEstado()))
                    .filter(r -> r.getFechaReserva() != null)
                    .filter(r -> YearMonth.from(r.getFechaReserva()).equals(mes))
                    .mapToDouble(Reserva::getTotal)
                    .sum();

            resultado.put(nombreMes, total);
        }

        return resultado;
    }

    private List<DashboardTopHotel> calcularTopHoteles(List<Reserva> reservas) {
        Map<String, Long> reservasPorHotel = reservas.stream()
                .filter(r -> r.getHotelNombre() != null)
                .collect(Collectors.groupingBy(
                        Reserva::getHotelNombre,
                        Collectors.counting()
                ));

        return reservasPorHotel.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    DashboardTopHotel topHotel = new DashboardTopHotel();
                    topHotel.setNombre(entry.getKey());
                    topHotel.setReservas(entry.getValue());
                    return topHotel;
                })
                .toList();
    }

    private List<DashboardReservaReciente> calcularReservasRecientes(List<Reserva> reservas) {
        return reservas.stream()
                .sorted(Comparator.comparing(
                        Reserva::getFechaReserva,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .limit(5)
                .map(reserva -> {
                    DashboardReservaReciente reciente = new DashboardReservaReciente();
                    reciente.setId(reserva.getId());
                    reciente.setCliente(resolveClienteNombre(reserva));
                    reciente.setHotel(reserva.getHotelNombre() != null ? reserva.getHotelNombre() : "N/A");
                    reciente.setFechaInicio(reserva.getFechaInicio());
                    reciente.setFechaFin(reserva.getFechaFin());
                    reciente.setTotal(reserva.getTotal());
                    reciente.setEstado(reserva.getEstado());
                    return reciente;
                })
                .toList();
    }

    private String resolveClienteNombre(Reserva reserva) {
        if (reserva.getCliente() == null) {
            return "N/A";
        }
        String nombre = reserva.getCliente().getNombre();
        String apellido = reserva.getCliente().getApellido();
        if (nombre == null && apellido == null) {
            return "N/A";
        }
        if (nombre == null) {
            return apellido;
        }
        if (apellido == null) {
            return nombre;
        }
        return nombre + " " + apellido;
    }
}
