package cl.apipedidos.http.client;

import cl.apipedidos.http.dto.ApiErrorResponse;
import cl.apipedidos.http.error.HttpClientException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public abstract class AbstractHttpClient {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    protected final ObjectMapper objectMapper;
    protected final HttpClient httpClient;
    protected final URI baseUri;

    protected AbstractHttpClient(String baseUrl, ObjectMapper objectMapper) {
        this(baseUrl, objectMapper, HttpClient.newBuilder().connectTimeout(DEFAULT_TIMEOUT).build());
    }

    protected AbstractHttpClient(String baseUrl, ObjectMapper objectMapper, HttpClient httpClient) {
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
        this.baseUri = URI.create(normalizeBaseUrl(baseUrl));
    }

    protected <T> T get(String path, TypeReference<T> responseType) {
        return execute("GET", path, null, responseType);
    }

    protected <T> T post(String path, Object body, TypeReference<T> responseType) {
        return execute("POST", path, body, responseType);
    }

    protected <T> T put(String path, Object body, TypeReference<T> responseType) {
        return execute("PUT", path, body, responseType);
    }

    protected void delete(String path) {
        execute("DELETE", path, null, null);
    }

    protected String encodePathSegment(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    protected String encodeQueryParam(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private <T> T execute(String method, String path, Object body, TypeReference<T> responseType) {
        HttpRequest request = buildRequest(method, path, body);

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return handleResponse(response, responseType);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new HttpClientException(-1, "HTTP client request interrupted", null, null);
        } catch (IOException exception) {
            throw new HttpClientException(-1, "HTTP client request failed", null, exception.getMessage());
        }
    }

    private HttpRequest buildRequest(String method, String path, Object body) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(resolvePath(path))
            .timeout(DEFAULT_TIMEOUT)
            .header("Accept", "application/json");

        if (body != null) {
            builder.header("Content-Type", "application/json");
        }

        return switch (method) {
            case "GET" -> builder.GET().build();
            case "POST" -> builder.POST(HttpRequest.BodyPublishers.ofString(serialize(body))).build();
            case "PUT" -> builder.PUT(HttpRequest.BodyPublishers.ofString(serialize(body))).build();
            case "DELETE" -> builder.DELETE().build();
            default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        };
    }

    private URI resolvePath(String path) {
        String normalizedPath = path.startsWith("/") ? path.substring(1) : path;
        return baseUri.resolve(normalizedPath);
    }

    private String normalizeBaseUrl(String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }

    private String serialize(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Unable to serialize HTTP request body", exception);
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, TypeReference<T> responseType) {
        int statusCode = response.statusCode();

        if (statusCode >= 200 && statusCode < 300) {
            if (responseType == null || statusCode == 204 || response.body() == null || response.body().isBlank()) {
                return null;
            }

            try {
                return objectMapper.readValue(response.body(), responseType);
            } catch (IOException exception) {
                throw new HttpClientException(statusCode, "Unable to deserialize HTTP response", null, response.body());
            }
        }

        ApiErrorResponse errorResponse = parseErrorResponse(response.body());
        String message = errorResponse != null && errorResponse.message() != null
            ? errorResponse.message()
            : "HTTP request failed with status " + statusCode;

        throw new HttpClientException(statusCode, message, errorResponse, response.body());
    }

    private ApiErrorResponse parseErrorResponse(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(body, ApiErrorResponse.class);
        } catch (IOException exception) {
            return null;
        }
    }
}