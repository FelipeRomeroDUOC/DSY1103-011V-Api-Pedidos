# API Reference — log-service

> Base URL: `http://localhost:8089`

## Formato de Respuesta Estandar (ApiResponse)

Todas las respuestas del servicio utilizan el wrapper `ApiResponse<T>`:

```json
{
  "mensaje": "Operacion completada",
  "data": { ... },
  "exitoso": true,
  "timestamp": "2026-05-19T12:00:00"
}
```

## Formato de Error Estandar

```json
{
  "mensaje": "La solicitud contiene errores de validacion",
  "data": ["servicio no puede estar vacio"],
  "exitoso": false,
  "timestamp": "2026-05-19T12:00:00"
}
```

---

## 1. Registrar Log — `POST /api/logs`

Registra una nueva entrada de log en la base local.

**Request Body (`LogRequestDTO`):**

| Campo | Tipo | Requerido | Validacion |
|---|---|---|---|
| `servicio` | `String` | Si | `@NotBlank` |
| `operacion` | `String` | Si | `@NotBlank` |
| `usuarioId` | `String` | No | Opcional |
| `resultado` | `String` | Si | `@NotBlank` |
| `detalle` | `String` | No | Opcional |

```bash
curl -X POST http://localhost:8089/api/logs \
  -H "Content-Type: application/json" \
  -d '{
    "servicio": "pedido-service",
    "operacion": "CREAR_PEDIDO",
    "usuarioId": "42",
    "resultado": "EXITO",
    "detalle": "Pedido PED-007 creado correctamente"
  }'
```

**201 Created**

```json
{
  "mensaje": "Log registrado correctamente",
  "data": {
    "id": 1,
    "servicio": "pedido-service",
    "operacion": "CREAR_PEDIDO",
    "usuarioId": "42",
    "timestamp": "2026-05-19T12:00:00",
    "resultado": "EXITO",
    "detalle": "Pedido PED-007 creado correctamente"
  },
  "exitoso": true,
  "timestamp": "2026-05-19T12:00:00"
}
```

**Errores:** `400` por validacion.

---

## 2. Consultar Logs — `GET /api/logs`

Lista logs con filtros opcionales por servicio y fecha inicial.

| Parametro | Tipo | Descripcion |
|---|---|---|
| `servicio` | `String` | Filtra por nombre de servicio |
| `desde` | `LocalDateTime` | Filtra desde una fecha ISO-8601 |

```bash
curl "http://localhost:8089/api/logs"
curl "http://localhost:8089/api/logs?servicio=pedido-service"
curl "http://localhost:8089/api/logs?desde=2026-05-01T00:00:00"
curl "http://localhost:8089/api/logs?servicio=fabricacion-service&desde=2026-05-01T00:00:00"
```

**200 OK**

```json
{
  "mensaje": "Logs consultados correctamente",
  "data": [],
  "exitoso": true,
  "timestamp": "2026-05-19T12:00:00"
}
```

---

## 3. Ping — `GET /api/logs/ping`

Healthcheck del servicio.

```bash
curl http://localhost:8089/api/logs/ping
```

**200 OK**

```json
{
  "mensaje": "log-service OK",
  "data": "log-service OK",
  "exitoso": true,
  "timestamp": "2026-05-19T12:00:00"
}
```

---

## Manejo de Errores

| Excepcion | HTTP | Causa |
|---|---|---|
| `ResponseStatusException` | Variable | Errores de negocio |
| `MethodArgumentNotValidException` | 400 | Validacion fallida |
| `MethodArgumentTypeMismatchException` | 400 | Tipo de parametro invalido |
| `MissingServletRequestParameterException` | 400 | Falta un parametro requerido |
| `Exception` | 500 | Error inesperado |
