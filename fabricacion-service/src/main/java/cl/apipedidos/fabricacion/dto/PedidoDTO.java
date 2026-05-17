package cl.apipedidos.fabricacion.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO local para deserializar la respuesta de pedido-service.
 * No tiene dependencia directa con pedido-service.
 */
public record PedidoDTO(
    Long id,
    String numeroPedido,
    Long clienteId,
    String estado,
    BigDecimal monto,
    String tipoDespacho,
    LocalDateTime fechaCreacion,
    LocalDateTime fechaActualizacion
) {
}
