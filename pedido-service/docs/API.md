# API Reference — pedido-service

> Base URL: `http://localhost:8081`

## Formato de Error Estándar

```json
{
  "status": 404,
  "message": "Pedido no encontrado: 99",
  "path": "/api/pedidos/99",
  "timestamp": "2026-05-18T12:00:00-04:00",
  "errors": null
}
```

---

## Endpoints

### 1. Crear Pedido — `POST /api/pedidos`

Crea un pedido con ítems. Valida que el cliente exista en `cliente-service`.

**Request Body (`CreatePedidoRequest`):**

| Campo | Tipo | Requerido | Validación |
|---|---|---|---|
| `numeroPedido` | `String` | ✅ | `@NotBlank` |
| `clienteId` | `Long` | ✅ | `@NotNull` |
| `tipoDespacho` | `TipoDespacho` | ✅ | `RETIRO`, `RM`/`DOMICILIO`, `REGION` |
| `items` | `List<CreateItemRequest>` | ✅ | min 1 item |

**Item (`CreateItemRequest`):**

| Campo | Tipo | Requerido | Validación |
|---|---|---|---|
| `productoId` | `Long` | ✅ | `@NotNull` |
| `cantidad` | `Integer` | ✅ | `@Min(1)` |
| `precioUnitario` | `BigDecimal` | ✅ | `@DecimalMin("0.01")` |

```bash
curl -X POST http://localhost:8081/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "numeroPedido": "PED-001",
    "clienteId": 1,
    "tipoDespacho": "DOMICILIO",
    "items": [{"productoId":100,"cantidad":2,"precioUnitario":15000}]
  }'
```

**201 Created:** Retorna `PedidoDTO` con ítems y monto total calculado.

**Errores:** `400` (validación), `404` (cliente no existe), `409` (pedido duplicado), `502` (cliente-service no disponible)

---

### 2. Listar Pedidos — `GET /api/pedidos`

Filtros opcionales por query params.

| Parámetro | Tipo | Descripción |
|---|---|---|
| `estado` | `EstadoPedido` | Filtrar por estado |
| `tipo` | `TipoDespacho` | Filtrar por tipo despacho |
| `clienteId` | `Long` | Filtrar por cliente |

```bash
curl "http://localhost:8081/api/pedidos?estado=PENDIENTE&clienteId=1"
```

**200 OK:** Retorna `List<PedidoDTO>`.

---

### 3. Obtener Pedido por ID — `GET /api/pedidos/{id}`

```bash
curl http://localhost:8081/api/pedidos/1
```

**200 OK:** Retorna `PedidoDTO`. **Errores:** `404`

---

### 4. Obtener por Número — `GET /api/pedidos/numero/{numeroPedido}`

```bash
curl http://localhost:8081/api/pedidos/numero/PED-001
```

**200 OK:** Retorna `PedidoDTO`. **Errores:** `404`

---

### 5. Obtener Items — `GET /api/pedidos/{id}/items`

```bash
curl http://localhost:8081/api/pedidos/1/items
```

**200 OK:** Retorna `List<ItemPedidoDTO>`. **Errores:** `404`

---

### 6. Actualizar Estado — `PATCH /api/pedidos/{id}/estado`

Cambia el estado con validación de transiciones.

**Request Body:**

| Campo | Tipo | Requerido |
|---|---|---|
| `nuevoEstado` | `EstadoPedido` | ✅ |

```bash
curl -X PATCH http://localhost:8081/api/pedidos/1/estado \
  -H "Content-Type: application/json" \
  -d '{"nuevoEstado":"EN_FABRICACION"}'
```

**200 OK:** Retorna `PedidoDTO`. **Errores:** `404`, `409` (transición inválida)

---

### 7. Eliminar Pedido — `DELETE /api/pedidos/{id}`

No permite eliminar pedidos con estado `DESPACHADO` o `ENTREGADO`.

```bash
curl -X DELETE http://localhost:8081/api/pedidos/1
```

**204 No Content.** **Errores:** `404`, `409` (no eliminable)

---

## Estados de Pedido (`EstadoPedido`)

| Estado | Descripción |
|---|---|
| `PENDIENTE` | Recién creado (estado inicial) |
| `EN_FABRICACION` | En proceso de manufactura |
| `LISTO` | Fabricación completada |
| `DESPACHADO` | Enviado al cliente |
| `ENTREGADO` | Recibido por el cliente |
| `CANCELADO` | Pedido cancelado |

### Transiciones Válidas

```
PENDIENTE ──► EN_FABRICACION ──► LISTO ──► DESPACHADO ──► ENTREGADO
    │                │
    └──► CANCELADO ◄──┘
```

| Desde | Hacia (permitidos) |
|---|---|
| `PENDIENTE` | `EN_FABRICACION`, `CANCELADO` |
| `EN_FABRICACION` | `LISTO`, `CANCELADO` |
| `LISTO` | `DESPACHADO` |
| `DESPACHADO` | `ENTREGADO` |
| `CANCELADO` | *(ninguno — estado final)* |
| `ENTREGADO` | *(ninguno — estado final)* |

## Tipos de Despacho (`TipoDespacho`)

| Valor | Aliases aceptados | Descripción |
|---|---|---|
| `RETIRO` | `RETIRO` | Retiro en tienda |
| `RM` | `RM`, `DOMICILIO` | Despacho Región Metropolitana |
| `REGION` | `REGION` | Despacho a regiones |

## Manejo de Errores

| Excepción | HTTP | Causa |
|---|---|---|
| `ResponseStatusException` | Variable | Errores de negocio y validación |
| `MethodArgumentNotValidException` | 400 | Validación `@Valid` fallida |
| `Exception` | 500 | Error inesperado |
