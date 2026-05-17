package cl.apipedidos.pedido.dto;

import cl.apipedidos.pedido.entity.EstadoPedido;
import cl.apipedidos.pedido.entity.TipoDespacho;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoDTO(
    Long id,
    String numeroPedido,
    Long clienteId,
    EstadoPedido estado,
    BigDecimal monto,
    TipoDespacho tipoDespacho,
    LocalDateTime fechaCreacion,
    LocalDateTime fechaActualizacion,
    List<ItemPedidoDTO> items
) {
}