package cl.apipedidos.logservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogRequestDTO {

    @NotBlank
    private String servicio;

    @NotBlank
    private String operacion;

    private String usuarioId;

    @NotBlank
    private String resultado;

    private String detalle;
}