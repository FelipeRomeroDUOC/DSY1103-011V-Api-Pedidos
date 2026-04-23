package cl.apipedidos.ubicacion.entity;

import cl.apipedidos.cliente.entity.Cliente;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "comunas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Comuna {

    @Id
    @Column(name = "id_comuna", nullable = false, length = 5, updatable = false)
    private String idComuna;

    @NotBlank
    @Size(max = 120)
    @Column(name = "nombre_comuna", nullable = false, unique = true, length = 120)
    private String nombreComuna;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_region", nullable = false)
    private Region region;

    @JsonIgnore
    @OneToMany(mappedBy = "comuna")
    private List<Cliente> clientes = new ArrayList<>();

    public Comuna(String idComuna, String nombreComuna, Region region) {
        this(idComuna, nombreComuna, region, new ArrayList<>());
    }

    @PrePersist
    @PreUpdate
    public void normalize() {
        if (idComuna != null) {
            idComuna = idComuna.trim();
        }
        if (nombreComuna != null) {
            nombreComuna = nombreComuna.trim();
        }
    }
}