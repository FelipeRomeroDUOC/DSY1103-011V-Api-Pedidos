package cl.apipedidos.estado.repository;

import cl.apipedidos.estado.entity.CambioEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CambioEstadoRepository extends JpaRepository<CambioEstado, Long> {
    List<CambioEstado> findByPedidoIdOrderByFechaCambioAsc(Long pedidoId);
}
