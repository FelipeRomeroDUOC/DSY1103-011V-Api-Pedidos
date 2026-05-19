package cl.apipedidos.transportistaservice.service;

import cl.apipedidos.transportistaservice.dto.TransportistaRequestDTO;
import cl.apipedidos.transportistaservice.dto.TransportistaResponseDTO;
import cl.apipedidos.transportistaservice.dto.TransportistaUpdateDTO;
import cl.apipedidos.transportistaservice.model.Transportista;
import cl.apipedidos.transportistaservice.repository.TransportistaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransportistaServiceImpl implements TransportistaService {

    private final TransportistaRepository transportistaRepository;

    @Override
    @Transactional
    public TransportistaResponseDTO crearTransportista(TransportistaRequestDTO request) {
        if (transportistaRepository.existsByCodigoInterno(request.getCodigoInterno())) {
            throw new IllegalStateException("El código interno ya existe");
        }

        Transportista t = new Transportista();
        t.setNombre(request.getNombre());
        t.setCodigoInterno(request.getCodigoInterno());
        t.setContacto(request.getContacto());
        t.setRegionesCobertura(request.getRegionesCobertura());
        t.setActivo(true);

        return toDTO(transportistaRepository.save(t));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransportistaResponseDTO> listarActivos() {
        return transportistaRepository.findByActivoTrue().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TransportistaResponseDTO obtenerPorId(Long id) {
        Transportista t = transportistaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transportista no encontrado"));
                
        if (!t.isActivo()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transportista inactivo");
        }
        
        return toDTO(t);
    }

    @Override
    @Transactional
    public TransportistaResponseDTO actualizarTransportista(Long id, TransportistaUpdateDTO request) {
        Transportista t = transportistaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transportista no encontrado"));

        if (request.getNombre() != null && !request.getNombre().trim().isEmpty()) {
            t.setNombre(request.getNombre());
        }
        if (request.getContacto() != null) {
            t.setContacto(request.getContacto());
        }
        if (request.getRegionesCobertura() != null) {
            t.setRegionesCobertura(request.getRegionesCobertura());
        }
        if (request.getActivo() != null) {
            t.setActivo(request.getActivo());
        }

        return toDTO(transportistaRepository.save(t));
    }

    private TransportistaResponseDTO toDTO(Transportista t) {
        TransportistaResponseDTO dto = new TransportistaResponseDTO();
        dto.setId(t.getId());
        dto.setNombre(t.getNombre());
        dto.setCodigoInterno(t.getCodigoInterno());
        dto.setContacto(t.getContacto());
        dto.setRegionesCobertura(t.getRegionesCobertura());
        dto.setActivo(t.isActivo());
        return dto;
    }
}
