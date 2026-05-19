package cl.apipedidos.logservice.controller;

import cl.apipedidos.logservice.dto.ApiResponse;
import cl.apipedidos.logservice.dto.LogRequestDTO;
import cl.apipedidos.logservice.entity.LogEntrada;
import cl.apipedidos.logservice.service.LogService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @PostMapping
    public ResponseEntity<ApiResponse<LogEntrada>> registrarLog(@Valid @RequestBody LogRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(logService.registrarLog(request));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LogEntrada>>> consultarLogs(
            @RequestParam(required = false) String servicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde) {
        return ResponseEntity.ok(logService.consultarLogs(servicio, desde));
    }

    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<String>> ping() {
        return ResponseEntity.ok(ApiResponse.success("log-service OK", "log-service OK"));
    }
}