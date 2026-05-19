# Modelo de Datos: Transportista Service

El servicio utiliza una base de datos relacional (H2 por defecto). El modelo está optimizado para consultas rápidas de estado de actividad.

## Tabla: `transportistas`

| Columna | Tipo de Dato | Restricciones | Descripción |
|---------|--------------|---------------|-------------|
| `id` | `BIGINT` | `PK, AUTO_INCREMENT` | Identificador único del transportista. |
| `nombre` | `VARCHAR(100)` | `NOT NULL` | Razón social o nombre comercial del proveedor. |
| `codigo_interno`| `VARCHAR(20)` | `NOT NULL, UNIQUE` | Código único abreviado (ej. STK, CHILEX). |
| `contacto` | `VARCHAR(100)` | `NOT NULL` | Información de contacto (teléfono, email). |
| `regiones_cobertura`| `VARCHAR(255)`| `NOT NULL` | Cadena separada por comas con las regiones admitidas. |
| `activo` | `BOOLEAN` | `NOT NULL, DEFAULT true`| Indica si el proveedor está habilitado para recibir despachos. |

## Población Automática de Datos
Al iniciar el servicio, si no existen registros, se ejecuta un script de inicialización (`data.sql`) que inserta los siguientes proveedores por defecto:
- **Starken** (Código: STK, Cobertura Nacional).
- **Paket** (Código: PKT, Cobertura RM, V y VI).

*(Nota: La ejecución de este script se encuentra diferida hasta que Hibernate construya la tabla mediante `spring.jpa.defer-datasource-initialization=true`)*.
