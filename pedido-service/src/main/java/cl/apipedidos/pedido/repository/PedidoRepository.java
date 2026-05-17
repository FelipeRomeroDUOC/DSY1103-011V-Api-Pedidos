package cl.apipedidos.pedido.repository;

import cl.apipedidos.pedido.entity.EstadoPedido;
import cl.apipedidos.pedido.entity.Pedido;
import cl.apipedidos.pedido.entity.TipoDespacho;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Optional<Pedido> findByNumeroPedido(String numeroPedido);

    boolean existsByNumeroPedido(String numeroPedido);

    List<Pedido> findByClienteIdOrderByFechaCreacionDesc(Long clienteId);

    List<Pedido> findByEstado(EstadoPedido estado);

    List<Pedido> findByEstadoAndTipoDespacho(EstadoPedido estado, TipoDespacho tipoDespacho);
}