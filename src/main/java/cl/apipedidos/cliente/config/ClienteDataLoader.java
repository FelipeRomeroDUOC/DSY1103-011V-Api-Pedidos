package cl.apipedidos.cliente.config;

import cl.apipedidos.cliente.entity.Cliente;
import cl.apipedidos.cliente.repository.ClienteRepository;
import cl.apipedidos.ubicacion.entity.Comuna;
import cl.apipedidos.ubicacion.repository.ComunaRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
@Profile({"default", "h2"})
public class ClienteDataLoader implements CommandLineRunner, Ordered {

    private static final Logger logger = Logger.getLogger(ClienteDataLoader.class.getName());

    private final ClienteRepository clienteRepository;
    private final ComunaRepository comunaRepository;

    public ClienteDataLoader(ClienteRepository clienteRepository, ComunaRepository comunaRepository) {
        this.clienteRepository = clienteRepository;
        this.comunaRepository = comunaRepository;
    }

    @Override
    public void run(String... args) {
        if (clienteRepository.count() > 0) {
            return;
        }

        List<Cliente> clientes = new ArrayList<>();
        long siguienteId = 1L;

        siguienteId = agregarClienteSiEsValido(clientes, siguienteId, "Ana Pérez", 12345678L, "9", "Av. Providencia 123", "ana@ejemplo.cl", "+56912345678", "Providencia", LocalDate.of(2026, 4, 1));
        siguienteId = agregarClienteSiEsValido(clientes, siguienteId, "Carlos Soto", 87654321L, "K", "Las Condes 456", "carlos@ejemplo.cl", "+56923456789", "Las Condes", LocalDate.of(2026, 4, 2));
        siguienteId = agregarClienteSiEsValido(clientes, siguienteId, "María Fernández", 11223344L, "1", "Manuel Montt 789", "maria@ejemplo.cl", "+56934567890", "Providencia", LocalDate.of(2026, 4, 3));
        siguienteId = agregarClienteSiEsValido(clientes, siguienteId, "Pedro González", 22334455L, "8", "Avenida Apoquindo 1000", "pedro@ejemplo.cl", "+56945678901", "Las Condes", LocalDate.of(2026, 4, 4));
        agregarClienteSiEsValido(clientes, siguienteId, "Camila Rojas", 33445566L, "7", "San Martín 321", "camila@ejemplo.cl", "+56956789012", "Santiago Centro", LocalDate.of(2026, 4, 5));

        if (!clientes.isEmpty()) {
            clienteRepository.saveAll(clientes);
        }
    }

    private long agregarClienteSiEsValido(List<Cliente> clientes, long idCliente, String nombre, Long rut, String dv, String direccion, String email, String telefono, String comuna, LocalDate fechaRegistro) {
        Optional<Comuna> comunaEncontrada = resolverComuna(comuna);

        if (comunaEncontrada.isEmpty()) {
            logger.warning("No se encontró la comuna de ejemplo: " + comuna + ". Se omitirá este cliente de ejemplo.");
            return idCliente;
        }

        Cliente cliente = new Cliente();
        cliente.setIdCliente(idCliente);
        cliente.setNombreCl(nombre);
        cliente.setRutCl(rut);
        cliente.setDivCl(dv);
        cliente.setDireccionCl(direccion);
        cliente.setEmailCl(email);
        cliente.setTelefonoCl(telefono);
        cliente.setComuna(comunaEncontrada.get());
        cliente.setFechaRegistro(fechaRegistro);
        clientes.add(cliente);

        return idCliente + 1;
    }

    private Optional<Comuna> resolverComuna(String nombreComuna) {
        return comunaRepository.findByNombreComunaIgnoreCase(nombreComuna);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
