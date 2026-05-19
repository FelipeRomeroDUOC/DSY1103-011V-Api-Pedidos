package cl.apipedidos.transportistaservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TransportistaRequestDTO {
    @NotBlank
    private String nombre;
    
    @NotBlank
    private String codigoInterno;
    
    private String contacto;
    
    private String regionesCobertura;
}
