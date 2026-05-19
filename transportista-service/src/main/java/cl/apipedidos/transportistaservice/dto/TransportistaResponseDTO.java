package cl.apipedidos.transportistaservice.dto;

import lombok.Data;

@Data
public class TransportistaResponseDTO {
    private Long id;
    private String nombre;
    private String codigoInterno;
    private String contacto;
    private String regionesCobertura;
    private boolean activo;
}
