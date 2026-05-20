package cl.apipedidos.producto.service;

import cl.apipedidos.producto.dto.ProductoRequestDTO;
import cl.apipedidos.producto.dto.ProductoResponseDTO;
import cl.apipedidos.producto.entity.Producto;
import cl.apipedidos.producto.repository.ProductoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;

    @Override
    @Transactional
    public ProductoResponseDTO crear(ProductoRequestDTO request) {
        log.info("Creando producto nombre={}", request.nombre());

        if (productoRepository.existsByNombreIgnoreCase(request.nombre().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un producto con el nombre: " + request.nombre());
        }

        Producto producto = new Producto();
        producto.setNombre(request.nombre());
        producto.setDescripcion(request.descripcion());
        producto.setCategoria(request.categoria());
        producto.setPrecioBase(request.precioBase());
        producto.setActivo(true);

        Producto guardado = productoRepository.save(producto);
        log.info("Producto creado id={}", guardado.getId());
        return toDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listar(boolean incluirInactivos) {
        List<Producto> productos = incluirInactivos 
                ? productoRepository.findAll() 
                : productoRepository.findByActivoTrue();
                
        return productos.stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponseDTO obtenerPorId(Long id) {
        Producto producto = productoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado: " + id));
        return toDTO(producto);
    }

    @Override
    @Transactional
    public ProductoResponseDTO actualizar(Long id, ProductoRequestDTO request) {
        log.info("Actualizando producto id={}", id);

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado: " + id));

        if (productoRepository.existsByNombreIgnoreCaseAndIdNot(request.nombre().trim(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe otro producto con el nombre: " + request.nombre());
        }

        producto.setNombre(request.nombre());
        producto.setDescripcion(request.descripcion());
        producto.setCategoria(request.categoria());
        producto.setPrecioBase(request.precioBase());

        Producto actualizado = productoRepository.save(producto);
        log.info("Producto actualizado id={}", actualizado.getId());
        return toDTO(actualizado);
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        log.info("Desactivando producto id={}", id);

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado: " + id));

        producto.setActivo(false);
        productoRepository.save(producto);
        log.info("Producto desactivado id={}", id);
    }

    @Override
    @Transactional
    public void activar(Long id) {
        log.info("Activando producto id={}", id);

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado: " + id));

        if (producto.isActivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El producto ya se encuentra activo");
        }

        producto.setActivo(true);
        productoRepository.save(producto);
        log.info("Producto activado id={}", id);
    }

    private ProductoResponseDTO toDTO(Producto producto) {
        return new ProductoResponseDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getCategoria(),
                producto.getPrecioBase(),
                producto.isActivo()
        );
    }
}

