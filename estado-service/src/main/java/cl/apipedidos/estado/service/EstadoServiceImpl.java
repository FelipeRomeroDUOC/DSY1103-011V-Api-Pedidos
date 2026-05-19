package cl.apipedidos.estado.service;

import cl.apipedidos.estado.dto.CambioEstadoRequestDTO;
import cl.apipedidos.estado.dto.CambioEstadoResponseDTO;
import cl.apipedidos.estado.entity.CambioEstado;
import cl.apipedidos.estado.repository.CambioEstadoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EstadoServiceImpl implements EstadoService {

    private final CambioEstadoRepository cambioEstadoRepository;

    @Override
    @Transactional
    public CambioEstadoResponseDTO registrarCambio(CambioEstadoRequestDTO request) {
        log.info("Registrando cambio de estado para pedidoId={}: {} -> {}", 
                request.pedidoId(), request.estadoAnterior(), request.estadoNuevo());

        CambioEstado cambio = new CambioEstado();
        cambio.setPedidoId(request.pedidoId());
        cambio.setEstadoAnterior(request.estadoAnterior());
        cambio.setEstadoNuevo(request.estadoNuevo());
        cambio.setFechaCambio(LocalDateTime.now());
        cambio.setUsuarioId(request.usuarioId());

        CambioEstado guardado = cambioEstadoRepository.save(cambio);
        log.info("Cambio de estado registrado con id={}", guardado.getId());

        return toDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CambioEstadoResponseDTO> obtenerHistorialPorPedido(Long pedidoId) {
        log.info("Obteniendo historial de estados para pedidoId={}", pedidoId);
        List<CambioEstado> historial = cambioEstadoRepository.findByPedidoIdOrderByFechaCambioAsc(pedidoId);
        return historial.stream().map(this::toDTO).toList();
    }

    private CambioEstadoResponseDTO toDTO(CambioEstado cambio) {
        return new CambioEstadoResponseDTO(
                cambio.getId(),
                cambio.getPedidoId(),
                cambio.getEstadoAnterior(),
                cambio.getEstadoNuevo(),
                cambio.getFechaCambio(),
                cambio.getUsuarioId()
        );
    }
}
