package cl.apipedidos.producto.controller;

import cl.apipedidos.producto.dto.ApiResponse;
import cl.apipedidos.producto.dto.ProductoRequestDTO;
import cl.apipedidos.producto.dto.ProductoResponseDTO;
import cl.apipedidos.producto.service.ProductoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@Slf4j
public class ProductoController {

    private final ProductoService productoService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductoResponseDTO>> crear(@Valid @RequestBody ProductoRequestDTO request) {
        log.info("POST /api/productos nombre={}", request.nombre());
        ProductoResponseDTO producto = productoService.crear(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Producto creado exitosamente", producto));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductoResponseDTO>>> listar(
            @RequestParam(required = false, defaultValue = "false") boolean incluirInactivos) {
        log.info("GET /api/productos?incluirInactivos={}", incluirInactivos);
        List<ProductoResponseDTO> productos = productoService.listar(incluirInactivos);
        return ResponseEntity.ok(ApiResponse.success("Productos obtenidos exitosamente", productos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoResponseDTO>> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/productos/{}", id);
        ProductoResponseDTO producto = productoService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success("Producto obtenido exitosamente", producto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoResponseDTO>> actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequestDTO request) {
        log.info("PUT /api/productos/{} nombre={}", id, request.nombre());
        ProductoResponseDTO producto = productoService.actualizar(id, request);
        return ResponseEntity.ok(ApiResponse.success("Producto actualizado exitosamente", producto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> desactivar(@PathVariable Long id) {
        log.info("DELETE /api/productos/{}", id);
        productoService.desactivar(id);
        return ResponseEntity.ok(ApiResponse.success("Producto desactivado exitosamente", null));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<ApiResponse<Void>> activar(@PathVariable Long id) {
        log.info("PATCH /api/productos/{}/activar", id);
        productoService.activar(id);
        return ResponseEntity.ok(ApiResponse.success("Producto activado exitosamente", null));
    }

    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<String>> ping() {
        return ResponseEntity.ok(ApiResponse.success("producto-service activo", null));
    }
}
