package cl.apipedidos.fabricacion.dto;

import cl.apipedidos.fabricacion.entity.EstadoFabricacion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdenFabricacionResponse {
    private Long id;
    private Long numeroPedido;
    private EstadoFabricacion estadoFabricacion;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String descripcionEstado;
    private String usuarioResponsable;
}

