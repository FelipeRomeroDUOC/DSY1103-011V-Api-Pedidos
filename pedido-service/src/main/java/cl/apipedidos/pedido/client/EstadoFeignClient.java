package cl.apipedidos.pedido.client;

 // Using ApiErrorResponse or generic response since we just want to post.
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "estado-service", url = "${services.estado-service.url}")
public interface EstadoFeignClient {

    @PostMapping("/api/estados")
    Object registrarCambio(@RequestBody CambioEstadoRequestDTO request);
}

