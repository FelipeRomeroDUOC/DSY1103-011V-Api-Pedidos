package cl.apipedidos.despacho.service;

import cl.apipedidos.despacho.client.PedidoFeignClient;
import cl.apipedidos.despacho.client.PedidoResponseDTO;
import cl.apipedidos.despacho.dto.ApiResponse;
import cl.apipedidos.despacho.dto.DespachoRequestDTO;
import cl.apipedidos.despacho.dto.DespachoResponseDTO;
import cl.apipedidos.despacho.dto.DespachoUpdateDTO;
import cl.apipedidos.despacho.entity.Despacho;
import cl.apipedidos.despacho.entity.TipoDespacho;
import cl.apipedidos.despacho.repository.DespachoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DespachoServiceImpl implements DespachoService {

    private final DespachoRepository despachoRepository;
    private final PedidoFeignClient pedidoClient;

    @Override
    @Transactional
    public DespachoResponseDTO registrar(DespachoRequestDTO request) {
        log.info("Registrando despacho para pedidoId={}", request.pedidoId());

        if (despachoRepository.existsByPedidoId(request.pedidoId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El pedido ya tiene un despacho registrado");
        }

        if (request.tipoDespacho() == TipoDespacho.REGION && 
            (request.transportista() == null || request.transportista().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El transportista es obligatorio para despachos a REGION");
        }

        try {
            ApiResponse<PedidoResponseDTO> response = pedidoClient.obtenerPedido(request.pedidoId());
            PedidoResponseDTO pedido = response.data();

            if (!"LISTO".equals(pedido.estado())) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, 
                        "El pedido no está en estado LISTO. Estado actual: " + pedido.estado());
            }
        } catch (feign.FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado en pedido-service");
        } catch (feign.FeignException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al comunicar con pedido-service: " + e.getMessage());
        }

        Despacho despacho = new Despacho();
        despacho.setPedidoId(request.pedidoId());
        despacho.setTipoDespacho(request.tipoDespacho());
        despacho.setTransportista(request.transportista());
        despacho.setFechaDespacho(request.fechaDespacho());
        despacho.setTrackingCode(request.trackingCode());

        Despacho guardado = despachoRepository.save(despacho);
        log.info("Despacho registrado con id={}", guardado.getId());

        return toDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DespachoResponseDTO> listar(TipoDespacho tipo) {
        List<Despacho> despachos = (tipo != null) 
                ? despachoRepository.findByTipoDespacho(tipo)
                : despachoRepository.findAll();

        return despachos.stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DespachoResponseDTO obtenerPorPedidoId(Long pedidoId) {
        Despacho despacho = despachoRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe despacho para el pedidoId: " + pedidoId));
        return toDTO(despacho);
    }

    @Override
    @Transactional
    public DespachoResponseDTO actualizar(Long id, DespachoUpdateDTO request) {
        log.info("Actualizando despacho id={}", id);

        Despacho despacho = despachoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Despacho no encontrado: " + id));

        if (request.transportista() != null) despacho.setTransportista(request.transportista());
        if (request.fechaDespacho() != null) despacho.setFechaDespacho(request.fechaDespacho());
        if (request.trackingCode() != null) despacho.setTrackingCode(request.trackingCode());

        Despacho actualizado = despachoRepository.save(despacho);
        log.info("Despacho actualizado id={}", actualizado.getId());

        return toDTO(actualizado);
    }

    private DespachoResponseDTO toDTO(Despacho despacho) {
        return new DespachoResponseDTO(
                despacho.getId(),
                despacho.getPedidoId(),
                despacho.getTipoDespacho(),
                despacho.getTransportista(),
                despacho.getFechaDespacho(),
                despacho.getTrackingCode()
        );
    }
}
