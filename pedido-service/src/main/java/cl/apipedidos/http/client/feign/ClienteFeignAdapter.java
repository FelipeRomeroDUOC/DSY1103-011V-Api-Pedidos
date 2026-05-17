package cl.apipedidos.http.client.feign;

import cl.apipedidos.http.dto.ClienteCreateRequestDTO;
import cl.apipedidos.http.dto.ClienteResponseDTO;
import cl.apipedidos.http.dto.ClienteUpdateRequestDTO;
import java.util.List;
import org.springframework.stereotype.Component;
import cl.apipedidos.http.error.HttpClientException;
import feign.FeignException;

@Component
public class ClienteFeignAdapter {

    private final ClienteFeignClient client;

    public ClienteFeignAdapter(ClienteFeignClient client) {
        this.client = client;
    }

    public ClienteResponseDTO crearCliente(ClienteCreateRequestDTO request) {
        try {
            return client.crearCliente(request);
        } catch (FeignException exception) {
            throw mapFeignException(exception);
        }
    }

    public List<ClienteResponseDTO> listarClientes(String comuna) {
        try {
            return client.listarClientes(comuna);
        } catch (FeignException exception) {
            throw mapFeignException(exception);
        }
    }

    public ClienteResponseDTO obtenerClientePorIdentificador(String identificador) {
        try {
            return client.obtenerClientePorIdentificador(identificador);
        } catch (FeignException exception) {
            throw mapFeignException(exception);
        }
    }

    public ClienteResponseDTO actualizarCliente(Long id, ClienteUpdateRequestDTO request) {
        try {
            return client.actualizarCliente(id, request);
        } catch (FeignException exception) {
            throw mapFeignException(exception);
        }
    }

    public void eliminarCliente(Long id) {
        try {
            client.eliminarCliente(id);
        } catch (FeignException exception) {
            throw mapFeignException(exception);
        }
    }

    private HttpClientException mapFeignException(FeignException exception) {
        int status = exception.status();
        String body = null;
        try {
            body = exception.contentUTF8();
        } catch (Exception ignore) {
        }

        return new HttpClientException(status, exception.getMessage(), null, body);
    }
}
