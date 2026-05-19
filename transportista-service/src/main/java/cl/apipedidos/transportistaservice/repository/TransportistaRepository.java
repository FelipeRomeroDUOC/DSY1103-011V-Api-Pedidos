package cl.apipedidos.transportistaservice.repository;

import cl.apipedidos.transportistaservice.model.Transportista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransportistaRepository extends JpaRepository<Transportista, Long> {
    boolean existsByCodigoInterno(String codigoInterno);
    List<Transportista> findByActivoTrue();
}
