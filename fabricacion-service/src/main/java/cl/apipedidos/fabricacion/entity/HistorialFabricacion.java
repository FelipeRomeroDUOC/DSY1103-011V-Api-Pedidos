package cl.apipedidos.fabricacion.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_fabricacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialFabricacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_fabricacion_id", nullable = false)
    private OrdenFabricacion ordenFabricacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior")
    private EstadoFabricacion estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false)
    private EstadoFabricacion estadoNuevo;

    @Column(name = "fecha_cambio", nullable = false)
    private LocalDateTime fechaCambio;

    @Column(name = "usuario_id")
    private String usuarioId;

    @Column(name = "motivo")
    private String motivo;

    @PrePersist
    protected void onCreate() {
        this.fechaCambio = LocalDateTime.now();
    }
}
