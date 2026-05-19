package cl.apipedidos.logservice.service;

import cl.apipedidos.logservice.dto.ApiResponse;
import cl.apipedidos.logservice.dto.LogRequestDTO;
import cl.apipedidos.logservice.entity.LogEntrada;
import cl.apipedidos.logservice.repository.LogRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final LogRepository logRepository;

    @Override
    @Transactional
    public ApiResponse<LogEntrada> registrarLog(LogRequestDTO request) {
        LogEntrada entrada = new LogEntrada();
        entrada.setServicio(request.getServicio());
        entrada.setOperacion(request.getOperacion());
        entrada.setUsuarioId(request.getUsuarioId());
        entrada.setResultado(request.getResultado());
        entrada.setDetalle(request.getDetalle());
        entrada.setTimestamp(LocalDateTime.now());

        LogEntrada guardado = logRepository.save(entrada);
        return ApiResponse.success("Log registrado correctamente", guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<LogEntrada>> consultarLogs(String servicio, LocalDateTime desde) {
        List<LogEntrada> logs;

        if (servicio == null && desde == null) {
            logs = logRepository.findAll();
        } else if (servicio != null && desde == null) {
            logs = logRepository.findByServicio(servicio);
        } else if (servicio == null) {
            logs = logRepository.findByTimestampAfter(desde);
        } else {
            logs = logRepository.findByServicioAndTimestampAfter(servicio, desde);
        }

        return ApiResponse.success("Logs consultados correctamente", logs);
    }
}