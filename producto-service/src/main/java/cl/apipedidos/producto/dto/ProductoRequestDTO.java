package cl.apipedidos.producto.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductoRequestDTO(
    @NotBlank(message = "El nombre es obligatorio") @Size(max = 150) String nombre,
    @Size(max = 500) String descripcion,
    @NotBlank(message = "La categoría es obligatoria") @Size(max = 100) String categoria,
    @NotNull(message = "El precio base es obligatorio") @DecimalMin(value = "0.0", message = "El precio base debe ser >= 0") Double precioBase
) {
}
