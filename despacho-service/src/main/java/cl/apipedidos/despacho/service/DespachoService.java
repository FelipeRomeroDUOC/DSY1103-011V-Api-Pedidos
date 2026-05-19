package cl.apipedidos.despacho.service;

import cl.apipedidos.despacho.dto.DespachoRequestDTO;
import cl.apipedidos.despacho.dto.DespachoResponseDTO;
import cl.apipedidos.despacho.dto.DespachoUpdateDTO;
import cl.apipedidos.despacho.entity.TipoDespacho;

import java.util.List;

public interface DespachoService {

    DespachoResponseDTO registrar(DespachoRequestDTO request);

    List<DespachoResponseDTO> listar(TipoDespacho tipo);

    DespachoResponseDTO obtenerPorPedidoId(Long pedidoId);

    DespachoResponseDTO actualizar(Long id, DespachoUpdateDTO request);
}
