# Modelo de Datos — metrica-service

La base de datos de este servicio **no es fuente de verdad**, sino un volcado de persistencia pasiva (Snapshots). Sirve de registro histórico post-cálculo de inteligencia de negocios.

## Entidad: `MetricaCliente`

> Mapeada a la tabla `metricas_cliente`.

| Columna | Tipo SQL | Descripción |
|---|---|---|
| `clienteId` | `BIGINT` | PK Principal. Mismo ID que posee en `cliente-service`. |
| `montoTotal` | `DOUBLE` | La sumatoria en dinero calculada. |
| `cantidadPedidos` | `INT` | Total de tickets / facturas emitidas para esta persona. |
| `frecuenciaAnual` | `DOUBLE` | Pedidos divididos por el tiempo de permanencia. |
| `ultimaActualizacion` | `TIMESTAMP` | Marca temporal del momento en que se procesó el dato. |

## Entidad: `MetricaProducto`

> Mapeada a la tabla `metricas_producto`.

| Columna | Tipo SQL | Descripción |
|---|---|---|
| `productoId` | `BIGINT` | PK Principal. Mismo ID que posee en `producto-service`. |
| `nombre` | `VARCHAR(255)` | Cache del nombre descriptivo. |
| `totalVendido` | `INT` | Unidades físicas comerciadas. |
| `periodo` | `VARCHAR(255)` | Etiqueta de la franja de fecha analizada. |
| `ultimaActualizacion` | `TIMESTAMP` | Marca temporal. |
