package cl.apipedidos.metrica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricaClienteResponseDTO {
    private Long clienteId;
    private String nombreCliente;
    private Double montoTotal;
    private Integer cantidadPedidos;
    private Double frecuenciaAnual;
}
