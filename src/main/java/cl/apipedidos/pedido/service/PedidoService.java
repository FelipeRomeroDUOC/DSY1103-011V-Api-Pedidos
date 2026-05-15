package cl.apipedidos.pedido.service;

import cl.apipedidos.pedido.client.ClienteServiceClient;
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
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final ClienteServiceClient clienteServiceClient;

    public PedidoService(
        PedidoRepository pedidoRepository,
        ItemPedidoRepository itemPedidoRepository,
        ClienteServiceClient clienteServiceClient
    ) {
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.clienteServiceClient = clienteServiceClient;
    }

    public PedidoDTO crearPedido(CreatePedidoRequest request) {
        if (pedidoRepository.existsByNumeroPedido(request.numeroPedido())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El pedido ya existe: " + request.numeroPedido());
        }

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
        return toDTO(pedidoRepository.save(pedido));
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
    public List<PedidoDTO> listarPedidos(EstadoPedido estado, TipoDespacho tipoDespacho, Long clienteId) {
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
        validarTransicionEstado(pedido.getEstado(), request.nuevoEstado());
        pedido.setEstado(request.nuevoEstado());
        return toDTO(pedidoRepository.save(pedido));
    }

    public void eliminarPedido(Long id) {
        Pedido pedido = buscarPedidoPorId(id);

        if (pedido.getEstado() == EstadoPedido.DESPACHADO || pedido.getEstado() == EstadoPedido.ENTREGADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede eliminar un pedido despachado o entregado");
        }

        pedidoRepository.delete(pedido);
    }

    private Pedido buscarPedidoPorId(Long id) {
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