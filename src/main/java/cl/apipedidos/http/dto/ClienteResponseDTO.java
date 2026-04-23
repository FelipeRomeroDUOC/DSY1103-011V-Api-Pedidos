package cl.apipedidos.http.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public record ClienteResponseDTO(
    Long idCliente,
    String nombreCl,
    Long rutCl,
    String divCl,
    String direccionCl,
    String emailCl,
    String telefonoCl,
    String comuna,
    String region,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    LocalDate fechaRegistro
) {
}