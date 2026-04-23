package cl.apipedidos.ubicacion.config;

import cl.apipedidos.ubicacion.entity.Comuna;
import cl.apipedidos.ubicacion.entity.Region;
import cl.apipedidos.ubicacion.repository.ComunaRepository;
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
    private final ComunaRepository comunaRepository;
    private final ObjectMapper objectMapper;

    public UbicacionDataLoader(RegionRepository regionRepository, ComunaRepository comunaRepository, ObjectMapper objectMapper) {
        this.regionRepository = regionRepository;
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
                List<Comuna> comunas = new ArrayList<>();

                for (JsonNode regionNode : regionsNode) {
                    Region region = new Region(
                        regionNode.path("idRegion").asText(),
                        regionNode.path("nombreRegion").asText()
                    );
                    regions.add(region);

                    for (JsonNode comunaNode : regionNode.path("comunas")) {
                        comunas.add(new Comuna(
                            comunaNode.path("idComuna").asText(),
                            comunaNode.path("nombreComuna").asText(),
                            region
                        ));
                    }
                }

                regionRepository.saveAll(regions);
                comunaRepository.saveAll(comunas);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("No fue posible cargar las regiones y comunas de Chile", exception);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}