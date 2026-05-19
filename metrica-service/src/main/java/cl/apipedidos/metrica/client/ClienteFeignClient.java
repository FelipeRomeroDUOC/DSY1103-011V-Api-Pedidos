package cl.apipedidos.metrica.client;

import cl.apipedidos.metrica.dto.ApiResponse;
import cl.apipedidos.metrica.dto.ClienteResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cliente-service", url = "${cliente.service.url}")
public interface ClienteFeignClient {

    @GetMapping("/api/clientes/{id}")
    ApiResponse<ClienteResponseDTO> obtenerCliente(@PathVariable("id") Long id);
}
