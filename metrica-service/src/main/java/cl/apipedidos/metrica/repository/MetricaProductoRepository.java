package cl.apipedidos.metrica.repository;

import cl.apipedidos.metrica.entity.MetricaProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetricaProductoRepository extends JpaRepository<MetricaProducto, Long> {
}
