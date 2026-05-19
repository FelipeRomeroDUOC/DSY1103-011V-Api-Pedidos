package cl.apipedidos.producto.service;

import cl.apipedidos.producto.dto.ProductoRequestDTO;
import cl.apipedidos.producto.dto.ProductoResponseDTO;
import java.util.List;

public interface ProductoService {

    ProductoResponseDTO crear(ProductoRequestDTO request);

    List<ProductoResponseDTO> listar(boolean incluirInactivos);

    ProductoResponseDTO obtenerPorId(Long id);

    ProductoResponseDTO actualizar(Long id, ProductoRequestDTO request);

    void desactivar(Long id);

    void activar(Long id);
}
