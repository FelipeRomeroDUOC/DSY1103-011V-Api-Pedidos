package cl.apipedidos.producto.dto;

public record ProductoResponseDTO(
    Long id,
    String nombre,
    String descripcion,
    String categoria,
    Double precioBase,
    boolean activo
) {
}
