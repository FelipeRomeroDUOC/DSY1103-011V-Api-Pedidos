package cl.apipedidos.authservice.config;

import cl.apipedidos.authservice.entity.Usuario;
import cl.apipedidos.authservice.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("h2")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder   passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) return;

        List.of(
            crearUsuario("Administrador",    "admin@empresa.com",           "pass123", Usuario.Rol.ADMIN),
            crearUsuario("Ana García",       "ana.garcia@empresa.com",      "user123", Usuario.Rol.ENCARGADO_PEDIDOS),
            crearUsuario("Carlos López",     "carlos.lopez@empresa.com",    "user123", Usuario.Rol.ENCARGADO_DESPACHO),
            crearUsuario("María Fernández",  "maria.fernandez@empresa.com", "user123", Usuario.Rol.COMERCIAL)
        ).forEach(usuarioRepository::save);

        System.out.println("[auth-service] Datos iniciales cargados: 4 usuarios de prueba.");
    }

    private Usuario crearUsuario(String nombre, String email, String pass, Usuario.Rol rol) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(pass));   // BCrypt — nunca texto plano
        u.setRol(rol);
        u.setActivo(true);
        return u;
    }
}
