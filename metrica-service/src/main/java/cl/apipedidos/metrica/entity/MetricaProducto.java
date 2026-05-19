package cl.apipedidos.metrica.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "metricas_producto")
@Getter
@Setter
public class MetricaProducto {

    @Id
    private Long productoId;

    private String nombre;
    private Integer totalVendido;
    private String periodo;
    private LocalDateTime ultimaActualizacion;
}
