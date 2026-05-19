package cl.apipedidos.transportistaservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "transportistas")
@Getter
@Setter
public class Transportista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nombre;

    @NotBlank
    @Column(unique = true)
    private String codigoInterno;

    private String contacto;

    private String regionesCobertura;

    private boolean activo = true;
}
