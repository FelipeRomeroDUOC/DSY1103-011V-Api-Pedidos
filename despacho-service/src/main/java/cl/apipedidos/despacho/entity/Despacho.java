package cl.apipedidos.despacho.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "despachos")
@Getter
@Setter
public class Despacho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long pedidoId;

    @Enumerated(EnumType.STRING)
    @NotNull
    private TipoDespacho tipoDespacho;

    // Nombre del transportista (ej: "Starken", "Paket") o null si es RETIRO/RM
    private String transportista;

    private LocalDate fechaDespacho;

    private String trackingCode;
}
