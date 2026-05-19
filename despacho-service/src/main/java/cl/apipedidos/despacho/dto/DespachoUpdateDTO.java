package cl.apipedidos.despacho.dto;

import java.time.LocalDate;

public record DespachoUpdateDTO(
        String transportista,
        LocalDate fechaDespacho,
        String trackingCode
) {}
