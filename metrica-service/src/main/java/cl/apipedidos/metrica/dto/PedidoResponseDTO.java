package cl.apipedidos.metrica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponseDTO {
    private Long id;
    private Long clienteId;
    private Double monto;
    private LocalDateTime fechaCreacion;
    private List<ItemPedidoDTO> items;
}
