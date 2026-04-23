package cl.apipedidos.cliente.entity;

import cl.apipedidos.ubicacion.entity.Comuna;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @Column(name = "id_cliente")
    private Long idCliente;

    @NotBlank
    @Size(max = 150)
    @Column(name = "nombre_cl", nullable = false, length = 150)
    private String nombreCl;

    @NotNull
    @Min(1_000_000L)
    @Max(999_999_999L)
    @Column(name = "rut_cl", nullable = false, unique = true)
    private Long rutCl;

    @NotBlank
    @Size(min = 1, max = 2)
    @Pattern(regexp = "^[0-9Kk]{1,2}$")
    @Column(name = "div_cl", nullable = false, length = 2)
    private String divCl;

    @NotBlank
    @Size(max = 200)
    @Column(name = "direccion_cl", nullable = false, length = 200)
    private String direccionCl;

    @NotBlank
    @Size(max = 254)
    @Column(name = "email_cl", nullable = false, length = 254)
    private String emailCl;

    @NotBlank
    @Size(max = 30)
    @Column(name = "telefono_cl", nullable = false, length = 30)
    private String telefonoCl;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_comuna", nullable = false)
    private Comuna comuna;

    @Column(name = "fecha_registro", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate fechaRegistro;

    @PrePersist
    public void beforeCreate() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDate.now();
        }
        if (divCl != null) {
            divCl = divCl.trim().toUpperCase();
        }
        if (nombreCl != null) {
            nombreCl = nombreCl.trim();
        }
        if (direccionCl != null) {
            direccionCl = direccionCl.trim();
        }
        if (emailCl != null) {
            emailCl = emailCl.trim();
        }
        if (telefonoCl != null) {
            telefonoCl = telefonoCl.trim();
        }
    }

    @PreUpdate
    public void beforeUpdate() {
        if (divCl != null) {
            divCl = divCl.trim().toUpperCase();
        }
        if (nombreCl != null) {
            nombreCl = nombreCl.trim();
        }
        if (direccionCl != null) {
            direccionCl = direccionCl.trim();
        }
        if (emailCl != null) {
            emailCl = emailCl.trim();
        }
        if (telefonoCl != null) {
            telefonoCl = telefonoCl.trim();
        }
    }
}
