# API Reference — estado-service

> Base URL: `http://localhost:8085`

## Formato de Respuesta Estándar (ApiResponse)

Al igual que en los demás módulos, se utiliza un envoltorio genérico para todas las respuestas:

```json
{
  "mensaje": "Descripción del éxito/error",
  "data": { ... },
  "exitoso": true,
  "timestamp": "2026-05-19T00:00:00.000"
}
```

---

## Endpoints

### 1. Registrar Cambio de Estado — `POST /api/estados`

Este endpoint es consumido internamente por `pedido-service` cada vez que muta la propiedad estado de una orden.

**Request Body (`CambioEstadoRequestDTO`):**

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `pedidoId` | `Long` | ✅ | ID numérico del pedido afectado. |
| `estadoAnterior` | `String` | ✅ | Estado original previo al cambio (ej. `"PENDIENTE"`). |
| `estadoNuevo` | `String` | ✅ | Nuevo estado consolidado (ej. `"EN_FABRICACION"`). |
| `usuarioId` | `Long` | ❌ | ID del usuario que forzó el evento (Reservado para Auth futura, puede ser null). |

**Ejemplo de Petición:**
```bash
curl -X POST http://localhost:8085/api/estados \
  -H "Content-Type: application/json" \
  -d '{
    "pedidoId": 33,
    "estadoAnterior": "PENDIENTE",
    "estadoNuevo": "EN_FABRICACION",
    "usuarioId": null
  }'
```

**Respuestas Posibles:**
* `201 Created`: Cambio insertado satisfactoriamente.
* `400 Bad Request`: Falta un campo obligatorio (`pedidoId`, `estadoAnterior`, `estadoNuevo`).

---

### 2. Obtener Historial de un Pedido — `GET /api/estados/{pedidoId}`

Recupera el historial cronológico (ordenado por fecha de inserción ascendente) de todas las transiciones por las que pasó un pedido.

**Ejemplo de Petición:**
```bash
curl http://localhost:8085/api/estados/33
```

**Respuesta Esperada (`200 OK`):**
```json
{
  "mensaje": "Historial obtenido correctamente",
  "data": [
    {
      "id": 1,
      "pedidoId": 33,
      "estadoAnterior": "PENDIENTE",
      "estadoNuevo": "EN_FABRICACION",
      "fechaCambio": "2026-05-19T10:00:00.000",
      "usuarioId": null
    },
    {
      "id": 2,
      "pedidoId": 33,
      "estadoAnterior": "EN_FABRICACION",
      "estadoNuevo": "LISTO",
      "fechaCambio": "2026-05-19T14:30:00.000",
      "usuarioId": null
    }
  ],
  "exitoso": true,
  "timestamp": "2026-05-19T15:00:00.000"
}
```

> **Nota arquitectónica**: Si el `pedidoId` provisto no existe (o aún no tiene mutaciones de estado), el servicio devolverá una lista vacía `[]` con status `200 OK`, y no un error `404`. Esto se debe a que `estado-service` desconoce el catálogo original de `pedido-service`.

---

### 3. Healthcheck — `GET /api/estados/ping`

Endpoint base para comprobar la vitalidad del servicio de auditoría.

```json
{
  "mensaje": "estado-service activo",
  "data": null,
  "exitoso": true,
  "timestamp": "2026-05-19T00:00:00.000"
}
```
