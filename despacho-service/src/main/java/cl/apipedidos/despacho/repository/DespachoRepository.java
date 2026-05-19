package cl.apipedidos.despacho.repository;

import cl.apipedidos.despacho.entity.Despacho;
import cl.apipedidos.despacho.entity.TipoDespacho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DespachoRepository extends JpaRepository<Despacho, Long> {
    boolean existsByPedidoId(Long pedidoId);
    Optional<Despacho> findByPedidoId(Long pedidoId);
    List<Despacho> findByTipoDespacho(TipoDespacho tipo);
}
