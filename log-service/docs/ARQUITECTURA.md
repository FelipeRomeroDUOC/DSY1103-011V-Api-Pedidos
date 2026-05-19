# Arquitectura — log-service

## Visión General

`log-service` es un microservicio de escritura simple. Su unica responsabilidad es recibir eventos operativos, normalizar datos basicos y persistirlos en H2.

## Diagrama de Capas

```text
┌──────────────────────────────────────────────┐
│                   Controller                 │
│                 LogController                │
├──────────────────────────────────────────────┤
│                    Service                   │
│             LogService / LogServiceImpl      │
├──────────────────────────────────────────────┤
│                  Repository                  │
│                  LogRepository               │
├──────────────────────────────────────────────┤
│                    Entity                    │
│                  LogEntrada                  │
├──────────────────────────────────────────────┤
│                H2 Database                   │
└──────────────────────────────────────────────┘
```

## Flujo: Registrar Log

```text
POST /api/logs
    │
    ▼
LogController.registrarLog()
    │
    ▼
LogServiceImpl.registrarLog()
    ├── Construye LogEntrada desde LogRequestDTO
    ├── Asigna timestamp = LocalDateTime.now()
    ├── Persiste con LogRepository.save()
    └── Retorna ApiResponse<LogEntrada>
```

## Flujo: Consultar Logs

```text
GET /api/logs?servicio=X&desde=Y
    │
    ▼
LogController.consultarLogs()
    │
    ▼
LogServiceImpl.consultarLogs()
    ├── Sin filtros -> findAll()
    ├── Solo servicio -> findByServicio()
    ├── Solo fecha -> findByTimestampAfter()
    └── Ambos -> findByServicioAndTimestampAfter()
```

## Estructura de Paquetes

```text
cl.apipedidos.logservice
├── LogServiceApplication.java
├── config/
│   └── ApiExceptionHandler.java
├── controller/
│   └── LogController.java
├── dto/
│   ├── ApiResponse.java
│   └── LogRequestDTO.java
├── entity/
│   └── LogEntrada.java
├── repository/
│   └── LogRepository.java
└── service/
    ├── LogService.java
    └── LogServiceImpl.java
```

## Independencia

Este servicio no consume otros microservicios. Puede ejecutarse y validarse de forma aislada.
