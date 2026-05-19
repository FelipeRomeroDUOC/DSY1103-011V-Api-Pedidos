package cl.apipedidos.transportistaservice.service;

import cl.apipedidos.transportistaservice.dto.TransportistaRequestDTO;
import cl.apipedidos.transportistaservice.dto.TransportistaResponseDTO;
import cl.apipedidos.transportistaservice.dto.TransportistaUpdateDTO;

import java.util.List;

public interface TransportistaService {
    TransportistaResponseDTO crearTransportista(TransportistaRequestDTO request);
    List<TransportistaResponseDTO> listarActivos();
    TransportistaResponseDTO obtenerPorId(Long id);
    TransportistaResponseDTO actualizarTransportista(Long id, TransportistaUpdateDTO request);
}
