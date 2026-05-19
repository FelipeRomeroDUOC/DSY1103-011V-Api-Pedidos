package cl.apipedidos.logservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogEntrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String servicio;

    @NotBlank
    @Column(nullable = false)
    private String operacion;

    private String usuarioId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @NotBlank
    @Column(nullable = false)
    private String resultado;

    @Column(length = 1000)
    private String detalle;

    @PrePersist
    protected void beforeCreate() {
        normalize();
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void beforeUpdate() {
        normalize();
    }

    private void normalize() {
        servicio = normalizeText(servicio);
        operacion = normalizeText(operacion);
        usuarioId = normalizeNullableText(usuarioId);
        resultado = normalizeText(resultado);
        detalle = normalizeNullableText(detalle);
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeNullableText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}