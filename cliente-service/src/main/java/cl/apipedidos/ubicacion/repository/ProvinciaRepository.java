package cl.apipedidos.ubicacion.repository;

import cl.apipedidos.ubicacion.entity.Provincia;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProvinciaRepository extends JpaRepository<Provincia, String> {

    List<Provincia> findAllByRegion_IdRegionOrderByNombreProvinciaAsc(String idRegion);

    Optional<Provincia> findByNombreProvinciaIgnoreCase(String nombreProvincia);

    boolean existsByNombreProvinciaIgnoreCase(String nombreProvincia);
}