package cl.apipedidos.cliente.service;

import cl.apipedidos.cliente.dto.ClienteCreateRequestDTO;
import cl.apipedidos.cliente.dto.ClienteUpdateRequestDTO;
import cl.apipedidos.cliente.entity.Cliente;
import cl.apipedidos.cliente.repository.ClienteRepository;
import cl.apipedidos.ubicacion.entity.Comuna;
import cl.apipedidos.ubicacion.repository.ComunaRepository;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ComunaRepository comunaRepository;

    public ClienteService(ClienteRepository clienteRepository, ComunaRepository comunaRepository) {
        this.clienteRepository = clienteRepository;
        this.comunaRepository = comunaRepository;
    }

    public Cliente crear(ClienteCreateRequestDTO request) {
        log.info("Creando cliente nombre={}, rut={}, comuna={}", request.nombreCl(), request.rutCl(), request.comuna());
        Cliente cliente = construirCliente(request);
        validarUnicidadParaCreacion(cliente);
        Cliente clienteGuardado = clienteRepository.save(cliente);
        log.info("Cliente creado correctamente id={}, nombre={}, rut={}", clienteGuardado.getIdCliente(), clienteGuardado.getNombreCl(), clienteGuardado.getRutCl());
        return clienteGuardado;
    }

    @Transactional(readOnly = true)
    public List<Cliente> listar(String comuna) {
        log.debug("Listando clientes con filtro comuna={}", comuna);
        if (comuna == null || comuna.isBlank()) {
            return clienteRepository.findAll();
        }
        return clienteRepository.findByComuna_NombreComunaIgnoreCase(comuna.trim());
    }

    @Transactional(readOnly = true)
    public Cliente obtenerPorId(Long id) {
        log.debug("Obteniendo cliente por id={}", id);
        return buscarClientePorId(id);
    }

    @Transactional(readOnly = true)
    public Cliente obtenerPorIdentificador(String identificador) {
        log.debug("Obteniendo cliente por identificador={}", identificador);
        if (identificador == null || identificador.isBlank()) {
            log.warn("Identificador de cliente vacío o nulo");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado");
        }

        if (esNumerico(identificador)) {
            return buscarClientePorId(Long.parseLong(identificador));
        }

        return clienteRepository.findByNombreClIgnoreCase(identificador.trim())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
    }

    public Cliente actualizar(Long id, ClienteUpdateRequestDTO request) {
        log.info("Actualizando cliente id={} nombre={} rut={}", id, request.nombreCl(), request.rutCl());
        Cliente clienteExistente = buscarClientePorId(id);
        Cliente clienteActualizado = construirCliente(request);
        validarUnicidadParaActualizacion(id, clienteActualizado);

        clienteExistente.setNombreCl(clienteActualizado.getNombreCl());
        clienteExistente.setRutCl(clienteActualizado.getRutCl());
        clienteExistente.setDivCl(clienteActualizado.getDivCl());
        clienteExistente.setDireccionCl(clienteActualizado.getDireccionCl());
        clienteExistente.setComuna(clienteActualizado.getComuna());

        Cliente clienteGuardado = clienteRepository.save(clienteExistente);
        log.info("Cliente actualizado correctamente id={}, nombre={}, rut={}", clienteGuardado.getIdCliente(), clienteGuardado.getNombreCl(), clienteGuardado.getRutCl());
        return clienteGuardado;
    }

    public void eliminar(Long id) {
        log.info("Eliminando cliente id={}", id);
        if (!clienteRepository.existsById(id)) {
            log.warn("No existe cliente para eliminar id={}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado");
        }
        clienteRepository.deleteById(id);
        log.info("Cliente eliminado id={}", id);
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
        log.debug("Construyendo cliente para nombre={} comuna={}", request.nombreCl(), request.comuna());
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
        log.debug("Construyendo cliente actualizado para nombre={} comuna={}", request.nombreCl(), request.comuna());
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
        String comunaNormalizada = normalizarTexto(nombreComuna);
        log.debug("Resolviendo comuna={}", comunaNormalizada);
        Optional<Comuna> comunaExacta = comunaRepository.findByNombreComunaIgnoreCase(comunaNormalizada);
        if (comunaExacta.isPresent()) {
            log.debug("Comuna encontrada por coincidencia exacta={}", comunaExacta.get().getNombreComuna());
            return comunaExacta.get();
        }

        String comunaSinAcentos = normalizarBusquedaComuna(comunaNormalizada);
        return comunaRepository.findAll().stream()
            .filter(comuna -> normalizarBusquedaComuna(comuna.getNombreComuna()).equals(comunaSinAcentos))
            .findFirst()
            .orElseThrow(() -> {
                log.warn("Comuna no encontrada={}", nombreComuna);
                return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comuna no encontrada");
            });
    }

    private String normalizarBusquedaComuna(String valor) {
        if (valor == null) {
            return "";
        }

        String sinEspacios = valor.trim().toLowerCase(Locale.ROOT);
        String normalizado = Normalizer.normalize(sinEspacios, Normalizer.Form.NFD);
        return normalizado.replaceAll("\\p{M}+", "");
    }

    private boolean esNumerico(String valor) {
        return valor != null && !valor.isBlank() && valor.chars().allMatch(Character::isDigit);
    }

    private Cliente buscarClientePorId(Long id) {
        log.debug("Buscando cliente por id={}", id);
        return clienteRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
    }
}
