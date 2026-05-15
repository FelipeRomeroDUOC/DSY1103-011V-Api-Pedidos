package cl.apipedidos.pedido.client;

import cl.apipedidos.http.client.AbstractHttpClient;
import cl.apipedidos.http.dto.ClienteResponseDTO;
import cl.apipedidos.http.error.HttpClientException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ClienteServiceClient extends AbstractHttpClient {

    public ClienteServiceClient(
        ObjectMapper objectMapper,
        @Value("${services.cliente-service.url:http://localhost:8080}") String baseUrl
    ) {
        super(baseUrl, objectMapper);
    }

    public void validarCliente(Long clienteId) {
        try {
            get("/api/clientes/" + encodePathSegment(String.valueOf(clienteId)), new TypeReference<ClienteResponseDTO>() {});
        } catch (HttpClientException exception) {
            throw mapException(clienteId, exception);
        }
    }

    private ResponseStatusException mapException(Long clienteId, HttpClientException exception) {
        int statusCode = exception.getStatusCode();

        if (statusCode == -1) {
            return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo conectar con cliente-service", exception);
        }

        HttpStatus httpStatus;
        try {
            httpStatus = HttpStatus.valueOf(statusCode);
        } catch (IllegalArgumentException ignored) {
            httpStatus = HttpStatus.BAD_GATEWAY;
        }

        String message = exception.getErrorResponse() != null && exception.getErrorResponse().message() != null
            ? exception.getErrorResponse().message()
            : exception.getMessage();

        if (statusCode >= 500) {
            return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Error al validar cliente", exception);
        }

        if (statusCode == 404) {
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado: " + clienteId, exception);
        }

        return new ResponseStatusException(httpStatus, message, exception);
    }
}