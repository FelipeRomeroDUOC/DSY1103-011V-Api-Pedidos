package cl.apipedidos.http.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteCreateRequestDTO(
    @NotBlank @Size(max = 150) String nombreCl,
    @NotNull @Min(1_000_000L) @Max(999_999_999L) Long rutCl,
    @NotBlank @Size(min = 1, max = 2) @Pattern(regexp = "^[0-9Kk]{1,2}$") String divCl,
    @NotBlank @Size(max = 200) String direccionCl,
    @NotBlank @Email @Size(max = 254) String emailCl,
    @NotBlank @Size(max = 30) String telefonoCl,
    @NotBlank @Size(max = 120) String comuna
) {
}