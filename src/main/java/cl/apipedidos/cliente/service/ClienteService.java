package cl.apipedidos.cliente.service;

import cl.apipedidos.cliente.dto.ClienteCreateRequestDTO;
import cl.apipedidos.cliente.dto.ClienteUpdateRequestDTO;
import cl.apipedidos.cliente.entity.Cliente;
import cl.apipedidos.cliente.repository.ClienteRepository;
import cl.apipedidos.ubicacion.entity.Comuna;
import cl.apipedidos.ubicacion.repository.ComunaRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ComunaRepository comunaRepository;

    public ClienteService(ClienteRepository clienteRepository, ComunaRepository comunaRepository) {
        this.clienteRepository = clienteRepository;
        this.comunaRepository = comunaRepository;
    }

    public Cliente crear(ClienteCreateRequestDTO request) {
        Cliente cliente = construirCliente(request);
        cliente.setIdCliente(siguienteIdCliente());
        validarUnicidadParaCreacion(cliente);
        return clienteRepository.save(cliente);
    }

    @Transactional(readOnly = true)
    public List<Cliente> listar(String comuna) {
        if (comuna == null || comuna.isBlank()) {
            return clienteRepository.findAll();
        }
        return clienteRepository.findByComuna_NombreComunaIgnoreCase(comuna.trim());
    }

    @Transactional(readOnly = true)
    public Cliente obtenerPorId(Long id) {
        return buscarClientePorId(id);
    }

    @Transactional(readOnly = true)
    public Cliente obtenerPorIdentificador(String identificador) {
        if (identificador == null || identificador.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado");
        }

        if (esNumerico(identificador)) {
            return buscarClientePorId(Long.parseLong(identificador));
        }

        return clienteRepository.findByNombreClIgnoreCase(identificador.trim())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
    }

    public Cliente actualizar(Long id, ClienteUpdateRequestDTO request) {
        Cliente clienteExistente = buscarClientePorId(id);
        Cliente clienteActualizado = construirCliente(request);
        validarUnicidadParaActualizacion(id, clienteActualizado);

        clienteExistente.setNombreCl(clienteActualizado.getNombreCl());
        clienteExistente.setRutCl(clienteActualizado.getRutCl());
        clienteExistente.setDivCl(clienteActualizado.getDivCl());
        clienteExistente.setDireccionCl(clienteActualizado.getDireccionCl());
        clienteExistente.setComuna(clienteActualizado.getComuna());

        return clienteRepository.save(clienteExistente);
    }

    public void eliminar(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado");
        }
        clienteRepository.deleteById(id);
    }

    private void validarUnicidadParaCreacion(Cliente cliente) {
        if (clienteRepository.existsByNombreClIgnoreCase(cliente.getNombreCl())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nombre ya registrado");
        }
        if (clienteRepository.existsByRutCl(cliente.getRutCl())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "RUT ya registrado");
        }
    }

    private void validarUnicidadParaActualizacion(Long id, Cliente cliente) {
        if (clienteRepository.existsByNombreClIgnoreCaseAndIdClienteNot(cliente.getNombreCl(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nombre ya registrado");
        }
        if (clienteRepository.existsByRutClAndIdClienteNot(cliente.getRutCl(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "RUT ya registrado");
        }
    }

    private Cliente construirCliente(ClienteCreateRequestDTO request) {
        Cliente cliente = new Cliente();
        cliente.setNombreCl(normalizarTexto(request.nombreCl()));
        cliente.setRutCl(request.rutCl());
        cliente.setDivCl(normalizarDv(request.divCl()));
        cliente.setDireccionCl(normalizarTexto(request.direccionCl()));
        cliente.setEmailCl(normalizarTexto(request.emailCl()));
        cliente.setTelefonoCl(normalizarTexto(request.telefonoCl()));
        cliente.setComuna(resolverComuna(request.comuna()));
        return cliente;
    }

    private Cliente construirCliente(ClienteUpdateRequestDTO request) {
        Cliente cliente = new Cliente();
        cliente.setNombreCl(normalizarTexto(request.nombreCl()));
        cliente.setRutCl(request.rutCl());
        cliente.setDivCl(normalizarDv(request.divCl()));
        cliente.setDireccionCl(normalizarTexto(request.direccionCl()));
        cliente.setEmailCl(normalizarTexto(request.emailCl()));
        cliente.setTelefonoCl(normalizarTexto(request.telefonoCl()));
        cliente.setComuna(resolverComuna(request.comuna()));
        return cliente;
    }

    private String normalizarTexto(String texto) {
        return texto == null ? null : texto.trim();
    }

    private String normalizarDv(String divCl) {
        String divClNormalizado = normalizarTexto(divCl);
        return divClNormalizado == null ? null : divClNormalizado.toUpperCase(Locale.ROOT);
    }

    private Comuna resolverComuna(String nombreComuna) {
        return comunaRepository.findByNombreComunaIgnoreCase(normalizarTexto(nombreComuna))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comuna no encontrada"));
    }

    private boolean esNumerico(String valor) {
        return valor != null && !valor.isBlank() && valor.chars().allMatch(Character::isDigit);
    }

    private Cliente buscarClientePorId(Long id) {
        return clienteRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
    }

    private long siguienteIdCliente() {
        return clienteRepository.findMaxIdCliente() + 1;
    }
}
