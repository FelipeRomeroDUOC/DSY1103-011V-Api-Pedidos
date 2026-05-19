package cl.apipedidos.estado.controller;

import cl.apipedidos.estado.dto.ApiResponse;
import cl.apipedidos.estado.dto.CambioEstadoRequestDTO;
import cl.apipedidos.estado.dto.CambioEstadoResponseDTO;
import cl.apipedidos.estado.service.EstadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estados")
@RequiredArgsConstructor
public class EstadoController {

    private final EstadoService estadoService;

    @PostMapping
    public ResponseEntity<ApiResponse<CambioEstadoResponseDTO>> registrarCambio(
            @Valid @RequestBody CambioEstadoRequestDTO request) {
        CambioEstadoResponseDTO response = estadoService.registrarCambio(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Cambio de estado registrado correctamente", response));
    }

    @GetMapping("/{pedidoId}")
    public ResponseEntity<ApiResponse<List<CambioEstadoResponseDTO>>> obtenerHistorial(
            @PathVariable Long pedidoId) {
        List<CambioEstadoResponseDTO> historial = estadoService.obtenerHistorialPorPedido(pedidoId);
        return ResponseEntity.ok(ApiResponse.success("Historial obtenido correctamente", historial));
    }

    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<String>> ping() {
        return ResponseEntity.ok(ApiResponse.success("estado-service activo", null));
    }
}
