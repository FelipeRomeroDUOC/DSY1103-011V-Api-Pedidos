package cl.apipedidos.http.client;

import cl.apipedidos.http.dto.ClienteCreateRequestDTO;
import cl.apipedidos.http.dto.ClienteResponseDTO;
import cl.apipedidos.http.dto.ClienteUpdateRequestDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
public class ClienteApiClient extends AbstractHttpClient {

    public ClienteApiClient(
        ObjectMapper objectMapper,
        @Value("${app.http.base-url:http://localhost:8080}") String baseUrl
    ) {
        super(baseUrl, objectMapper);
    }

    public ClienteResponseDTO crearCliente(@Valid ClienteCreateRequestDTO request) {
        return post("/api/clientes", request, new TypeReference<ClienteResponseDTO>() {});
    }

    public List<ClienteResponseDTO> listarClientes() {
        return listarClientes(null);
    }

    public List<ClienteResponseDTO> listarClientes(String comuna) {
        String path = "/api/clientes";
        if (comuna != null && !comuna.isBlank()) {
            path += "?comuna=" + encodeQueryParam(comuna);
        }

        return get(path, new TypeReference<List<ClienteResponseDTO>>() {});
    }

    public ClienteResponseDTO obtenerClientePorIdentificador(String identificador) {
        return get("/api/clientes/" + encodePathSegment(identificador), new TypeReference<ClienteResponseDTO>() {});
    }

    public ClienteResponseDTO actualizarCliente(Long id, @Valid ClienteUpdateRequestDTO request) {
        return put("/api/clientes/" + id, request, new TypeReference<ClienteResponseDTO>() {});
    }

    public void eliminarCliente(Long id) {
        delete("/api/clientes/" + id);
    }
}