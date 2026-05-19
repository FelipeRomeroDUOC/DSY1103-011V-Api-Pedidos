# API Reference — cliente-service

> Base URL: `http://localhost:8082`

## Formato de Error Estándar

```json
{
  "status": 404,
  "message": "Cliente no encontrado",
  "path": "/api/clientes/99",
  "timestamp": "2026-05-18T12:00:00-04:00",
  "errors": null
}
```

---

## Endpoints de Clientes

### 1. Crear Cliente — `POST /api/clientes`

**Request Body (`ClienteCreateRequestDTO`):**

| Campo | Tipo | Requerido | Validación |
|---|---|---|---|
| `nombreCl` | `String` | ✅ | `@NotBlank`, max 150 |
| `rutCl` | `Long` | ✅ | 1.000.000 – 999.999.999 |
| `divCl` | `String` | ✅ | 1-2 chars, regex `^[0-9Kk]{1,2}$` |
| `direccionCl` | `String` | ✅ | `@NotBlank`, max 200 |
| `emailCl` | `String` | ✅ | `@Email`, max 254 |
| `telefonoCl` | `String` | ✅ | `@NotBlank`, max 30 |
| `comuna` | `String` | ✅ | Nombre de comuna (max 120) |

```bash
curl -X POST http://localhost:8082/api/clientes \
  -H "Content-Type: application/json" \
  -d '{
    "nombreCl": "Ana Pérez",
    "rutCl": 12345678,
    "divCl": "9",
    "direccionCl": "Av. Providencia 123",
    "emailCl": "ana@ejemplo.cl",
    "telefonoCl": "+56912345678",
    "comuna": "Providencia"
  }'
```

**201 Created:** Header `Location: /api/clientes/{id}`

```json
{
  "idCliente": 1,
  "nombreCl": "Ana Pérez",
  "rutCl": 12345678,
  "divCl": "9",
  "direccionCl": "Av. Providencia 123",
  "emailCl": "ana@ejemplo.cl",
  "telefonoCl": "+56912345678",
  "comuna": "Providencia",
  "provincia": "Santiago",
  "region": "Metropolitana de Santiago",
  "fechaRegistro": "18/05/2026"
}
```

**Errores:** `400` (validación, comuna no encontrada), `409` (nombre/RUT duplicado)

---

### 2. Listar Clientes — `GET /api/clientes`

| Parámetro | Tipo | Descripción |
|---|---|---|
| `comuna` | `String` | Filtrar por nombre de comuna (opcional) |

```bash
curl "http://localhost:8082/api/clientes?comuna=Providencia"
```

**200 OK:** `List<ClienteResponseDTO>`

---

### 3. Obtener Cliente — `GET /api/clientes/{identificador}`

Busca por **ID numérico** o por **nombre** (case-insensitive).

```bash
# Por ID
curl http://localhost:8082/api/clientes/1

# Por nombre
curl "http://localhost:8082/api/clientes/Ana%20Pérez"
```

**200 OK:** `ClienteResponseDTO`. **Errores:** `404`

---

### 4. Actualizar Cliente — `PUT /api/clientes/{id}`

Body idéntico a `ClienteCreateRequestDTO`. Valida unicidad de nombre/RUT excluyendo el propio registro.

```bash
curl -X PUT http://localhost:8082/api/clientes/1 \
  -H "Content-Type: application/json" \
  -d '{
    "nombreCl": "Ana Pérez González",
    "rutCl": 12345678,
    "divCl": "9",
    "direccionCl": "Av. Providencia 456",
    "emailCl": "ana.new@ejemplo.cl",
    "telefonoCl": "+56912345678",
    "comuna": "Las Condes"
  }'
```

**200 OK:** `ClienteResponseDTO`. **Errores:** `404`, `409` (duplicado)

---

### 5. Eliminar Cliente — `DELETE /api/clientes/{id}`

```bash
curl -X DELETE http://localhost:8082/api/clientes/1
```

**204 No Content.** **Errores:** `404`

---

## Endpoints de Ubicaciones

### 6. Listar Regiones — `GET /api/regiones`

```bash
curl http://localhost:8082/api/regiones
```

**200 OK:**
```json
[
  { "idRegion": "01", "nombreRegion": "Tarapacá" },
  { "idRegion": "02", "nombreRegion": "Antofagasta" }
]
```

---

### 7. Provincias por Región — `GET /api/regiones/{idRegion}/provincias`

```bash
curl http://localhost:8082/api/regiones/13/provincias
```

**200 OK:**
```json
[
  { "idProvincia": "131", "nombreProvincia": "Santiago", "idRegion": "13", "nombreRegion": "Metropolitana de Santiago" }
]
```

**Errores:** `404` (región no encontrada)

---

### 8. Comunas por Región — `GET /api/regiones/{idRegion}/comunas`

```bash
curl http://localhost:8082/api/regiones/13/comunas
```

**200 OK:**
```json
[
  { "idComuna": "13101", "nombreComuna": "Santiago", "idRegion": "13", "nombreRegion": "Metropolitana de Santiago" }
]
```

**Errores:** `404` (región no encontrada)

---

## Manejo de Errores

| Excepción | HTTP | Causa |
|---|---|---|
| `ResponseStatusException` | Variable | Errores de negocio |
| `MethodArgumentNotValidException` | 400 | Validación `@Valid` fallida |
| `Exception` | 500 | Error inesperado |

## Resolución de Comunas

El servicio resuelve el nombre de la comuna con búsqueda en dos pasos:
1. **Coincidencia exacta** (case-insensitive) via `findByNombreComunaIgnoreCase`
2. **Coincidencia sin acentos**: Normaliza NFD y elimina diacríticos para match fuzzy

Esto permite que tanto `"Providencia"` como `"providencia"` o `"Ñuñoa"` y `"Nunoa"` sean resueltos correctamente.
