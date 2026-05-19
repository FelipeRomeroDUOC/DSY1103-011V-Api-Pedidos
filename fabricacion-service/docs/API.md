# API Reference — fabricacion-service

> Base URL: `http://localhost:8086`

## Formato de Respuesta

Todas las respuestas usan el wrapper `ApiResponse<T>`:

```json
{
  "mensaje": "string",
  "data": { },
  "exitoso": true,
  "timestamp": "2026-05-18T12:00:00"
}
```

---

## Endpoints

### 1. Health Check — `GET /api/fabricacion/ping`

```bash
curl http://localhost:8086/api/fabricacion/ping
```

**200 OK:**
```json
{ "mensaje": "pong", "data": "fabricacion-service", "exitoso": true }
```

---

### 2. Crear Orden — `POST /api/fabricacion`

Crea una orden de fabricación. Valida que el pedido exista en `pedido-service` y que no haya duplicados.

**Request Body:**

| Campo | Tipo | Requerido | Validación |
|---|---|---|---|
| `numeroPedido` | `Long` | ✅ | `@NotNull` |
| `usuarioResponsable` | `String` | ✅ | `@NotBlank`, max 255 |
| `descripcionEstado` | `String` | ❌ | max 500 |

```bash
curl -X POST http://localhost:8086/api/fabricacion \
  -H "Content-Type: application/json" \
  -d '{"numeroPedido":1,"usuarioResponsable":"Operador 1"}'
```

**201 Created:**
```json
{
  "mensaje": "Orden creada",
  "data": {
    "id": 1, "numeroPedido": 1, "estadoFabricacion": "EN_PROCESO",
    "fechaInicio": "...", "fechaFin": null, "usuarioResponsable": "Operador 1"
  },
  "exitoso": true
}
```

**Efectos secundarios:**
- Registra historial: `null → EN_PROCESO`
- Notifica a `pedido-service`: estado → `EN_FABRICACION`

**Errores:** `400` (duplicado/validación), `404` (pedido no existe), `502` (servicio no disponible)

---

### 3. Obtener Orden — `GET /api/fabricacion/{id}`

```bash
curl http://localhost:8086/api/fabricacion/1
```

**200 OK:**
```json
{
  "mensaje": "Orden encontrada",
  "data": {
    "id": 1, "numeroPedido": 1, "estadoFabricacion": "EN_PROCESO",
    "fechaInicio": "...", "fechaFin": null, "usuarioResponsable": "Operador 1"
  },
  "exitoso": true
}
```

**Errores:** `400` (orden no encontrada)

---

### 4. Actualizar Estado — `PATCH /api/fabricacion/{id}/estado`

Cambia el estado de una orden. Si es `TERMINADO`, notifica a `pedido-service` (estado → `LISTO`).

**Request Body:**

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `nuevoEstado` | `EstadoFabricacion` | ✅ | `EN_PROCESO`, `TERMINADO`, `PAUSADO` |
| `motivo` | `String` | ❌ | Razón del cambio |
| `usuarioId` | `String` | ❌ | Usuario que hace el cambio |

```bash
curl -X PATCH http://localhost:8086/api/fabricacion/1/estado \
  -H "Content-Type: application/json" \
  -d '{"nuevoEstado":"TERMINADO","motivo":"Lote completado"}'
```

**200 OK:**
```json
{
  "mensaje": "Estado actualizado",
  "data": {
    "id": 1, "estadoFabricacion": "TERMINADO", "fechaFin": "..."
  },
  "exitoso": true
}
```

**Errores:** `400`, `404`, `409` (conflicto en pedido-service), `502`

---

## Estados de Fabricación

| Estado | Descripción |
|---|---|
| `EN_PROCESO` | Producción activa (estado inicial) |
| `TERMINADO` | Fabricación completada → notifica pedido |
| `PAUSADO` | Temporalmente detenida |

```
EN_PROCESO ──► TERMINADO
EN_PROCESO ──► PAUSADO
PAUSADO    ──► EN_PROCESO
PAUSADO    ──► TERMINADO
```

---

## Manejo de Errores

| Excepción | HTTP | Causa |
|---|---|---|
| `PedidoNoEncontradoException` | 404 | Pedido no existe |
| `FabricacionException` | 400 | Error de negocio |
| `ResponseStatusException` | Variable | Error Feign (502, 409) |
| `MethodArgumentNotValidException` | 400 | Validación fallida |
| `Exception` | 500 | Error inesperado |

**Ejemplo error:**
```json
{ "mensaje": "Ya existe una orden para el pedido: 1", "data": null, "exitoso": false }
```
