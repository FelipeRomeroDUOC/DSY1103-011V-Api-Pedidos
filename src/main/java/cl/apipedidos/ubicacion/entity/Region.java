package cl.apipedidos.ubicacion.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "regiones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Region {

    @Id
    @Column(name = "id_region", nullable = false, length = 2, updatable = false)
    private String idRegion;

    @NotBlank
    @Size(max = 120)
    @Column(name = "nombre_region", nullable = false, unique = true, length = 120)
    private String nombreRegion;

    @JsonIgnore
    @OneToMany(mappedBy = "region")
    private List<Comuna> comunas = new ArrayList<>();

    public Region(String idRegion, String nombreRegion) {
        this(idRegion, nombreRegion, new ArrayList<>());
    }

    @PrePersist
    @PreUpdate
    public void normalize() {
        if (idRegion != null) {
            idRegion = idRegion.trim();
        }
        if (nombreRegion != null) {
            nombreRegion = nombreRegion.trim();
        }
    }
}