package cl.apipedidos.ubicacion.controller;

import cl.apipedidos.ubicacion.dto.ComunaResponseDTO;
import cl.apipedidos.ubicacion.dto.ProvinciaResponseDTO;
import cl.apipedidos.ubicacion.dto.RegionResponseDTO;
import cl.apipedidos.ubicacion.entity.Comuna;
import cl.apipedidos.ubicacion.entity.Provincia;
import cl.apipedidos.ubicacion.entity.Region;
import cl.apipedidos.ubicacion.service.UbicacionService;
import cl.apipedidos.cliente.dto.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/regiones")
@RequiredArgsConstructor
public class UbicacionController {

    private final UbicacionService ubicacionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RegionResponseDTO>>> listarRegiones() {
        List<RegionResponseDTO> regiones = ubicacionService.listarRegiones().stream()
            .map(this::toRegionResponseDTO)
            .toList();
        return ResponseEntity.ok(ApiResponse.success("Listado de regiones", regiones));
    }

    @GetMapping("/{idRegion}/provincias")
    public ResponseEntity<ApiResponse<List<ProvinciaResponseDTO>>> listarProvinciasPorRegion(@PathVariable String idRegion) {
        List<ProvinciaResponseDTO> provincias = ubicacionService.listarProvinciasPorRegion(idRegion).stream()
            .map(this::toProvinciaResponseDTO)
            .toList();
        return ResponseEntity.ok(ApiResponse.success("Listado de provincias", provincias));
    }

    @GetMapping("/{idRegion}/comunas")
    public ResponseEntity<ApiResponse<List<ComunaResponseDTO>>> listarComunasPorRegion(@PathVariable String idRegion) {
        List<ComunaResponseDTO> comunas = ubicacionService.listarComunasPorRegion(idRegion).stream()
            .map(this::toComunaResponseDTO)
            .toList();
        return ResponseEntity.ok(ApiResponse.success("Listado de comunas", comunas));
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