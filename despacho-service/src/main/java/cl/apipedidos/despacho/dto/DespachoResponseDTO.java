package cl.apipedidos.despacho.dto;

import cl.apipedidos.despacho.entity.TipoDespacho;
import java.time.LocalDate;

public record DespachoResponseDTO(
        Long id,
        Long pedidoId,
        TipoDespacho tipoDespacho,
        String transportista,
        LocalDate fechaDespacho,
        String trackingCode
) {}
