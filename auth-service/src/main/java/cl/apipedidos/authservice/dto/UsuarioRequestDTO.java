package cl.apipedidos.authservice.dto;

import cl.apipedidos.authservice.entity.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequestDTO {

    @NotBlank
    private String nombre;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;  // texto plano → se hashea en el servicio

    private Usuario.Rol rol;  // opcional; default = ENCARGADO_PEDIDOS

    private Boolean activo;   // opcional; default = true
}
