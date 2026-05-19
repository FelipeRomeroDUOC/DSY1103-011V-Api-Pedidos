package cl.apipedidos.estado.dto;

import java.time.LocalDateTime;

public record ApiResponse<T>(
        String mensaje,
        T data,
        boolean exitoso,
        LocalDateTime timestamp
) {
    public static <T> ApiResponse<T> success(String mensaje, T data) {
        return new ApiResponse<>(mensaje, data, true, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String mensaje) {
        return new ApiResponse<>(mensaje, null, false, LocalDateTime.now());
    }
}
