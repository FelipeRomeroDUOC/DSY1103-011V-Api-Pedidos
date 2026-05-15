package cl.apipedidos.pedido.controller;

import cl.apipedidos.pedido.dto.CreatePedidoRequest;
import cl.apipedidos.pedido.dto.ItemPedidoDTO;
import cl.apipedidos.pedido.dto.PedidoDTO;
import cl.apipedidos.pedido.dto.UpdateEstadoRequest;
import cl.apipedidos.pedido.entity.EstadoPedido;
import cl.apipedidos.pedido.entity.TipoDespacho;
import cl.apipedidos.pedido.service.PedidoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<PedidoDTO> crearPedido(@Valid @RequestBody CreatePedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crearPedido(request));
    }

    @GetMapping
    public ResponseEntity<List<PedidoDTO>> listarPedidos(
        @RequestParam(required = false) EstadoPedido estado,
        @RequestParam(required = false) TipoDespacho tipo,
        @RequestParam(required = false) Long clienteId
    ) {
        return ResponseEntity.ok(pedidoService.listarPedidos(estado, tipo, clienteId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoDTO> obtenerPedido(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.obtenerPedido(id));
    }

    @GetMapping("/numero/{numeroPedido}")
    public ResponseEntity<PedidoDTO> obtenerPedidoPorNumero(@PathVariable String numeroPedido) {
        return ResponseEntity.ok(pedidoService.obtenerPedidoPorNumero(numeroPedido));
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<List<ItemPedidoDTO>> obtenerItemsPedido(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.obtenerItemsPedido(id));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<PedidoDTO> actualizarEstado(
        @PathVariable Long id,
        @Valid @RequestBody UpdateEstadoRequest request
    ) {
        return ResponseEntity.ok(pedidoService.actualizarEstadoPedido(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPedido(@PathVariable Long id) {
        pedidoService.eliminarPedido(id);
        return ResponseEntity.noContent().build();
    }
}