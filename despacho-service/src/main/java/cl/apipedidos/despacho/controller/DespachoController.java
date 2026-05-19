package cl.apipedidos.despacho.controller;

import cl.apipedidos.despacho.dto.ApiResponse;
import cl.apipedidos.despacho.dto.DespachoRequestDTO;
import cl.apipedidos.despacho.dto.DespachoResponseDTO;
import cl.apipedidos.despacho.dto.DespachoUpdateDTO;
import cl.apipedidos.despacho.entity.TipoDespacho;
import cl.apipedidos.despacho.service.DespachoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/despachos")
@RequiredArgsConstructor
@Slf4j
public class DespachoController {

    private final DespachoService despachoService;

    @PostMapping
    public ResponseEntity<ApiResponse<DespachoResponseDTO>> registrar(@Valid @RequestBody DespachoRequestDTO request) {
        log.info("POST /api/despachos pedidoId={}", request.pedidoId());
        DespachoResponseDTO despacho = despachoService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Despacho registrado correctamente", despacho));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DespachoResponseDTO>>> listar(@RequestParam(required = false) TipoDespacho tipo) {
        log.info("GET /api/despachos tipo={}", tipo);
        List<DespachoResponseDTO> despachos = despachoService.listar(tipo);
        return ResponseEntity.ok(ApiResponse.success("Despachos obtenidos exitosamente", despachos));
    }

    @GetMapping("/{pedidoId}")
    public ResponseEntity<ApiResponse<DespachoResponseDTO>> obtenerPorPedidoId(@PathVariable Long pedidoId) {
        log.info("GET /api/despachos/{}", pedidoId);
        DespachoResponseDTO despacho = despachoService.obtenerPorPedidoId(pedidoId);
        return ResponseEntity.ok(ApiResponse.success("Despacho obtenido exitosamente", despacho));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DespachoResponseDTO>> actualizar(@PathVariable Long id, @Valid @RequestBody DespachoUpdateDTO request) {
        log.info("PUT /api/despachos/{}", id);
        DespachoResponseDTO despacho = despachoService.actualizar(id, request);
        return ResponseEntity.ok(ApiResponse.success("Despacho actualizado exitosamente", despacho));
    }

    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<String>> ping() {
        return ResponseEntity.ok(ApiResponse.success("despacho-service activo", null));
    }
}
