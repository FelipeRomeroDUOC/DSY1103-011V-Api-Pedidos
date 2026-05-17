package cl.apipedidos.fabricacion.repository;

import cl.apipedidos.fabricacion.entity.EstadoFabricacion;
import cl.apipedidos.fabricacion.entity.OrdenFabricacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrdenFabricacionRepository extends JpaRepository<OrdenFabricacion, Long>, JpaSpecificationExecutor<OrdenFabricacion> {

    Optional<OrdenFabricacion> findByPedidoId(Long pedidoId);

    List<OrdenFabricacion> findByEstadoFabricacion(EstadoFabricacion estado);

    List<OrdenFabricacion> findByEstadoFabricacionAndFechaInicioBetween(EstadoFabricacion estado, LocalDateTime inicio, LocalDateTime fin);

    long countByEstadoFabricacion(EstadoFabricacion estado);

    List<OrdenFabricacion> findAllByOrderByFechaInicioDesc();

    @Query("SELECT o FROM OrdenFabricacion o WHERE o.estadoFabricacion = 'EN_PROCESO' AND o.fechaInicio < :fecha")
    List<OrdenFabricacion> findOrdenesEnProcesoPorMasDias(@Param("fecha") LocalDateTime fecha);
}
