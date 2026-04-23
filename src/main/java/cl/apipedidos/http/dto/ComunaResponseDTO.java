package cl.apipedidos.http.dto;

public record ComunaResponseDTO(
    String idComuna,
    String nombreComuna,
    String idRegion,
    String nombreRegion
) {
}