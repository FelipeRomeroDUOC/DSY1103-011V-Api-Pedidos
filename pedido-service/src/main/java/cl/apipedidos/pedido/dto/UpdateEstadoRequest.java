package cl.apipedidos.pedido.dto;

import cl.apipedidos.pedido.entity.EstadoPedido;
import jakarta.validation.constraints.NotNull;

public record UpdateEstadoRequest(
    @NotNull(message = "El estado es obligatorio") EstadoPedido nuevoEstado
) {
}