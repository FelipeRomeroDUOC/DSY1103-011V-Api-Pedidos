package cl.apipedidos.fabricacion.controller;

import cl.apipedidos.fabricacion.dto.ApiResponse;
import cl.apipedidos.fabricacion.dto.OrdenFabricacionRequest;
import cl.apipedidos.fabricacion.dto.OrdenFabricacionResponse;
import cl.apipedidos.fabricacion.dto.UpdateEstadoFabricacionRequest;
import cl.apipedidos.fabricacion.service.OrdenFabricacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/fabricacion")
@RequiredArgsConstructor
public class OrdenFabricacionController {

    private final OrdenFabricacionService ordenService;

    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<String>> ping() {
        return ResponseEntity.ok(ApiResponse.success("pong", "fabricacion-service"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrdenFabricacionResponse>> crearOrden(@Valid @RequestBody OrdenFabricacionRequest request) {
        OrdenFabricacionResponse resp = ordenService.crearOrden(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Orden creada", resp));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrdenFabricacionResponse>> obtenerOrden(@PathVariable Long id) {
        OrdenFabricacionResponse resp = ordenService.obtenerOrden(id);
        return ResponseEntity.ok(ApiResponse.success("Orden encontrada", resp));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<OrdenFabricacionResponse>> actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEstadoFabricacionRequest request) {
        OrdenFabricacionResponse resp = ordenService.actualizarEstado(id, request);
        return ResponseEntity.ok(ApiResponse.success("Estado actualizado", resp));
    }
}

