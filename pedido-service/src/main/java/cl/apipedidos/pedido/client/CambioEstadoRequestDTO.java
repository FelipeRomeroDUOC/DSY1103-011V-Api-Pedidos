package cl.apipedidos.pedido.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambioEstadoRequestDTO {
    private Long pedidoId;
    private String estadoAnterior;
    private String estadoNuevo;
    private Long usuarioId;
}
