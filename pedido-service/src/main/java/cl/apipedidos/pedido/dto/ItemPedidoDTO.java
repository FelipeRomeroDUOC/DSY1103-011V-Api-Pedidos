package cl.apipedidos.pedido.dto;

import java.math.BigDecimal;

public record ItemPedidoDTO(
    Long id,
    Long productoId,
    Integer cantidad,
    BigDecimal precioUnitario,
    BigDecimal subtotal
) {
}