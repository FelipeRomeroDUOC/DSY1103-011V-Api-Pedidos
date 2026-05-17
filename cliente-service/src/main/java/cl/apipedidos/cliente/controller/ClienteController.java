package cl.apipedidos.cliente.controller;

import cl.apipedidos.cliente.dto.ClienteCreateRequestDTO;
import cl.apipedidos.cliente.dto.ClienteResponseDTO;
import cl.apipedidos.cliente.dto.ClienteUpdateRequestDTO;
import cl.apipedidos.cliente.entity.Cliente;
import cl.apipedidos.cliente.service.ClienteService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

@RestController
@RequestMapping("/api/clientes")
@Slf4j
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> crearCliente(@Valid @RequestBody ClienteCreateRequestDTO request) {
        log.info("POST /api/clientes nombre={} rut={} comuna={}", request.nombreCl(), request.rutCl(), request.comuna());
        Cliente clienteCreado = clienteService.crear(request);
        return ResponseEntity.created(URI.create("/api/clientes/" + clienteCreado.getIdCliente()))
            .body(toResponseDTO(clienteCreado));
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listarClientes(@RequestParam(required = false) String comuna) {
        log.debug("GET /api/clientes comuna={}", comuna);
        return ResponseEntity.ok(clienteService.listar(comuna).stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList()));
    }

    @GetMapping("/{identificador}")
    public ResponseEntity<ClienteResponseDTO> obtenerCliente(@PathVariable String identificador) {
        log.debug("GET /api/clientes/{}", identificador);
        return ResponseEntity.ok(toResponseDTO(clienteService.obtenerPorIdentificador(identificador)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> actualizarCliente(@PathVariable Long id, @Valid @RequestBody ClienteUpdateRequestDTO request) {
        log.info("PUT /api/clientes/{} nombre={} rut={}", id, request.nombreCl(), request.rutCl());
        return ResponseEntity.ok(toResponseDTO(clienteService.actualizar(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        log.info("DELETE /api/clientes/{}", id);
        clienteService.eliminar(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
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
