package cl.apipedidos.ubicacion.config;

import cl.apipedidos.ubicacion.entity.Comuna;
import cl.apipedidos.ubicacion.entity.Provincia;
import cl.apipedidos.ubicacion.entity.Region;
import cl.apipedidos.ubicacion.repository.ComunaRepository;
import cl.apipedidos.ubicacion.repository.ProvinciaRepository;
import cl.apipedidos.ubicacion.repository.RegionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class UbicacionDataLoader implements CommandLineRunner, Ordered {

    private static final String DATA_FILE = "data/chile-divisiones-territoriales.json";

    private final RegionRepository regionRepository;
    private final ProvinciaRepository provinciaRepository;
    private final ComunaRepository comunaRepository;
    private final ObjectMapper objectMapper;

    public UbicacionDataLoader(
        RegionRepository regionRepository,
        ProvinciaRepository provinciaRepository,
        ComunaRepository comunaRepository,
        ObjectMapper objectMapper
    ) {
        this.regionRepository = regionRepository;
        this.provinciaRepository = provinciaRepository;
        this.comunaRepository = comunaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) {
        if (regionRepository.count() > 0) {
            return;
        }

        try {
            Resource resource = new ClassPathResource(DATA_FILE);
            try (InputStream inputStream = resource.getInputStream()) {
                JsonNode root = objectMapper.readTree(inputStream);
                JsonNode regionsNode = root.path("regions");

                List<Region> regions = new ArrayList<>();
                List<Provincia> provincias = new ArrayList<>();
                List<Comuna> comunas = new ArrayList<>();

                for (JsonNode regionNode : regionsNode) {
                    Region region = new Region(
                        regionNode.path("idRegion").asText(),
                        regionNode.path("nombreRegion").asText()
                    );
                    regions.add(region);

                    List<Provincia> provinciasDeRegion = new ArrayList<>();

                    for (JsonNode comunaNode : regionNode.path("comunas")) {
                        String idComuna = comunaNode.path("idComuna").asText();
                        String idProvincia = idComuna.substring(0, 3);

                        Provincia provincia = provinciasDeRegion.stream()
                            .filter(provinciaExistente -> provinciaExistente.getIdProvincia().equals(idProvincia))
                            .findFirst()
                            .orElseGet(() -> {
                                Provincia nuevaProvincia = new Provincia(
                                    idProvincia,
                                    ProvinciaCatalog.nombreProvincia(idProvincia),
                                    region
                                );
                                provinciasDeRegion.add(nuevaProvincia);
                                provincias.add(nuevaProvincia);
                                return nuevaProvincia;
                            });

                        comunas.add(new Comuna(
                            idComuna,
                            comunaNode.path("nombreComuna").asText(),
                            provincia
                        ));
                    }

                    region.getProvincias().addAll(provinciasDeRegion);
                }

                regionRepository.saveAll(regions);
                provinciaRepository.saveAll(provincias);
                comunaRepository.saveAll(comunas);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("No fue posible cargar las regiones, provincias y comunas de Chile", exception);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}