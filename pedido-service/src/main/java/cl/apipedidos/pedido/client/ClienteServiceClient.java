package cl.apipedidos.pedido.client;

import cl.apipedidos.http.client.feign.ClienteFeignAdapter;
import cl.apipedidos.http.error.HttpClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@Slf4j
public class ClienteServiceClient {

    private final ClienteFeignAdapter clienteAdapter;

    public ClienteServiceClient(ClienteFeignAdapter clienteAdapter) {
        this.clienteAdapter = clienteAdapter;
    }

    public void validarCliente(Long clienteId) {
        log.debug("Validando clienteId={} contra cliente-service", clienteId);
        try {
            var cliente = clienteAdapter.obtenerClientePorIdentificador(String.valueOf(clienteId));
            log.info("Cliente validado correctamente id={}, nombre={}", clienteId, cliente.nombreCl());
        } catch (HttpClientException exception) {
            log.warn("Falló la validación de clienteId={} status={} body={}", clienteId, exception.getStatusCode(), exception.getResponseBody());
            throw mapException(clienteId, exception);
        }
    }

    private ResponseStatusException mapException(Long clienteId, HttpClientException exception) {
        int statusCode = exception.getStatusCode();

        if (statusCode == -1) {
            log.error("cliente-service no respondió para clienteId={}", clienteId, exception);
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
            log.error("cliente-service devolvió error 5xx para clienteId={} status={} body={}", clienteId, statusCode, exception.getResponseBody(), exception);
            return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Error al validar cliente", exception);
        }

        if (statusCode == 404) {
            log.info("cliente-service informó cliente inexistente para clienteId={}", clienteId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado: " + clienteId, exception);
        }

        log.warn("cliente-service devolvió status inesperado para clienteId={} status={} body={}", clienteId, statusCode, exception.getResponseBody());
        return new ResponseStatusException(httpStatus, message, exception);
    }
}