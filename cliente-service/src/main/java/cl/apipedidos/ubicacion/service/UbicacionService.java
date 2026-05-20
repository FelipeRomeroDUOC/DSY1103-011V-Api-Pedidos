package cl.apipedidos.ubicacion.service;

import cl.apipedidos.ubicacion.entity.Comuna;
import cl.apipedidos.ubicacion.entity.Provincia;
import cl.apipedidos.ubicacion.entity.Region;
import cl.apipedidos.ubicacion.repository.ComunaRepository;
import cl.apipedidos.ubicacion.repository.ProvinciaRepository;
import cl.apipedidos.ubicacion.repository.RegionRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SuppressWarnings("null")
@Service
@Transactional(readOnly = true)
public class UbicacionService {

    private final RegionRepository regionRepository;
    private final ProvinciaRepository provinciaRepository;
    private final ComunaRepository comunaRepository;

    public UbicacionService(RegionRepository regionRepository, ProvinciaRepository provinciaRepository, ComunaRepository comunaRepository) {
        this.regionRepository = regionRepository;
        this.provinciaRepository = provinciaRepository;
        this.comunaRepository = comunaRepository;
    }

    public List<Region> listarRegiones() {
        return regionRepository.findAllByOrderByIdRegionAsc();
    }

    public List<Provincia> listarProvinciasPorRegion(String idRegion) {
        String regionIdNormalizado = normalizarIdRegion(idRegion);
        Region region = regionRepository.findById(regionIdNormalizado)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Región no encontrada"));
        return provinciaRepository.findAllByRegion_IdRegionOrderByNombreProvinciaAsc(region.getIdRegion());
    }

    public List<Comuna> listarComunasPorRegion(String idRegion) {
        String regionIdNormalizado = normalizarIdRegion(idRegion);
        Region region = regionRepository.findById(regionIdNormalizado)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Región no encontrada"));
        return comunaRepository.findByProvincia_Region_IdRegionOrderByNombreComunaAsc(region.getIdRegion());
    }

    private String normalizarIdRegion(String idRegion) {
        if (idRegion == null || idRegion.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El identificador de la región es obligatorio");
        }
        return idRegion.trim();
    }
}
