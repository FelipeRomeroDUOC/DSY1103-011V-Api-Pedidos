package cl.apipedidos.pedido.controller;

import cl.apipedidos.pedido.dto.CreatePedidoRequest;
import cl.apipedidos.pedido.dto.ItemPedidoDTO;
import cl.apipedidos.pedido.dto.PedidoDTO;
import cl.apipedidos.pedido.dto.UpdateEstadoRequest;
import cl.apipedidos.pedido.entity.EstadoPedido;
import cl.apipedidos.pedido.entity.TipoDespacho;
import cl.apipedidos.pedido.service.PedidoService;
import cl.apipedidos.pedido.dto.ApiResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<ApiResponse<PedidoDTO>> crearPedido(@Valid @RequestBody CreatePedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Pedido creado exitosamente", pedidoService.crearPedido(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PedidoDTO>>> listarPedidos(
        @RequestParam(required = false) EstadoPedido estado,
        @RequestParam(required = false) TipoDespacho tipo,
        @RequestParam(required = false) Long clienteId,
        @RequestParam(required = false) LocalDate desde,
        @RequestParam(required = false) LocalDate hasta
    ) {
        return ResponseEntity.ok(ApiResponse.success("Listado de pedidos", pedidoService.listarPedidos(estado, tipo, clienteId, desde, hasta)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PedidoDTO>> obtenerPedido(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Pedido encontrado", pedidoService.obtenerPedido(id)));
    }

    @GetMapping("/numero/{numeroPedido}")
    public ResponseEntity<ApiResponse<PedidoDTO>> obtenerPedidoPorNumero(@PathVariable String numeroPedido) {
        return ResponseEntity.ok(ApiResponse.success("Pedido encontrado", pedidoService.obtenerPedidoPorNumero(numeroPedido)));
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<ApiResponse<List<ItemPedidoDTO>>> obtenerItemsPedido(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Listado de items", pedidoService.obtenerItemsPedido(id)));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<PedidoDTO>> actualizarEstado(
        @PathVariable Long id,
        @Valid @RequestBody UpdateEstadoRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Estado de pedido actualizado", pedidoService.actualizarEstadoPedido(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarPedido(@PathVariable Long id) {
        pedidoService.eliminarPedido(id);
        return ResponseEntity.ok(ApiResponse.success("Pedido eliminado exitosamente", null));
    }
}