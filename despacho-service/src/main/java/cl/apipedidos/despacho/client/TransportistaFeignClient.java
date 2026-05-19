package cl.apipedidos.despacho.client;

import cl.apipedidos.despacho.dto.ApiResponse;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "transportista-service", url = "${services.transportista-service.url}")
public interface TransportistaFeignClient {

    @GetMapping("/api/transportistas/{id}")
    ApiResponse<TransportistaResponseDTO> obtenerTransportista(@PathVariable("id") Long id);

    @Data
    class TransportistaResponseDTO {
        private Long id;
        private String nombre;
        private String codigoInterno;
        private String contacto;
        private String regionesCobertura;
        private boolean activo;
    }
}
