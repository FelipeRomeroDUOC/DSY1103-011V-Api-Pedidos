package cl.apipedidos.metrica.repository;

import cl.apipedidos.metrica.entity.MetricaCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetricaClienteRepository extends JpaRepository<MetricaCliente, Long> {
}
