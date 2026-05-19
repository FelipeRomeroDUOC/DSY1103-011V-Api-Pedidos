package cl.apipedidos.despacho.dto;

import cl.apipedidos.despacho.entity.TipoDespacho;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record DespachoRequestDTO(
        @NotNull(message = "El ID del pedido es obligatorio")
        Long pedidoId,
        
        @NotNull(message = "El tipo de despacho es obligatorio")
        TipoDespacho tipoDespacho,
        
        String transportista,
        Long transportistaId,
        LocalDate fechaDespacho,
        String trackingCode
) {}
