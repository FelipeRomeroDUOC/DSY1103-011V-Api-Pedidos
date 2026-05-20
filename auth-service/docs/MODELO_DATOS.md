# Modelo de Datos - Auth Service

El `auth-service` administra su propia base de datos, completamente aislada de los otros microservicios.
La base de datos se crea localmente en el archivo `data/auth_service.mv.db`.

## Entidad Principal: `Usuario`

Representa a un empleado o sistema externo autorizado para operar en el ecosistema.

| Campo | Tipo | Restricciones | Descripción |
|---|---|---|---|
| `id` | Long | PK, AutoIncremental | Identificador único del usuario |
| `nombre` | String | Not Null, Max 100 | Nombre completo |
| `email` | String | Not Null, Unique, Max 100 | Usado como "username" en el login |
| `password` | String | Not Null, Max 255 | Contraseña encriptada en **BCrypt** |
| `rol` | Enum | Not Null | Perfil de autorización |
| `activo` | Boolean | Not Null | Estado de vigencia en el sistema |

### Roles Permitidos (Enum `Usuario.Rol`)
- `ADMIN`: Administrador global de todos los servicios.
- `ENCARGADO_PEDIDOS`: Acceso a la gestión de pedidos y clientes.
- `ENCARGADO_DESPACHO`: Acceso a la logística y rutas.
- `COMERCIAL`: Lectura de métricas y estados.
