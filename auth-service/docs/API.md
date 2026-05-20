# Referencia de API - Auth Service

Esta es la referencia de los endpoints expuestos por el `auth-service`. Todas las rutas base inician con `/api/auth`.

## 1. Endpoints Públicos (No requieren Token)

### 1.1 Ping (Healthcheck)
Verifica que el servicio está vivo.

- **URL:** `/api/auth/ping`
- **Method:** `GET`
- **Auth required:** NO

**Response (200 OK):**
```text
OK
```

### 1.2 Login (Generar Token)
Autentica a un usuario y le entrega su token JWT firmado.

- **URL:** `/api/auth/login`
- **Method:** `POST`
- **Auth required:** NO

**Request Body (application/json):**
```json
{
  "email": "admin@empresa.com",
  "password": "pass123"
}
```

**Response (200 OK):**
```json
{
  "status": "SUCCESS",
  "message": "Login exitoso",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

## 2. Endpoints Privados (Requieren Token `ADMIN`)

Los siguientes endpoints requieren el envío del header:
`Authorization: Bearer <TOKEN>` y que el usuario tenga el rol `ADMIN`.

### 2.1 Crear Usuario
- **URL:** `/api/auth/usuarios`
- **Method:** `POST`

**Request Body:**
```json
{
  "nombre": "Pedro Soto",
  "email": "pedro@empresa.com",
  "password": "mypassword",
  "rol": "COMERCIAL"
}
```

**Response (201 Created):**
```json
{
  "status": "SUCCESS",
  "data": {
    "id": 5,
    "nombre": "Pedro Soto",
    "email": "pedro@empresa.com",
    "rol": "COMERCIAL",
    "activo": true
  }
}
```

### 2.2 Listar Usuarios
- **URL:** `/api/auth/usuarios`
- **Method:** `GET`

**Response (200 OK):**
```json
{
  "status": "SUCCESS",
  "data": [
    {
      "id": 1,
      "nombre": "Administrador",
      "email": "admin@empresa.com",
      "rol": "ADMIN",
      "activo": true
    }
  ]
}
```
