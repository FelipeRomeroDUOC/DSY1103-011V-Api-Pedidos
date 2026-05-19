# Modelo de Datos — estado-service

El servicio tiene una única entidad persistida en una base de datos local H2.

## Entidad: `CambioEstado`

> Mapeada a la tabla `cambios_estado` en el motor relacional.

| Columna | Tipo SQL | Descripción |
|---|---|---|
| `id` | `BIGINT AUTO_INCREMENT` | PK principal. Identificador del salto de estado. |
| `pedidoId` | `BIGINT NOT NULL` | Agrupador para armar el historial de un pedido determinado. |
| `estadoAnterior` | `VARCHAR(255) NOT NULL` | El estado que tenía el pedido antes de la mutación. |
| `estadoNuevo` | `VARCHAR(255) NOT NULL` | El nuevo estado al que se cambió el pedido. |
| `fechaCambio` | `TIMESTAMP` | Marca temporal automática que genera `estado-service` apenas recibe el HTTP POST. |
| `usuarioId` | `BIGINT` | (Opcional). PK del hipotético trabajador/cliente que originó el cambio. Preparado para escalamiento Auth. |

## Consultas Críticas (JPA)

El sistema soporta el requerimiento esencial usando la anotación genérica en el Repository:

```java
List<CambioEstado> findByPedidoIdOrderByFechaCambioAsc(Long pedidoId);
```

Esto garantiza que una llamada a la API devuelva las trazas exactamente en el orden lógico en que ocurrieron sin requerir ordenamiento del lado de Spring Web (ahorro de RAM).
