package cl.apipedidos.config;

import cl.apipedidos.http.dto.ApiErrorResponse;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(ResponseStatusException exception, WebRequest request) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        String message = exception.getReason() != null && !exception.getReason().isBlank()
            ? exception.getReason()
            : status.getReasonPhrase();

        return buildResponse(status, message, request, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception, WebRequest request) {
        List<String> errors = exception.getBindingResult().getFieldErrors().stream()
            .map(this::formatFieldError)
            .collect(Collectors.toList());

        return buildResponse(HttpStatus.BAD_REQUEST, "La solicitud contiene errores de validación", request, errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception exception, WebRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al procesar la solicitud", request, List.of());
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, String message, WebRequest request, List<String> errors) {
        ApiErrorResponse body = new ApiErrorResponse(
            status.value(),
            message,
            request.getDescription(false).replace("uri=", ""),
            OffsetDateTime.now(),
            errors == null || errors.isEmpty() ? null : errors
        );

        return ResponseEntity.status(status).body(body);
    }

    private String formatFieldError(FieldError fieldError) {
        if (fieldError.getDefaultMessage() != null && !fieldError.getDefaultMessage().isBlank()) {
            return fieldError.getDefaultMessage();
        }

        return fieldError.getField() + " es inválido";
    }
}