# Referencia de API: Transportista Service

URL Base: `http://localhost:8088/api/transportistas`

Todas las respuestas exitosas y de error siguen el estándar `ApiResponse<T>`:
```json
{
  "mensaje": "Mensaje descriptivo",
  "data": { ... },
  "exitoso": true,
  "timestamp": "2026-05-19T10:00:00"
}
```

## Endpoints

### 1. Registrar Transportista
- **URL**: `/`
- **Método**: `POST`
- **Descripción**: Crea un nuevo registro de transportista en el sistema.

**Cuerpo (JSON):**
```json
{
  "nombre": "Starken",
  "codigoInterno": "STK",
  "contacto": "+56800123456",
  "regionesCobertura": "I,II,III,IV,V,VI,VII,VIII,IX,X,XI,XII,RM,XIV,XV,XVI"
}
```

**Respuestas Posibles:**
- `201 Created`: Transportista registrado correctamente.
- `400 Bad Request`: Error de validación (por ejemplo, longitud excedida o parámetros nulos).
- `409 Conflict`: Ya existe un transportista activo o inactivo con el mismo `codigoInterno`.

### 2. Listar Transportistas Activos
- **URL**: `/`
- **Método**: `GET`
- **Descripción**: Obtiene una lista de todos los proveedores que tienen el flag `activo=true`.

**Respuestas Posibles:**
- `200 OK`: Listado obtenido exitosamente.

### 3. Obtener Transportista por ID
- **URL**: `/{id}`
- **Método**: `GET`
- **Descripción**: Busca un transportista específico mediante su identificador numérico.

**Respuestas Posibles:**
- `200 OK`: Transportista encontrado.
- `404 Not Found`: No existe un transportista con ese ID.

### 4. Actualizar Transportista
- **URL**: `/{id}`
- **Método**: `PUT`
- **Descripción**: Actualiza los datos de un transportista existente, incluyendo su estado de activación.

**Cuerpo (JSON):**
```json
{
  "nombre": "Starken Cargo",
  "contacto": "+5699999999",
  "regionesCobertura": "RM, V",
  "activo": false
}
```

**Respuestas Posibles:**
- `200 OK`: Transportista actualizado.
- `400 Bad Request`: Error de validación en el payload.
- `404 Not Found`: Transportista no encontrado.

### 5. Healthcheck (Ping)
- **URL**: `/ping`
- **Método**: `GET`
- **Descripción**: Endpoint ligero para verificar que el servicio esté arriba. Retorna un 200 OK estandarizado.
