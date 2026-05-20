package cl.apipedidos.authservice.service;

import cl.apipedidos.authservice.config.JwtUtil;
import cl.apipedidos.authservice.dto.LoginRequestDTO;
import cl.apipedidos.authservice.dto.LoginResponseDTO;
import cl.apipedidos.authservice.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil               jwtUtil;
    private final UsuarioRepository     usuarioRepository;

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        // 1. Delegar la validación de credenciales a Spring Security (BCrypt interno)
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );

        // Si llega aquí, las credenciales son correctas
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // 2. Obtener el rol real desde la BD
        String rol = usuarioRepository.findByEmail(userDetails.getUsername())
            .map(u -> u.getRol().name())
            .orElseThrow();

        // 3. Generar el token JWT
        String token = jwtUtil.generarToken(userDetails.getUsername(), rol);

        // 4. Construir y retornar la respuesta
        return LoginResponseDTO.builder()
            .token(token)
            .tipo("Bearer")
            .email(userDetails.getUsername())
            .rol(rol)
            .expiracion(System.currentTimeMillis() + jwtUtil.getExpirationMs())
            .build();
    }
}
