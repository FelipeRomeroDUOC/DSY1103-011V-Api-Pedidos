package cl.apipedidos.metrica.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "metricas_cliente")
@Getter
@Setter
public class MetricaCliente {

    @Id
    private Long clienteId;

    private Double montoTotal;
    private Integer cantidadPedidos;
    private Double frecuenciaAnual;
    private LocalDateTime ultimaActualizacion;
}
