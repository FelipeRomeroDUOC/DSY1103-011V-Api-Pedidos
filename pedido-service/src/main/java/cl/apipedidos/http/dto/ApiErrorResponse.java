package cl.apipedidos.http.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiErrorResponse(
    int status,
    String message,
    String path,
    OffsetDateTime timestamp,
    List<String> errors
) {
}