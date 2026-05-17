package cl.apipedidos.fabricacion.dto;

import cl.apipedidos.fabricacion.entity.EstadoFabricacion;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEstadoFabricacionRequest {
    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoFabricacion nuevoEstado;

    private String motivo;
    private String usuarioId;
}
