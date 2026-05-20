package cl.apipedidos.pedido.service;

import cl.apipedidos.pedido.client.CambioEstadoRequestDTO;
import cl.apipedidos.pedido.client.ClienteServiceClient;
import cl.apipedidos.pedido.client.EstadoFeignClient;
import cl.apipedidos.pedido.dto.CreateItemRequest;
import cl.apipedidos.pedido.dto.CreatePedidoRequest;
import cl.apipedidos.pedido.dto.ItemPedidoDTO;
import cl.apipedidos.pedido.dto.PedidoDTO;
import cl.apipedidos.pedido.dto.UpdateEstadoRequest;
import cl.apipedidos.pedido.entity.EstadoPedido;
import cl.apipedidos.pedido.entity.ItemPedido;
import cl.apipedidos.pedido.entity.Pedido;
import cl.apipedidos.pedido.entity.TipoDespacho;
import cl.apipedidos.pedido.repository.ItemPedidoRepository;
import cl.apipedidos.pedido.repository.PedidoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SuppressWarnings("null")
@Service
@Slf4j
@Transactional
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final ClienteServiceClient clienteServiceClient;
    private final EstadoFeignClient estadoFeignClient;

    public PedidoService(
        PedidoRepository pedidoRepository,
        ItemPedidoRepository itemPedidoRepository,
        ClienteServiceClient clienteServiceClient,
        EstadoFeignClient estadoFeignClient
    ) {
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.clienteServiceClient = clienteServiceClient;
        this.estadoFeignClient = estadoFeignClient;
    }

    public PedidoDTO crearPedido(CreatePedidoRequest request) {
        log.info("Creando pedido numero={}, clienteId={}, tipoDespacho={}, items={}",
            request.numeroPedido(),
            request.clienteId(),
            request.tipoDespacho(),
            request.items() == null ? 0 : request.items().size());

        if (pedidoRepository.existsByNumeroPedido(request.numeroPedido())) {
            log.warn("Pedido duplicado detectado para numero={}", request.numeroPedido());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El pedido ya existe: " + request.numeroPedido());
        }

        log.debug("Validando clienteId={} antes de persistir pedido numero={}", request.clienteId(), request.numeroPedido());
        clienteServiceClient.validarCliente(request.clienteId());

        Pedido pedido = new Pedido();
        pedido.setNumeroPedido(request.numeroPedido().trim());
        pedido.setClienteId(request.clienteId());
        pedido.setTipoDespacho(request.tipoDespacho());
        pedido.setEstado(EstadoPedido.PENDIENTE);

        BigDecimal montoTotal = BigDecimal.ZERO;
        for (CreateItemRequest itemRequest : request.items()) {
            ItemPedido item = new ItemPedido();
            item.setProductoId(itemRequest.productoId());
            item.setCantidad(itemRequest.cantidad());
            item.setPrecioUnitario(itemRequest.precioUnitario());
            item.recalcularSubtotal();
            pedido.agregarItem(item);
            montoTotal = montoTotal.add(item.getSubtotal());
        }

        pedido.setMonto(montoTotal);
        Pedido pedidoGuardado = pedidoRepository.save(pedido);
        log.info("Pedido creado correctamente id={}, numero={}, monto={}, clienteId={}",
            pedidoGuardado.getId(), pedidoGuardado.getNumeroPedido(), pedidoGuardado.getMonto(), pedidoGuardado.getClienteId());
        return toDTO(pedidoGuardado);
    }

    @Transactional(readOnly = true)
    public PedidoDTO obtenerPedido(Long id) {
        return toDTO(buscarPedidoPorId(id));
    }

    @Transactional(readOnly = true)
    public PedidoDTO obtenerPedidoPorNumero(String numeroPedido) {
        Pedido pedido = pedidoRepository.findByNumeroPedido(numeroPedido)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado: " + numeroPedido));
        return toDTO(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoDTO> listarPedidos(EstadoPedido estado, TipoDespacho tipoDespacho, Long clienteId, LocalDate desde, LocalDate hasta) {
        List<Pedido> pedidos = pedidoRepository.findAll();

        if (clienteId != null) {
            pedidos = pedidos.stream().filter(pedido -> clienteId.equals(pedido.getClienteId())).toList();
        }

        if (estado != null) {
            pedidos = pedidos.stream().filter(pedido -> estado == pedido.getEstado()).toList();
        }

        if (tipoDespacho != null) {
            pedidos = pedidos.stream().filter(pedido -> tipoDespacho == pedido.getTipoDespacho()).toList();
        }
        
        if (desde != null) {
            pedidos = pedidos.stream().filter(pedido -> !pedido.getFechaCreacion().toLocalDate().isBefore(desde)).toList();
        }
        
        if (hasta != null) {
            pedidos = pedidos.stream().filter(pedido -> !pedido.getFechaCreacion().toLocalDate().isAfter(hasta)).toList();
        }

        return pedidos.stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoDTO> obtenerHistorialPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteIdOrderByFechaCreacionDesc(clienteId).stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<ItemPedidoDTO> obtenerItemsPedido(Long pedidoId) {
        if (!pedidoRepository.existsById(pedidoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado: " + pedidoId);
        }

        return itemPedidoRepository.findByPedidoId(pedidoId).stream().map(this::toDTO).toList();
    }

    public PedidoDTO actualizarEstadoPedido(Long id, UpdateEstadoRequest request) {
        Pedido pedido = buscarPedidoPorId(id);
        EstadoPedido estadoAnterior = pedido.getEstado();
        validarTransicionEstado(estadoAnterior, request.nuevoEstado());
        pedido.setEstado(request.nuevoEstado());
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        try {
            CambioEstadoRequestDTO cambio = new CambioEstadoRequestDTO(
                pedido.getId(),
                estadoAnterior.name(),
                request.nuevoEstado().name(),
                null
            );
            estadoFeignClient.registrarCambio(cambio);
        } catch (Exception e) {
            log.error("Error al notificar cambio de estado a estado-service: {}", e.getMessage());
        }

        return toDTO(pedidoGuardado);
    }

    public void eliminarPedido(Long id) {
        Pedido pedido = buscarPedidoPorId(id);

        if (pedido.getEstado() == EstadoPedido.DESPACHADO || pedido.getEstado() == EstadoPedido.ENTREGADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede eliminar un pedido despachado o entregado");
        }

        pedidoRepository.delete(pedido);
        log.info("Pedido eliminado id={}, numero={}", pedido.getId(), pedido.getNumeroPedido());
    }

    private Pedido buscarPedidoPorId(Long id) {
        log.debug("Buscando pedido por id={}", id);
        return pedidoRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado: " + id));
    }

    private void validarTransicionEstado(EstadoPedido estadoActual, EstadoPedido estadoNuevo) {
        boolean transicionValida = switch (estadoActual) {
            case PENDIENTE -> estadoNuevo == EstadoPedido.EN_FABRICACION || estadoNuevo == EstadoPedido.CANCELADO;
            case EN_FABRICACION -> estadoNuevo == EstadoPedido.LISTO || estadoNuevo == EstadoPedido.CANCELADO;
            case LISTO -> estadoNuevo == EstadoPedido.DESPACHADO;
            case DESPACHADO -> estadoNuevo == EstadoPedido.ENTREGADO;
            case CANCELADO, ENTREGADO -> false;
        };

        if (!transicionValida) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede cambiar de " + estadoActual + " a " + estadoNuevo);
        }
    }

    private PedidoDTO toDTO(Pedido pedido) {
        return new PedidoDTO(
            pedido.getId(),
            pedido.getNumeroPedido(),
            pedido.getClienteId(),
            pedido.getEstado(),
            pedido.getMonto(),
            pedido.getTipoDespacho(),
            pedido.getFechaCreacion(),
            pedido.getFechaActualizacion(),
            pedido.getItems().stream().map(this::toDTO).toList()
        );
    }

    private ItemPedidoDTO toDTO(ItemPedido itemPedido) {
        return new ItemPedidoDTO(
            itemPedido.getId(),
            itemPedido.getProductoId(),
            itemPedido.getCantidad(),
            itemPedido.getPrecioUnitario(),
            itemPedido.getSubtotal()
        );
    }
}
