package cl.apipedidos.authservice.controller;

import cl.apipedidos.authservice.dto.ApiResponse;
import cl.apipedidos.authservice.dto.UsuarioRequestDTO;
import cl.apipedidos.authservice.dto.UsuarioResponseDTO;
import cl.apipedidos.authservice.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // GET /api/auth/usuarios — solo ADMIN
    @GetMapping
    public ResponseEntity<ApiResponse<List<UsuarioResponseDTO>>> listar() {
        return ResponseEntity.ok(
            ApiResponse.success("Usuarios obtenidos", usuarioService.listar()));
    }

    // POST /api/auth/usuarios — solo ADMIN
    @PostMapping
    public ResponseEntity<ApiResponse<UsuarioResponseDTO>> crear(
            @Valid @RequestBody UsuarioRequestDTO request) {
        UsuarioResponseDTO creado = usuarioService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Usuario creado", creado));
    }

    // PUT /api/auth/usuarios/{id} — solo ADMIN
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponseDTO>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequestDTO request) {
        UsuarioResponseDTO actualizado = usuarioService.actualizar(id, request);
        return ResponseEntity.ok(
            ApiResponse.success("Usuario actualizado", actualizado));
    }
}
