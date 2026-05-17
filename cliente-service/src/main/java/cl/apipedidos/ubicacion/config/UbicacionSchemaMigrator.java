package cl.apipedidos.ubicacion.config;

import cl.apipedidos.ubicacion.entity.Comuna;
import cl.apipedidos.ubicacion.entity.Provincia;
import cl.apipedidos.ubicacion.entity.Region;
import cl.apipedidos.ubicacion.repository.ComunaRepository;
import cl.apipedidos.ubicacion.repository.ProvinciaRepository;
import cl.apipedidos.ubicacion.repository.RegionRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Transactional
public class UbicacionSchemaMigrator implements CommandLineRunner {

    private final RegionRepository regionRepository;
    private final ProvinciaRepository provinciaRepository;
    private final ComunaRepository comunaRepository;

    public UbicacionSchemaMigrator(
        RegionRepository regionRepository,
        ProvinciaRepository provinciaRepository,
        ComunaRepository comunaRepository
    ) {
        this.regionRepository = regionRepository;
        this.provinciaRepository = provinciaRepository;
        this.comunaRepository = comunaRepository;
    }

    @Override
    public void run(String... args) {
        if (provinciaRepository.count() > 0 || comunaRepository.count() == 0) {
            return;
        }

        List<Comuna> comunas = comunaRepository.findAll();
        Map<String, Provincia> provinciasPorId = new LinkedHashMap<>();

        for (Comuna comuna : comunas) {
            String idProvincia = extraerIdProvincia(comuna.getIdComuna());
            Provincia provincia = provinciasPorId.computeIfAbsent(idProvincia, this::crearProvinciaDesdeId);
            comuna.setProvincia(provincia);
        }

        provinciaRepository.saveAll(provinciasPorId.values());
        comunaRepository.saveAll(comunas);
    }

    private Provincia crearProvinciaDesdeId(String idProvincia) {
        String idRegion = idProvincia.substring(0, 2);
        Region region = regionRepository.findById(idRegion)
            .orElseThrow(() -> new IllegalStateException("No se encontró la región asociada a la provincia " + idProvincia));
        return new Provincia(idProvincia, ProvinciaCatalog.nombreProvincia(idProvincia), region);
    }

    private String extraerIdProvincia(String idComuna) {
        if (idComuna == null || idComuna.length() < 3) {
            throw new IllegalStateException("No se pudo derivar la provincia desde la comuna " + idComuna);
        }
        return idComuna.substring(0, 3).trim();
    }
}