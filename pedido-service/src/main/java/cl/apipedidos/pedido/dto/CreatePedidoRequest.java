package cl.apipedidos.pedido.dto;

import cl.apipedidos.pedido.entity.TipoDespacho;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreatePedidoRequest(
    @NotBlank(message = "El número de pedido es obligatorio") String numeroPedido,
    @NotNull(message = "El cliente es obligatorio") Long clienteId,
    @NotNull(message = "El tipo de despacho es obligatorio") TipoDespacho tipoDespacho,
    @NotNull(message = "Debe incluir al menos un item") @Size(min = 1, message = "El pedido debe tener al menos un item") @Valid List<CreateItemRequest> items
) {
}