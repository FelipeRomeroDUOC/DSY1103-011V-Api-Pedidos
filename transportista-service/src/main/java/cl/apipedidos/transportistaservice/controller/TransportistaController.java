package cl.apipedidos.transportistaservice.controller;

import cl.apipedidos.transportistaservice.dto.TransportistaRequestDTO;
import cl.apipedidos.transportistaservice.dto.TransportistaResponseDTO;
import cl.apipedidos.transportistaservice.dto.TransportistaUpdateDTO;
import cl.apipedidos.transportistaservice.response.ApiResponse;
import cl.apipedidos.transportistaservice.service.TransportistaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transportistas")
@RequiredArgsConstructor
public class TransportistaController {

    private final TransportistaService transportistaService;

    @PostMapping
    public ResponseEntity<ApiResponse<TransportistaResponseDTO>> registrarTransportista(
            @Valid @RequestBody TransportistaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transportista registrado correctamente", transportistaService.crearTransportista(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransportistaResponseDTO>>> listarTransportistasActivos() {
        return ResponseEntity.ok(ApiResponse.success("Listado obtenido", transportistaService.listarActivos()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransportistaResponseDTO>> obtenerTransportista(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Transportista obtenido", transportistaService.obtenerPorId(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransportistaResponseDTO>> actualizarTransportista(
            @PathVariable Long id, 
            @Valid @RequestBody TransportistaUpdateDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Transportista actualizado", transportistaService.actualizarTransportista(id, request)));
    }

    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<Void>> ping() {
        return ResponseEntity.ok(ApiResponse.success("transportista-service activo", null));
    }
}
