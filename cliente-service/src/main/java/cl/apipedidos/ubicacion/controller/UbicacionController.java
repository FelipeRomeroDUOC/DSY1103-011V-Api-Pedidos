package cl.apipedidos.ubicacion.controller;

import cl.apipedidos.ubicacion.dto.ComunaResponseDTO;
import cl.apipedidos.ubicacion.dto.ProvinciaResponseDTO;
import cl.apipedidos.ubicacion.dto.RegionResponseDTO;
import cl.apipedidos.ubicacion.entity.Comuna;
import cl.apipedidos.ubicacion.entity.Provincia;
import cl.apipedidos.ubicacion.entity.Region;
import cl.apipedidos.ubicacion.service.UbicacionService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/regiones")
public class UbicacionController {

    private final UbicacionService ubicacionService;

    public UbicacionController(UbicacionService ubicacionService) {
        this.ubicacionService = ubicacionService;
    }

    @GetMapping
    public ResponseEntity<List<RegionResponseDTO>> listarRegiones() {
        return ResponseEntity.ok(ubicacionService.listarRegiones().stream()
            .map(this::toRegionResponseDTO)
            .toList());
    }

    @GetMapping("/{idRegion}/provincias")
    public ResponseEntity<List<ProvinciaResponseDTO>> listarProvinciasPorRegion(@PathVariable String idRegion) {
        return ResponseEntity.ok(ubicacionService.listarProvinciasPorRegion(idRegion).stream()
            .map(this::toProvinciaResponseDTO)
            .toList());
    }

    @GetMapping("/{idRegion}/comunas")
    public ResponseEntity<List<ComunaResponseDTO>> listarComunasPorRegion(@PathVariable String idRegion) {
        return ResponseEntity.ok(ubicacionService.listarComunasPorRegion(idRegion).stream()
            .map(this::toComunaResponseDTO)
            .toList());
    }

    private RegionResponseDTO toRegionResponseDTO(Region region) {
        return new RegionResponseDTO(region.getIdRegion(), region.getNombreRegion());
    }

    private ProvinciaResponseDTO toProvinciaResponseDTO(Provincia provincia) {
        return new ProvinciaResponseDTO(
            provincia.getIdProvincia(),
            provincia.getNombreProvincia(),
            provincia.getRegion().getIdRegion(),
            provincia.getRegion().getNombreRegion()
        );
    }

    private ComunaResponseDTO toComunaResponseDTO(Comuna comuna) {
        return new ComunaResponseDTO(
            comuna.getIdComuna(),
            comuna.getNombreComuna(),
            comuna.getProvincia().getRegion().getIdRegion(),
            comuna.getProvincia().getRegion().getNombreRegion()
        );
    }
}