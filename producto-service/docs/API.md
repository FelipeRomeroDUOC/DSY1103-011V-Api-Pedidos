# API Reference — producto-service

> Base URL: `http://localhost:8083`

## Formato de Respuesta Estándar (ApiResponse)

**TODAS** las respuestas exitosas utilizan el siguiente formato de envoltura:

```json
{
  "mensaje": "Descripción de la operación",
  "data": { ... },
  "exitoso": true,
  "timestamp": "2026-05-18T12:00:00"
}
```

---

## Formato de Error Estándar

```json
{
  "mensaje": "Producto no encontrado: 99",
  "data": null,
  "exitoso": false,
  "timestamp": "2026-05-18T12:00:00"
}
```

---

## Endpoints

### 1. Crear Producto — `POST /api/productos`

**Request Body (`ProductoRequestDTO`):**

| Campo | Tipo | Requerido | Validación |
|---|---|---|---|
| `nombre` | `String` | ✅ | `@NotBlank`, max 150 |
| `descripcion` | `String` | ❌ | max 500 |
| `categoria` | `String` | ✅ | `@NotBlank`, max 100 |
| `precioBase` | `Double` | ✅ | `@DecimalMin("0.0")` |

```bash
curl -X POST http://localhost:8083/api/productos \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Silla ergonómica",
    "descripcion": "Silla de oficina con soporte lumbar",
    "categoria": "Mobiliario",
    "precioBase": 89990.0
  }'
```

**201 Created:**

```json
{
  "mensaje": "Producto creado exitosamente",
  "data": {
    "id": 1,
    "nombre": "Silla ergonómica",
    "descripcion": "Silla de oficina con soporte lumbar",
    "categoria": "Mobiliario",
    "precioBase": 89990.0,
    "activo": true
  },
  "exitoso": true,
  "timestamp": "2026-05-18T12:00:00"
}
```

**Errores:** `400` (validación), `409` (nombre duplicado)

---

### 2. Listar Productos Activos — `GET /api/productos`

Retorna todos los productos con `activo = true`.

```bash
curl http://localhost:8083/api/productos
```

**200 OK:** `ApiResponse<List<ProductoResponseDTO>>`

---

### 3. Obtener Producto por ID — `GET /api/productos/{id}`

Retorna un producto si existe y está activo. Usado por `pedido-service` vía Feign.

```bash
curl http://localhost:8083/api/productos/1
```

**200 OK:** `ApiResponse<ProductoResponseDTO>`. **Errores:** `404` (no existe o inactivo)

---

### 4. Actualizar Producto — `PUT /api/productos/{id}`

**Request Body:** mismo esquema que `POST`.

```bash
curl -X PUT http://localhost:8083/api/productos/1 \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Silla ergonómica premium",
    "descripcion": "Silla con soporte lumbar y reposacabezas",
    "categoria": "Mobiliario",
    "precioBase": 119990.0
  }'
```

**200 OK:** `ApiResponse<ProductoResponseDTO>`. **Errores:** `404`, `409` (nombre duplicado)

---

### 5. Desactivar Producto (Soft Delete) — `DELETE /api/productos/{id}`

No elimina el registro; establece `activo = false`. Preserva la integridad referencial con `pedido-service`.

```bash
curl -X DELETE http://localhost:8083/api/productos/1
```

**200 OK:** `ApiResponse<Void>`. **Errores:** `404`

---

### 6. Healthcheck — `GET /api/productos/ping`

```bash
curl http://localhost:8083/api/productos/ping
```

```json
{ "mensaje": "producto-service activo", "data": null, "exitoso": true, "timestamp": "..." }
```

---

## Manejo de Errores

| Excepción | HTTP | Causa |
|---|---|---|
| `ResponseStatusException` | Variable | Errores de negocio (404, 409) |
| `MethodArgumentNotValidException` | 400 | Validación `@Valid` fallida |
| `Exception` | 500 | Error inesperado |
