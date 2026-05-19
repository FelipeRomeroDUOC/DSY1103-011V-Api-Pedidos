package cl.apipedidos.logservice.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    private String mensaje;
    private T data;
    private boolean exitoso;
    private LocalDateTime timestamp;

    public static <T> ApiResponse<T> success(String mensaje, T data) {
        return ApiResponse.<T>builder()
            .mensaje(mensaje)
            .data(data)
            .exitoso(true)
            .timestamp(LocalDateTime.now())
            .build();
    }

    public static <T> ApiResponse<T> error(String mensaje) {
        return ApiResponse.<T>builder()
            .mensaje(mensaje)
            .data(null)
            .exitoso(false)
            .timestamp(LocalDateTime.now())
            .build();
    }

    public static <T> ApiResponse<T> error(String mensaje, T data) {
        return ApiResponse.<T>builder()
            .mensaje(mensaje)
            .data(data)
            .exitoso(false)
            .timestamp(LocalDateTime.now())
            .build();
    }
}