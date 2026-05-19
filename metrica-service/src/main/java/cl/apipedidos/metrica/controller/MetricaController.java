package cl.apipedidos.metrica.controller;

import cl.apipedidos.metrica.dto.ApiResponse;
import cl.apipedidos.metrica.dto.MetricaClienteResponseDTO;
import cl.apipedidos.metrica.dto.MetricaProductoResponseDTO;
import cl.apipedidos.metrica.dto.ResumenVentasResponseDTO;
import cl.apipedidos.metrica.service.MetricaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/metricas")
@RequiredArgsConstructor
public class MetricaController {

    private final MetricaService metricaService;

    @GetMapping("/clientes/{id}")
    public ResponseEntity<ApiResponse<MetricaClienteResponseDTO>> obtenerMetricasCliente(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Métricas del cliente obtenidas correctamente", metricaService.obtenerMetricasCliente(id)));
    }

    @GetMapping("/clientes/ranking")
    public ResponseEntity<ApiResponse<List<MetricaClienteResponseDTO>>> obtenerRankingClientes(
            @RequestParam(required = false, defaultValue = "10") Integer limite) {
        return ResponseEntity.ok(ApiResponse.success("Ranking obtenido correctamente", metricaService.obtenerRankingClientes(limite)));
    }

    @GetMapping("/productos/top")
    public ResponseEntity<ApiResponse<List<MetricaProductoResponseDTO>>> obtenerTopProductos(
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta,
            @RequestParam(required = false, defaultValue = "10") Integer limite) {
            
        if (desde == null) desde = LocalDate.now().withDayOfMonth(1);
        if (hasta == null) hasta = LocalDate.now();
        
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }
        
        return ResponseEntity.ok(ApiResponse.success("Top productos obtenido", metricaService.obtenerTopProductos(desde, hasta, limite)));
    }

    @GetMapping("/ventas")
    public ResponseEntity<ApiResponse<ResumenVentasResponseDTO>> obtenerResumenVentas(
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta) {
            
        if (desde == null) desde = LocalDate.now().withDayOfMonth(1);
        if (hasta == null) hasta = LocalDate.now();

        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }

        return ResponseEntity.ok(ApiResponse.success("Resumen de ventas obtenido", metricaService.obtenerResumenVentas(desde, hasta)));
    }

    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<String>> ping() {
        return ResponseEntity.ok(ApiResponse.success("metrica-service activo", null));
    }
}
