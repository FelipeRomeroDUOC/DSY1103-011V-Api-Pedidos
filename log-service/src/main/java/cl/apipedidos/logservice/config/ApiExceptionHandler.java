package cl.apipedidos.logservice.config;

import cl.apipedidos.logservice.dto.ApiResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Object>> handleResponseStatusException(ResponseStatusException exception) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        String message = exception.getReason() != null && !exception.getReason().isBlank()
            ? exception.getReason()
            : status.getReasonPhrase();

        log.warn("Request handled with status {}: {}", status.value(), message, exception);
        return buildResponse(status, message, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        List<String> errors = exception.getBindingResult().getFieldErrors().stream()
            .map(this::formatFieldError)
            .toList();

        log.warn("Validation failed: {}", errors, exception);
        return buildResponse(HttpStatus.BAD_REQUEST, "La solicitud contiene errores de validación", errors);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ApiResponse<Object>> handleBadRequestException(Exception exception) {
        log.warn("Bad request received", exception);
        return buildResponse(HttpStatus.BAD_REQUEST, "Los parámetros de la solicitud son inválidos", List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception exception) {
        log.error("Unhandled exception", exception);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al procesar la solicitud", List.of());
    }

    private ResponseEntity<ApiResponse<Object>> buildResponse(HttpStatus status, String message, List<String> errors) {
        ApiResponse<Object> body = errors == null || errors.isEmpty()
            ? ApiResponse.error(message)
            : ApiResponse.error(message, errors);

        body.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(status).body(body);
    }

    private String formatFieldError(FieldError fieldError) {
        String defaultMessage = fieldError.getDefaultMessage();
        if (defaultMessage != null && !defaultMessage.isBlank()) {
            return defaultMessage;
        }

        return fieldError.getField() + " es inválido";
    }
}