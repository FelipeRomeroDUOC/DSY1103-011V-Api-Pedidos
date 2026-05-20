package cl.apipedidos.authservice.service;

import cl.apipedidos.authservice.dto.UsuarioRequestDTO;
import cl.apipedidos.authservice.dto.UsuarioResponseDTO;
import cl.apipedidos.authservice.entity.Usuario;
import cl.apipedidos.authservice.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder   passwordEncoder;

    @Override
    public List<UsuarioResponseDTO> listar() {
        return usuarioRepository.findAll().stream()
            .map(this::toResponseDTO)
            .toList();
    }

    @Override
    public UsuarioResponseDTO crear(UsuarioRequestDTO request) {
        // Verificar email duplicado
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Ya existe un usuario con el email: " + request.getEmail());
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(request.getRol() != null ? request.getRol() : Usuario.Rol.ENCARGADO_PEDIDOS);
        usuario.setActivo(request.getActivo() != null ? request.getActivo() : true);

        Usuario guardado = usuarioRepository.save(usuario);
        return toResponseDTO(guardado);
    }

    @Override
    public UsuarioResponseDTO actualizar(Long id, UsuarioRequestDTO request) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Usuario no encontrado con ID: " + id));

        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());

        // Solo hashear si viene password nuevo
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRol() != null) {
            usuario.setRol(request.getRol());
        }

        if (request.getActivo() != null) {
            usuario.setActivo(request.getActivo());
        }

        Usuario actualizado = usuarioRepository.save(usuario);
        return toResponseDTO(actualizado);
    }

    private UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        return UsuarioResponseDTO.builder()
            .id(usuario.getId())
            .nombre(usuario.getNombre())
            .email(usuario.getEmail())
            .rol(usuario.getRol().name())
            .activo(usuario.isActivo())
            .build();
    }
}
