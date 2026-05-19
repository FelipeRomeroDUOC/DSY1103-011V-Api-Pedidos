package cl.apipedidos.estado.service;

import cl.apipedidos.estado.dto.CambioEstadoRequestDTO;
import cl.apipedidos.estado.dto.CambioEstadoResponseDTO;

import java.util.List;

public interface EstadoService {
    CambioEstadoResponseDTO registrarCambio(CambioEstadoRequestDTO request);
    List<CambioEstadoResponseDTO> obtenerHistorialPorPedido(Long pedidoId);
}
