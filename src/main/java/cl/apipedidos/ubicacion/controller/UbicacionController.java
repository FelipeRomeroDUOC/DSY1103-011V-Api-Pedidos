package cl.apipedidos.ubicacion.controller;

import cl.apipedidos.ubicacion.dto.ComunaResponseDTO;
import cl.apipedidos.ubicacion.dto.RegionResponseDTO;
import cl.apipedidos.ubicacion.entity.Comuna;
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

    @GetMapping("/{idRegion}/comunas")
    public ResponseEntity<List<ComunaResponseDTO>> listarComunasPorRegion(@PathVariable String idRegion) {
        return ResponseEntity.ok(ubicacionService.listarComunasPorRegion(idRegion).stream()
            .map(this::toComunaResponseDTO)
            .toList());
    }

    private RegionResponseDTO toRegionResponseDTO(Region region) {
        return new RegionResponseDTO(region.getIdRegion(), region.getNombreRegion());
    }

    private ComunaResponseDTO toComunaResponseDTO(Comuna comuna) {
        return new ComunaResponseDTO(
            comuna.getIdComuna(),
            comuna.getNombreComuna(),
            comuna.getRegion().getIdRegion(),
            comuna.getRegion().getNombreRegion()
        );
    }
}