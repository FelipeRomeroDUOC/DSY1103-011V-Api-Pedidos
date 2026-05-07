package cl.apipedidos.ubicacion.entity;

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
@Table(name = "provincias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Provincia {

    @Id
    @Column(name = "id_provincia", nullable = false, length = 3, updatable = false)
    private String idProvincia;

    @NotBlank
    @Size(max = 120)
    @Column(name = "nombre_provincia", nullable = false, unique = true, length = 120)
    private String nombreProvincia;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_region", nullable = false)
    private Region region;

    @JsonIgnore
    @OneToMany(mappedBy = "provincia")
    private List<Comuna> comunas = new ArrayList<>();

    public Provincia(String idProvincia, String nombreProvincia, Region region) {
        this(idProvincia, nombreProvincia, region, new ArrayList<>());
    }

    @PrePersist
    @PreUpdate
    public void normalize() {
        if (idProvincia != null) {
            idProvincia = idProvincia.trim();
        }
        if (nombreProvincia != null) {
            nombreProvincia = nombreProvincia.trim();
        }
    }
}