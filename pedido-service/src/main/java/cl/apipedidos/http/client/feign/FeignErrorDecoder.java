package cl.apipedidos.http.client.feign;

import cl.apipedidos.http.dto.ApiErrorResponse;
import cl.apipedidos.http.error.HttpClientException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;

    public FeignErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        String body = null;
        try (InputStream in = response.body() != null ? response.body().asInputStream() : null) {
            if (in != null) {
                byte[] bytes = in.readAllBytes();
                if (bytes.length > 0) {
                    body = new String(bytes, StandardCharsets.UTF_8);
                }
            }
        } catch (IOException ignored) {
        }

        ApiErrorResponse errorResponse = null;
        if (body != null && !body.isBlank()) {
            try {
                errorResponse = objectMapper.readValue(body, ApiErrorResponse.class);
            } catch (Exception ignored) {
            }
        }

        return new HttpClientException(response.status(),
            errorResponse != null && errorResponse.message() != null ? errorResponse.message() : "Feign client error",
            errorResponse,
            body
        );
    }
}
