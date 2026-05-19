package cl.apipedidos.metrica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricaProductoResponseDTO {
    private Long productoId;
    private String nombre;
    private Integer totalVendido;
}
