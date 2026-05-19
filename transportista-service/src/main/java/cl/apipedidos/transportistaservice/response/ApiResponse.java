package cl.apipedidos.transportistaservice.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String mensaje;
    private T data;
    private boolean exitoso;
    private LocalDateTime timestamp;

    public static <T> ApiResponse<T> success(String mensaje, T data) {
        return new ApiResponse<>(mensaje, data, true, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String mensaje) {
        return new ApiResponse<>(mensaje, null, false, LocalDateTime.now());
    }
}
