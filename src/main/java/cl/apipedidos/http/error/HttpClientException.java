package cl.apipedidos.http.error;

import cl.apipedidos.http.dto.ApiErrorResponse;

public class HttpClientException extends RuntimeException {

    private final int statusCode;
    private final ApiErrorResponse errorResponse;
    private final String responseBody;

    public HttpClientException(int statusCode, String message, ApiErrorResponse errorResponse, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.errorResponse = errorResponse;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public ApiErrorResponse getErrorResponse() {
        return errorResponse;
    }

    public String getResponseBody() {
        return responseBody;
    }
}