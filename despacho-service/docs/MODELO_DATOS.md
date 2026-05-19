# Modelo de Datos — despacho-service

La capa de persistencia se basa en JPA (Hibernate) sobre una base de datos local embebida H2.

## Entidad Principal: `Despacho`

> Mapeada a la tabla `despachos` en la base de datos local.

| Propiedad / Columna | Tipo de Dato (Java) | Tipo de Columna (SQL) | Restricciones / Notas |
|---|---|---|---|
| `id` | `Long` | `BIGINT` | `PRIMARY KEY`, `AUTO_INCREMENT`. Identificador único interno. |
| `pedidoId` | `Long` | `BIGINT` | `@NotNull`. El identificador unívoco proveniente de `pedido-service`. En el servicio se valida lógicamente su unicidad antes del `save()`. |
| `tipoDespacho` | `TipoDespacho` | `VARCHAR(255)` | `@NotNull`, `@Enumerated(EnumType.STRING)`. Define la naturaleza operativa del envío. |
| `transportista` | `String` | `VARCHAR(255)` | Opcional, pero forzado lógicamente por el servicio si `tipoDespacho` es `REGION`. Almacena el nombre (ej: "Starken"). |
| `fechaDespacho` | `LocalDate` | `DATE` | Opcional. Indica qué día programado saldrá (o salió) de la bodega. |
| `trackingCode` | `String` | `VARCHAR(255)` | Opcional. Código alfanumérico para el seguimiento del paquete por el cliente final. |

## Enumeración: `TipoDespacho`

Tipos permitidos en este microservicio. (Nota: `pedido-service` tiene un tipo de despacho asignado, que debe tener paridad lógica).

| Valor | Descripción | Requerimientos Adicionales |
|---|---|---|
| `RETIRO` | El cliente pasa a buscar el producto a la tienda física. | Ninguno. No requiere transportista. |
| `RM` | Despacho interno para la Región Metropolitana (logística propia). | No requiere transportista obligatoriamente. |
| `REGION` | Despacho delegado a agencias de envíos nacionales. | **Exige** un campo `transportista` no nulo ni vacío en el DTO de entrada. |

## Restricciones Lógicas

Aunque `pedidoId` no esté marcado como `@Column(unique=true)` explícitamente en el `@Entity` por motivos de flexibilidad a futuro, el `DespachoServiceImpl` invoca:
```java
if (despachoRepository.existsByPedidoId(request.pedidoId())) {
    throw new ResponseStatusException(HttpStatus.CONFLICT, "El pedido ya tiene un despacho registrado");
}
```
Esto garantiza que **la cardinalidad entre Pedido y Despacho sea 1:1 estricto** en la vida útil actual de la aplicación.
