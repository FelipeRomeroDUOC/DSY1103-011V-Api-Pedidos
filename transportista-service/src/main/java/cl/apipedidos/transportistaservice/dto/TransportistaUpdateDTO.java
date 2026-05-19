package cl.apipedidos.transportistaservice.dto;

import lombok.Data;

@Data
public class TransportistaUpdateDTO {
    private String nombre;
    private String contacto;
    private String regionesCobertura;
    private Boolean activo;
}
