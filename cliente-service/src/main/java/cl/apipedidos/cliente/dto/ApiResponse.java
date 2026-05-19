package cl.apipedidos.cliente.dto;

import java.time.LocalDateTime;

public class ApiResponse<T> {
    private String mensaje;
    private T data;
    private boolean exitoso;
    private LocalDateTime timestamp;

    public ApiResponse() {
    }

    public ApiResponse(String mensaje, T data, boolean exitoso, LocalDateTime timestamp) {
        this.mensaje = mensaje;
        this.data = data;
        this.exitoso = exitoso;
        this.timestamp = timestamp;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isExitoso() {
        return exitoso;
    }

    public void setExitoso(boolean exitoso) {
        this.exitoso = exitoso;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public static <T> ApiResponse<T> success(String mensaje, T data) {
        return new ApiResponse<>(mensaje, data, true, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String mensaje) {
        return new ApiResponse<>(mensaje, null, false, LocalDateTime.now());
    }
}
