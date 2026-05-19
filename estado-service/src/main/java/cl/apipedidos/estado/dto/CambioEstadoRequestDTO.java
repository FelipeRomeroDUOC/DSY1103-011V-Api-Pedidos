package cl.apipedidos.estado.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoRequestDTO(
        @NotNull(message = "El pedidoId es obligatorio")
        Long pedidoId,
        
        @NotBlank(message = "El estado anterior es obligatorio")
        String estadoAnterior,
        
        @NotBlank(message = "El estado nuevo es obligatorio")
        String estadoNuevo,
        
        Long usuarioId
) {}
