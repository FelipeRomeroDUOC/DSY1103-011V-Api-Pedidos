package cl.apipedidos.producto.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String nombre;

    @Size(max = 500)
    @Column(length = 500)
    private String descripcion;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String categoria;

    @NotNull
    @DecimalMin("0.0")
    @Column(name = "precio_base", nullable = false)
    private Double precioBase;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    public void beforeCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (nombre != null) {
            nombre = nombre.trim();
        }
        if (descripcion != null) {
            descripcion = descripcion.trim();
        }
        if (categoria != null) {
            categoria = categoria.trim();
        }
    }

    @PreUpdate
    public void beforeUpdate() {
        fechaActualizacion = LocalDateTime.now();
        if (nombre != null) {
            nombre = nombre.trim();
        }
        if (descripcion != null) {
            descripcion = descripcion.trim();
        }
        if (categoria != null) {
            categoria = categoria.trim();
        }
    }
}
