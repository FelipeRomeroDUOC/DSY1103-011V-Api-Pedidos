package cl.apipedidos.authservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String password;        // hash BCrypt — nunca texto plano

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Rol rol = Rol.ENCARGADO_PEDIDOS;

    @Column(nullable = false)
    private boolean activo = true;  // false → 401 aunque el token sea válido

    public enum Rol {
        ADMIN,
        ENCARGADO_PEDIDOS,
        ENCARGADO_DESPACHO,
        COMERCIAL
    }
}
