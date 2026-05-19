package cl.apipedidos.metrica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumenVentasResponseDTO {
    private LocalDate desde;
    private LocalDate hasta;
    private Double montoTotal;
    private Integer cantidadPedidos;
}
