# Modelo de Datos — pedido-service

## Diagrama Entidad-Relación

```
┌───────────────────────────────┐
│            pedidos            │
├───────────────────────────────┤
│ PK  id                BIGINT │
│     numero_pedido     VARCHAR│  ← UNIQUE
│     cliente_id        BIGINT │  ← Ref. lógica a cliente-service
│     estado            VARCHAR│
│     monto             DECIMAL│
│     tipo_despacho     VARCHAR│
│     fecha_creacion  TIMESTAMP│
│     fecha_actualizacion TSTMP│
└──────────────┬────────────────┘
               │ 1
               │ N
┌──────────────┴────────────────┐
│         items_pedido          │
├───────────────────────────────┤
│ PK  id                BIGINT │
│ FK  pedido_id         BIGINT │
│     producto_id       BIGINT │
│     cantidad         INTEGER │
│     precio_unitario  DECIMAL │
│     subtotal         DECIMAL │
└───────────────────────────────┘
```

## Entidades JPA

### `Pedido`

| Campo | Tipo | Columna | Nullable | Notas |
|---|---|---|---|---|
| `id` | `Long` | `id` | NO | PK, auto-generado |
| `numeroPedido` | `String` | `numero_pedido` | NO | UNIQUE, max 80 |
| `clienteId` | `Long` | `cliente_id` | NO | Referencia lógica |
| `estado` | `EstadoPedido` | `estado` | NO | Enum STRING, max 30 |
| `monto` | `BigDecimal` | `monto` | NO | precision 12, scale 2 |
| `tipoDespacho` | `TipoDespacho` | `tipo_despacho` | NO | Enum STRING, max 30 |
| `fechaCreacion` | `LocalDateTime` | `fecha_creacion` | NO | Auto @PrePersist |
| `fechaActualizacion` | `LocalDateTime` | `fecha_actualizacion` | SÍ | Auto @PreUpdate |
| `items` | `List<ItemPedido>` | — | — | @OneToMany, cascade ALL |

**Lifecycle:** `@PrePersist` asigna `fechaCreacion`, `estado=PENDIENTE`, `monto=0`. `@PreUpdate` asigna `fechaActualizacion`.

### `ItemPedido`

| Campo | Tipo | Columna | Nullable | Notas |
|---|---|---|---|---|
| `id` | `Long` | `id` | NO | PK, auto-generado |
| `pedido` | `Pedido` | `pedido_id` | NO | FK, LAZY |
| `productoId` | `Long` | `producto_id` | NO | |
| `cantidad` | `Integer` | `cantidad` | NO | |
| `precioUnitario` | `BigDecimal` | `precio_unitario` | NO | precision 12, scale 2 |
| `subtotal` | `BigDecimal` | `subtotal` | NO | Calculado automáticamente |

**Lifecycle:** `@PrePersist` → `recalcularSubtotal()` = `precioUnitario × cantidad`.

### Enums

**`EstadoPedido`:** `PENDIENTE`, `EN_FABRICACION`, `LISTO`, `DESPACHADO`, `ENTREGADO`, `CANCELADO`

**`TipoDespacho`** (con `@JsonCreator`/`@JsonValue`):

| Valor | Aliases de entrada |
|---|---|
| `RETIRO` | `RETIRO` |
| `RM` | `RM`, `DOMICILIO` |
| `REGION` | `REGION` |

## Repositorios

### `PedidoRepository`

| Método | Descripción |
|---|---|
| `findByNumeroPedido(String)` | Buscar por número correlativo |
| `existsByNumeroPedido(String)` | Verificar duplicado |
| `findByClienteIdOrderByFechaCreacionDesc(Long)` | Historial de cliente |
| `findByEstado(EstadoPedido)` | Filtrar por estado |
| `findByEstadoAndTipoDespacho(...)` | Filtrar por estado + tipo |

### `ItemPedidoRepository`

| Método | Descripción |
|---|---|
| `findByPedidoId(Long)` | Items de un pedido |
