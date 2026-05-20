package cl.apipedidos.authservice.config;

import cl.apipedidos.authservice.entity.Usuario;
import cl.apipedidos.authservice.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
            .filter(u -> u.getPassword() != null && !u.getPassword().isBlank())
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        return org.springframework.security.core.userdetails.User
            .withUsername(usuario.getEmail())
            .password(usuario.getPassword())
            .roles(usuario.getRol().name())   // Spring agrega ROLE_ automáticamente
            .disabled(!usuario.isActivo())
            .build();
    }
}
