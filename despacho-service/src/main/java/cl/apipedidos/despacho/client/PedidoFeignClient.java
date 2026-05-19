package cl.apipedidos.despacho.client;

import cl.apipedidos.despacho.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "pedido-service", url = "${pedido.service.url}")
public interface PedidoFeignClient {

    @GetMapping("/api/pedidos/{id}")
    ApiResponse<PedidoResponseDTO> obtenerPedido(@PathVariable("id") Long id);
}
