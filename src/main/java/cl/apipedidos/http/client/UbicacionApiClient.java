package cl.apipedidos.http.client;

import cl.apipedidos.http.dto.ComunaResponseDTO;
import cl.apipedidos.http.dto.RegionResponseDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UbicacionApiClient extends AbstractHttpClient {

    public UbicacionApiClient(
        ObjectMapper objectMapper,
        @Value("${app.http.base-url:http://localhost:8080}") String baseUrl
    ) {
        super(baseUrl, objectMapper);
    }

    public List<RegionResponseDTO> listarRegiones() {
        return get("/api/regiones", new TypeReference<List<RegionResponseDTO>>() {});
    }

    public List<ComunaResponseDTO> listarComunasPorRegion(String idRegion) {
        return get("/api/regiones/" + encodePathSegment(idRegion) + "/comunas", new TypeReference<List<ComunaResponseDTO>>() {});
    }
}