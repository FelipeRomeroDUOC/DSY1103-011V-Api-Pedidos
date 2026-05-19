# API Reference — despacho-service

> Base URL: `http://localhost:8084`

## Formato de Respuesta Estándar (ApiResponse)

Todas las respuestas exitosas de este servicio utilizan el envoltorio genérico del monorepo:

```json
{
  "mensaje": "Descripción de la operación exitosa",
  "data": { ... },
  "exitoso": true,
  "timestamp": "2026-05-19T00:00:00.000"
}
```

---

## Endpoints

### 1. Registrar Despacho — `POST /api/despachos`

Registra la información de despacho de un pedido. Previamente verifica en `pedido-service` que el pedido exista y esté en estado `LISTO`.

**Request Body (`DespachoRequestDTO`):**

| Campo | Tipo | Requerido | Validación / Reglas |
|---|---|---|---|
| `pedidoId` | `Long` | ✅ | El pedido debe existir en `pedido-service` (estado: LISTO). Solo puede haber 1 despacho por pedido. |
| `tipoDespacho` | `TipoDespacho` | ✅ | Valores: `RETIRO`, `RM`, `REGION` |
| `transportista` | `String` | ⚠️ | Obligatorio **sólo** si `tipoDespacho` = `REGION`. |
| `fechaDespacho` | `LocalDate` | ❌ | Fecha programada del despacho (ej. "2026-05-20"). |
| `trackingCode` | `String` | ❌ | Código de seguimiento de la agencia externa. |

**Ejemplo de Petición:**
```bash
curl -X POST http://localhost:8084/api/despachos \
  -H "Content-Type: application/json" \
  -d '{
    "pedidoId": 33,
    "tipoDespacho": "REGION",
    "transportista": "Starken",
    "fechaDespacho": "2026-05-20",
    "trackingCode": "STK-999"
  }'
```

**Respuestas Posibles:**
* `201 Created`: Despacho creado con éxito.
* `400 Bad Request`: Faltan datos (ej. transportista omitido para `REGION`).
* `404 Not Found`: Pedido no encontrado en `pedido-service`.
* `409 Conflict`: El pedido ya tiene un despacho asociado.
* `422 Unprocessable Entity`: El pedido existe pero no está en estado `LISTO` (ej. está `PENDIENTE`).

---

### 2. Listar Despachos — `GET /api/despachos`

Retorna todos los despachos registrados.

| Query Param | Tipo | Descripción |
|---|---|---|
| `tipo` | `TipoDespacho` | Opcional. Filtra resultados por `RETIRO`, `RM` o `REGION`. |

**Ejemplos:**
```bash
curl http://localhost:8084/api/despachos
curl http://localhost:8084/api/despachos?tipo=REGION
```

**200 OK:** Retorna `ApiResponse<List<DespachoResponseDTO>>`.

---

### 3. Obtener Despacho por Pedido ID — `GET /api/despachos/{pedidoId}`

Obtiene el despacho asignado a un número de pedido en particular.

```bash
curl http://localhost:8084/api/despachos/33
```

**Respuestas:**
* `200 OK`: `ApiResponse<DespachoResponseDTO>`.
* `404 Not Found`: No hay despacho registrado para ese ID de pedido.

---

### 4. Actualizar Despacho — `PUT /api/despachos/{id}`

Actualiza información superficial de seguimiento (como `transportista`, `fechaDespacho` y `trackingCode`). No permite alterar a qué pedido está asignado (`pedidoId`) ni qué modo original tenía (`tipoDespacho`).

**Request Body (`DespachoUpdateDTO`):**
```json
{
  "transportista": "Chilexpress",
  "trackingCode": "CHX-123456"
}
```

**Respuestas:**
* `200 OK`: `ApiResponse<DespachoResponseDTO>`.
* `404 Not Found`: El despacho indicado no existe.

---

### 5. Healthcheck — `GET /api/despachos/ping`

Utilizado para comprobar el estado de salud del servicio.

```json
{
  "mensaje": "despacho-service activo",
  "data": null,
  "exitoso": true,
  "timestamp": "2026-05-19T00:00:00.000"
}
```
