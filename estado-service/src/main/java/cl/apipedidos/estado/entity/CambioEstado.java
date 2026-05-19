package cl.apipedidos.estado.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "cambios_estado")
@Getter
@Setter
public class CambioEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long pedidoId;

    @NotBlank
    private String estadoAnterior;

    @NotBlank
    private String estadoNuevo;

    private LocalDateTime fechaCambio;

    private Long usuarioId;
}
