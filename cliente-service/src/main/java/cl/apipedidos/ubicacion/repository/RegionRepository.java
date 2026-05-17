package cl.apipedidos.ubicacion.repository;

import cl.apipedidos.ubicacion.entity.Region;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionRepository extends JpaRepository<Region, String> {

    List<Region> findAllByOrderByIdRegionAsc();

    Optional<Region> findByNombreRegionIgnoreCase(String nombreRegion);

    boolean existsByNombreRegionIgnoreCase(String nombreRegion);
}