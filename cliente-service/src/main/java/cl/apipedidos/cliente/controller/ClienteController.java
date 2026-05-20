package cl.apipedidos.cliente.controller;

import cl.apipedidos.cliente.dto.ClienteCreateRequestDTO;
import cl.apipedidos.cliente.dto.ClienteResponseDTO;
import cl.apipedidos.cliente.dto.ClienteUpdateRequestDTO;
import cl.apipedidos.cliente.entity.Cliente;
import cl.apipedidos.cliente.service.ClienteService;
import cl.apipedidos.cliente.dto.ApiResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings("null")
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Slf4j
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    public ResponseEntity<ApiResponse<ClienteResponseDTO>> crearCliente(@Valid @RequestBody ClienteCreateRequestDTO request) {
        log.info("POST /api/clientes nombre={} rut={} comuna={}", request.nombreCl(), request.rutCl(), request.comuna());
        Cliente clienteCreado = clienteService.crear(request);
        return ResponseEntity.created(URI.create("/api/clientes/" + clienteCreado.getIdCliente()))
            .body(ApiResponse.success("Cliente creado exitosamente", toResponseDTO(clienteCreado)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ClienteResponseDTO>>> listarClientes(@RequestParam(required = false) String comuna) {
        log.debug("GET /api/clientes comuna={}", comuna);
        List<ClienteResponseDTO> clientes = clienteService.listar(comuna).stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Listado de clientes", clientes));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClienteResponseDTO>> obtenerCliente(@PathVariable String id) {
        log.debug("GET /api/clientes/{}", id);
        return ResponseEntity.ok(ApiResponse.success("Cliente encontrado", toResponseDTO(clienteService.obtenerPorIdentificador(id))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClienteResponseDTO>> actualizarCliente(@PathVariable Long id, @Valid @RequestBody ClienteUpdateRequestDTO request) {
        log.info("PUT /api/clientes/{} nombre={} rut={}", id, request.nombreCl(), request.rutCl());
        return ResponseEntity.ok(ApiResponse.success("Cliente actualizado", toResponseDTO(clienteService.actualizar(id, request))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarCliente(@PathVariable Long id) {
        log.info("DELETE /api/clientes/{}", id);
        clienteService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.success("Cliente eliminado exitosamente", null));
    }

    private ClienteResponseDTO toResponseDTO(Cliente cliente) {
        return new ClienteResponseDTO(
            cliente.getIdCliente(),
            cliente.getNombreCl(),
            cliente.getRutCl(),
            cliente.getDivCl(),
            cliente.getDireccionCl(),
            cliente.getEmailCl(),
            cliente.getTelefonoCl(),
            cliente.getComuna().getNombreComuna(),
            cliente.getComuna().getProvincia().getNombreProvincia(),
            cliente.getComuna().getProvincia().getRegion().getNombreRegion(),
            cliente.getFechaRegistro()
        );
    }
}


