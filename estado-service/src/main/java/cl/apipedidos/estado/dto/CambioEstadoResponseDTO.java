package cl.apipedidos.estado.dto;

import java.time.LocalDateTime;

public record CambioEstadoResponseDTO(
        Long id,
        Long pedidoId,
        String estadoAnterior,
        String estadoNuevo,
        LocalDateTime fechaCambio,
        Long usuarioId
) {}
