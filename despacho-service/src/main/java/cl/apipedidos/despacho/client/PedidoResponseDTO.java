package cl.apipedidos.despacho.client;

public record PedidoResponseDTO(
        Long id,
        String estado,
        String tipoDespacho
) {}
