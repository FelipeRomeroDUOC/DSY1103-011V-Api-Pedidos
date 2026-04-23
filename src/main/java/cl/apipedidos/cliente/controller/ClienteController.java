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
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> crearCliente(@Valid @RequestBody ClienteCreateRequestDTO request) {
        Cliente clienteCreado = clienteService.crear(request);
        return ResponseEntity.created(URI.create("/api/clientes/" + clienteCreado.getIdCliente()))
            .body(toResponseDTO(clienteCreado));
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listarClientes(@RequestParam(required = false) String comuna) {
        return ResponseEntity.ok(clienteService.listar(comuna).stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList()));
    }

    @GetMapping("/{identificador}")
    public ResponseEntity<ClienteResponseDTO> obtenerCliente(@PathVariable String identificador) {
        return ResponseEntity.ok(toResponseDTO(clienteService.obtenerPorIdentificador(identificador)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> actualizarCliente(@PathVariable Long id, @Valid @RequestBody ClienteUpdateRequestDTO request) {
        return ResponseEntity.ok(toResponseDTO(clienteService.actualizar(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
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
            cliente.getComuna().getRegion().getNombreRegion(),
            cliente.getFechaRegistro()
        );
    }
}
