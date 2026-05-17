package cl.apipedidos.fabricacion.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordenes_fabricacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdenFabricacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_id", nullable = false, unique = true)
    private Long pedidoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_fabricacion", nullable = false)
    private EstadoFabricacion estadoFabricacion;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "descripcion_estado")
    private String descripcionEstado;

    @Column(name = "usuario_responsable")
    private String usuarioResponsable;

    @OneToMany(mappedBy = "ordenFabricacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistorialFabricacion> historial = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.estadoFabricacion == null) {
            this.estadoFabricacion = EstadoFabricacion.EN_PROCESO;
        }
        if (this.fechaInicio == null) {
            this.fechaInicio = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
