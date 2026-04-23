package cl.apipedidos.cliente.repository;

import cl.apipedidos.cliente.entity.Cliente;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByNombreClIgnoreCase(String nombreCl);

    List<Cliente> findByComuna_NombreComunaIgnoreCase(String nombreComuna);

    boolean existsByNombreClIgnoreCase(String nombreCl);

    boolean existsByNombreClIgnoreCaseAndIdClienteNot(String nombreCl, Long idCliente);

    boolean existsByRutCl(Long rutCl);

    boolean existsByRutClAndIdClienteNot(Long rutCl, Long idCliente);

    @Query("select coalesce(max(c.idCliente), 0) from Cliente c")
    long findMaxIdCliente();
}
