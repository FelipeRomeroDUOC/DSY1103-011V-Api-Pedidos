package cl.apipedidos.ubicacion.repository;

import cl.apipedidos.ubicacion.entity.Comuna;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComunaRepository extends JpaRepository<Comuna, String> {

    List<Comuna> findByRegion_IdRegionOrderByNombreComunaAsc(String idRegion);

    Optional<Comuna> findByNombreComunaIgnoreCase(String nombreComuna);

    List<Comuna> findByRegion_NombreRegionIgnoreCase(String nombreRegion);

    boolean existsByNombreComunaIgnoreCase(String nombreComuna);
}