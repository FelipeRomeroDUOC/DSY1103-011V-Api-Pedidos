package cl.apipedidos.pedido.repository;

import cl.apipedidos.pedido.entity.ItemPedido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {

    List<ItemPedido> findByPedidoId(Long pedidoId);
}