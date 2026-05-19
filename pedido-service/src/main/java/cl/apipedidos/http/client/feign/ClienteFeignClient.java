package cl.apipedidos.http.client.feign;

import cl.apipedidos.http.dto.ClienteCreateRequestDTO;
import cl.apipedidos.http.dto.ClienteResponseDTO;
import cl.apipedidos.http.dto.ClienteUpdateRequestDTO;
import cl.apipedidos.http.dto.ApiResponse;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "cliente-service", url = "${services.cliente-service.url:http://127.0.0.1:8080}")
public interface ClienteFeignClient {

    @PostMapping("/api/clientes")
    ApiResponse<ClienteResponseDTO> crearCliente(@RequestBody ClienteCreateRequestDTO request);

    @GetMapping("/api/clientes")
    ApiResponse<java.util.List<ClienteResponseDTO>> listarClientes(@RequestParam(value = "comuna", required = false) String comuna);

    @GetMapping("/api/clientes/{id}")
    ApiResponse<ClienteResponseDTO> obtenerClientePorIdentificador(@PathVariable("id") String identificador);

    @PutMapping("/api/clientes/{id}")
    ApiResponse<ClienteResponseDTO> actualizarCliente(@PathVariable("id") Long id, @RequestBody ClienteUpdateRequestDTO request);

    @DeleteMapping("/api/clientes/{id}")
    ApiResponse<Void> eliminarCliente(@PathVariable("id") Long id);
}
