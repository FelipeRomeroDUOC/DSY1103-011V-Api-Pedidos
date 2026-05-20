package cl.apipedidos.authservice.dto;

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
public class LoginResponseDTO {

    private String token;        // JWT firmado
    private String tipo;         // siempre "Bearer"
    private String email;
    private String rol;
    private long   expiracion;   // timestamp Unix de expiración
}
