package cl.apipedidos.logservice.service;

import cl.apipedidos.logservice.dto.ApiResponse;
import cl.apipedidos.logservice.dto.LogRequestDTO;
import cl.apipedidos.logservice.entity.LogEntrada;
import java.time.LocalDateTime;
import java.util.List;

public interface LogService {

    ApiResponse<LogEntrada> registrarLog(LogRequestDTO request);

    ApiResponse<List<LogEntrada>> consultarLogs(String servicio, LocalDateTime desde);
}