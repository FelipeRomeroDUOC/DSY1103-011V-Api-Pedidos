package cl.apipedidos.fabricacion.service;

import cl.apipedidos.fabricacion.client.PedidoServiceClient;
import cl.apipedidos.fabricacion.dto.OrdenFabricacionRequest;
import cl.apipedidos.fabricacion.dto.OrdenFabricacionResponse;
import cl.apipedidos.fabricacion.dto.UpdateEstadoFabricacionRequest;
import cl.apipedidos.fabricacion.entity.EstadoFabricacion;
import cl.apipedidos.fabricacion.entity.OrdenFabricacion;
import cl.apipedidos.fabricacion.exception.FabricacionException;
import cl.apipedidos.fabricacion.exception.PedidoNoEncontradoException;
import cl.apipedidos.fabricacion.repository.OrdenFabricacionRepository;
import cl.apipedidos.fabricacion.repository.HistorialFabricacionRepository;
import cl.apipedidos.fabricacion.entity.HistorialFabricacion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
public class OrdenFabricacionService {

    private final OrdenFabricacionRepository ordenRepository;
    private final HistorialFabricacionRepository historialRepository;
    private final PedidoServiceClient pedidoServiceClient;

    @Transactional
    public OrdenFabricacionResponse crearOrden(OrdenFabricacionRequest request) {
        // validar existencia pedido
        try {
            pedidoServiceClient.validarExistencia(request.getNumeroPedido());
        } catch (PedidoNoEncontradoException e) {
            throw e;
        } catch (Exception e) {
            throw new FabricacionException("Error al validar pedido", e);
        }

        // validar duplicado
        if (ordenRepository.findByPedidoId(request.getNumeroPedido()).isPresent()) {
            throw new FabricacionException("Ya existe una orden para el pedido: " + request.getNumeroPedido());
        }

        OrdenFabricacion orden = new OrdenFabricacion();
        orden.setPedidoId(request.getNumeroPedido());
        orden.setUsuarioResponsable(request.getUsuarioResponsable());
        orden.setDescripcionEstado(request.getDescripcionEstado());
        orden.setEstadoFabricacion(EstadoFabricacion.EN_PROCESO);
        orden.setFechaInicio(LocalDateTime.now());

        OrdenFabricacion saved = ordenRepository.save(orden);

        // registrar historial inicial
        HistorialFabricacion h = new HistorialFabricacion();
        h.setOrdenFabricacion(saved);
        h.setEstadoAnterior(null);
        h.setEstadoNuevo(EstadoFabricacion.EN_PROCESO);
        h.setUsuarioId(request.getUsuarioResponsable());
        h.setMotivo("Inicio de fabricación");
        historialRepository.save(h);

        pedidoServiceClient.notificarPedidoEnFabricacion(request.getNumeroPedido());

        return mapToResponse(saved);
    }

    public OrdenFabricacionResponse obtenerOrden(Long id) {
        OrdenFabricacion orden = ordenRepository.findById(id)
            .orElseThrow(() -> new FabricacionException("Orden no encontrada: " + id));
        return mapToResponse(orden);
    }

    @Transactional
    public OrdenFabricacionResponse actualizarEstado(Long id, UpdateEstadoFabricacionRequest request) {
        OrdenFabricacion orden = ordenRepository.findById(id)
            .orElseThrow(() -> new FabricacionException("Orden no encontrada: " + id));

        EstadoFabricacion anterior = orden.getEstadoFabricacion();
        orden.setEstadoFabricacion(request.getNuevoEstado());
        if (request.getNuevoEstado() == EstadoFabricacion.TERMINADO) {
            orden.setFechaFin(LocalDateTime.now());
            // notificar pedido
            pedidoServiceClient.notificarPedidoListo(orden.getPedidoId());
        }

        OrdenFabricacion updated = ordenRepository.save(orden);
        // registrar historial de cambio
        HistorialFabricacion h = new HistorialFabricacion();
        h.setOrdenFabricacion(updated);
        h.setEstadoAnterior(anterior);
        h.setEstadoNuevo(request.getNuevoEstado());
        h.setUsuarioId(request.getUsuarioId() != null ? request.getUsuarioId() : orden.getUsuarioResponsable());
        h.setMotivo(request.getMotivo() != null ? request.getMotivo() : "Cambio de estado");
        historialRepository.save(h);

        return mapToResponse(updated);
    }

    private OrdenFabricacionResponse mapToResponse(OrdenFabricacion orden) {
        OrdenFabricacionResponse resp = new OrdenFabricacionResponse();
        resp.setId(orden.getId());
        resp.setNumeroPedido(orden.getPedidoId());
        resp.setEstadoFabricacion(orden.getEstadoFabricacion());
        resp.setFechaInicio(orden.getFechaInicio());
        resp.setFechaFin(orden.getFechaFin());
        resp.setFechaCreacion(orden.getFechaCreacion());
        resp.setFechaActualizacion(orden.getFechaActualizacion());
        resp.setDescripcionEstado(orden.getDescripcionEstado());
        resp.setUsuarioResponsable(orden.getUsuarioResponsable());
        return resp;
    }
}

