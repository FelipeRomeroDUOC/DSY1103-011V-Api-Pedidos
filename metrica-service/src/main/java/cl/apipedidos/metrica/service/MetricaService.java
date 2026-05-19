package cl.apipedidos.metrica.service;

import cl.apipedidos.metrica.dto.MetricaClienteResponseDTO;
import cl.apipedidos.metrica.dto.MetricaProductoResponseDTO;
import cl.apipedidos.metrica.dto.ResumenVentasResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface MetricaService {
    MetricaClienteResponseDTO obtenerMetricasCliente(Long clienteId);
    List<MetricaClienteResponseDTO> obtenerRankingClientes(Integer limite);
    List<MetricaProductoResponseDTO> obtenerTopProductos(LocalDate desde, LocalDate hasta, Integer limite);
    ResumenVentasResponseDTO obtenerResumenVentas(LocalDate desde, LocalDate hasta);
}
