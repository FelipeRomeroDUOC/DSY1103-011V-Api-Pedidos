package cl.apipedidos.fabricacion.repository;

import cl.apipedidos.fabricacion.entity.EstadoFabricacion;
import cl.apipedidos.fabricacion.entity.HistorialFabricacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HistorialFabricacionRepository extends JpaRepository<HistorialFabricacion, Long> {

    List<HistorialFabricacion> findByOrdenFabricacionId(Long ordenId);

    List<HistorialFabricacion> findByOrdenFabricacionIdOrderByFechaCambioDesc(Long ordenId);

    List<HistorialFabricacion> findByEstadoNuevo(EstadoFabricacion estado);

    @Query("SELECT h FROM HistorialFabricacion h WHERE h.ordenFabricacion.id IN :ordenIds ORDER BY h.fechaCambio DESC")
    List<HistorialFabricacion> findHistorialForOrdenes(@Param("ordenIds") List<Long> ordenIds);
}
