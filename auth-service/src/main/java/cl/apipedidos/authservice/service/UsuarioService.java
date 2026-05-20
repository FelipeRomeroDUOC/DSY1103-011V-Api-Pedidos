package cl.apipedidos.authservice.service;

import cl.apipedidos.authservice.dto.UsuarioRequestDTO;
import cl.apipedidos.authservice.dto.UsuarioResponseDTO;

import java.util.List;

public interface UsuarioService {
    List<UsuarioResponseDTO> listar();
    UsuarioResponseDTO crear(UsuarioRequestDTO request);
    UsuarioResponseDTO actualizar(Long id, UsuarioRequestDTO request);
}
