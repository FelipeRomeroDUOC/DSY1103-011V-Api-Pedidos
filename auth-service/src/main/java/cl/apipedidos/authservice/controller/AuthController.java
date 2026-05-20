package cl.apipedidos.authservice.controller;

import cl.apipedidos.authservice.dto.ApiResponse;
import cl.apipedidos.authservice.dto.LoginRequestDTO;
import cl.apipedidos.authservice.dto.LoginResponseDTO;
import cl.apipedidos.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/login — público, no requiere token
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login exitoso", response));
    }

    // POST /api/auth/logout — requiere token válido
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // Con JWT stateless el logout es del lado del cliente (descartar el token).
        // El servidor confirma la recepción.
        return ResponseEntity.ok(
            ApiResponse.success("Sesión cerrada. Descarta el token en el cliente.", null));
    }

    // GET /api/auth/ping — healthcheck público
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("auth-service OK");
    }
}
