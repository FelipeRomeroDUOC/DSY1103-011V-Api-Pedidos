package cl.apipedidos.fabricacion.client;

import cl.apipedidos.fabricacion.dto.PedidoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "pedido-service", url = "${services.pedido-service.url:http://localhost:8081}")
public interface PedidoFeignClient {

    @GetMapping("/api/pedidos/{id}")
    PedidoDTO obtenerPedido(@PathVariable("id") Long id);

    @PatchMapping("/api/pedidos/{id}/estado")
    PedidoDTO actualizarEstado(@PathVariable("id") Long id,
                               @RequestBody UpdateEstadoRequest request);

    class UpdateEstadoRequest {
        private String nuevoEstado;

        public UpdateEstadoRequest() {}

        public UpdateEstadoRequest(String nuevoEstado) { this.nuevoEstado = nuevoEstado; }

        public String getNuevoEstado() { return nuevoEstado; }
        public void setNuevoEstado(String nuevoEstado) { this.nuevoEstado = nuevoEstado; }
    }
}
