# Modelo de Datos — fabricacion-service

## Diagrama Entidad-Relación

```
┌─────────────────────────────────┐
│       ordenes_fabricacion       │
├─────────────────────────────────┤
│ PK  id                 BIGINT  │
│     pedido_id           BIGINT  │  ← UNIQUE, referencia lógica a pedido-service
│     estado_fabricacion VARCHAR  │
│     fecha_inicio      TIMESTAMP │
│     fecha_fin         TIMESTAMP │
│     fecha_creacion    TIMESTAMP │
│     fecha_actualizacion TIMESTAMP│
│     descripcion_estado VARCHAR  │
│     usuario_responsable VARCHAR │
└──────────────┬──────────────────┘
               │ 1
               │
               │ N
┌──────────────┴──────────────────┐
│      historial_fabricacion      │
├─────────────────────────────────┤
│ PK  id                 BIGINT  │
│ FK  orden_fabricacion_id BIGINT │  ← ON DELETE CASCADE
│     estado_anterior    VARCHAR  │
│     estado_nuevo       VARCHAR  │
│     fecha_cambio      TIMESTAMP │
│     usuario_id         VARCHAR  │
│     motivo             VARCHAR  │
└─────────────────────────────────┘
```

---

## Tabla: `ordenes_fabricacion`

Almacena las órdenes de fabricación, cada una vinculada a un pedido único.

| Columna | Tipo | Nullable | Default | Descripción |
|---|---|---|---|---|
| `id` | `BIGINT` | NO | Auto-generado (IDENTITY) | PK |
| `pedido_id` | `BIGINT` | NO | — | ID del pedido en `pedido-service` (UNIQUE) |
| `estado_fabricacion` | `VARCHAR(50)` | NO | `'EN_PROCESO'` | Estado actual de la orden |
| `fecha_inicio` | `TIMESTAMP` | NO | `CURRENT_TIMESTAMP` | Inicio de fabricación |
| `fecha_fin` | `TIMESTAMP` | SÍ | `NULL` | Fin de fabricación |
| `fecha_creacion` | `TIMESTAMP` | NO | `CURRENT_TIMESTAMP` | Fecha de creación del registro |
| `fecha_actualizacion` | `TIMESTAMP` | SÍ | `NULL` | Última actualización |
| `descripcion_estado` | `VARCHAR(500)` | SÍ | `NULL` | Descripción libre |
| `usuario_responsable` | `VARCHAR(255)` | NO | — | Usuario a cargo |

**Constraints:**
- `PRIMARY KEY (id)`
- `UNIQUE (pedido_id)` — Un pedido solo puede tener una orden de fabricación

---

## Tabla: `historial_fabricacion`

Registro de auditoría de cada cambio de estado en una orden de fabricación.

| Columna | Tipo | Nullable | Default | Descripción |
|---|---|---|---|---|
| `id` | `BIGINT` | NO | Auto-generado (IDENTITY) | PK |
| `orden_fabricacion_id` | `BIGINT` | NO | — | FK → `ordenes_fabricacion.id` |
| `estado_anterior` | `VARCHAR(50)` | SÍ | `NULL` | Estado antes del cambio |
| `estado_nuevo` | `VARCHAR(50)` | NO | — | Nuevo estado |
| `fecha_cambio` | `TIMESTAMP` | NO | `CURRENT_TIMESTAMP` | Momento del cambio |
| `usuario_id` | `VARCHAR(255)` | SÍ | `NULL` | Usuario que realizó el cambio |
| `motivo` | `VARCHAR(500)` | SÍ | `NULL` | Razón del cambio |

**Constraints:**
- `PRIMARY KEY (id)`
- `FOREIGN KEY (orden_fabricacion_id) REFERENCES ordenes_fabricacion(id) ON DELETE CASCADE`

---

## Enum: `EstadoFabricacion`

```java
public enum EstadoFabricacion {
    EN_PROCESO,   // Producción activa
    TERMINADO,    // Fabricación completada
    PAUSADO       // Temporalmente detenida
}
```

Se almacena como `VARCHAR` (EnumType.STRING) en ambas tablas.

---

## Entidades JPA

### `OrdenFabricacion`

```java
@Entity
@Table(name = "ordenes_fabricacion")
public class OrdenFabricacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_id", nullable = false, unique = true)
    private Long pedidoId;

    @Enumerated(EnumType.STRING)
    private EstadoFabricacion estadoFabricacion;

    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String descripcionEstado;
    private String usuarioResponsable;

    @OneToMany(mappedBy = "ordenFabricacion", cascade = ALL, orphanRemoval = true)
    private List<HistorialFabricacion> historial;
}
```

**Lifecycle Callbacks:**
- `@PrePersist`: Asigna `fechaCreacion`, `fechaInicio` y estado `EN_PROCESO` si son `null`.
- `@PreUpdate`: Actualiza `fechaActualizacion`.

### `HistorialFabricacion`

```java
@Entity
@Table(name = "historial_fabricacion")
public class HistorialFabricacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_fabricacion_id", nullable = false)
    private OrdenFabricacion ordenFabricacion;

    @Enumerated(EnumType.STRING)
    private EstadoFabricacion estadoAnterior;
    @Enumerated(EnumType.STRING)
    private EstadoFabricacion estadoNuevo;

    private LocalDateTime fechaCambio;
    private String usuarioId;
    private String motivo;
}
```

**Lifecycle Callbacks:**
- `@PrePersist`: Asigna `fechaCambio`.

---

## Repositorios

### `OrdenFabricacionRepository`

| Método | Descripción |
|---|---|
| `findByPedidoId(Long)` | Buscar por ID de pedido |
| `findByEstadoFabricacion(Estado)` | Filtrar por estado |
| `findByEstadoFabricacionAndFechaInicioBetween(...)` | Filtrar por estado y rango de fechas |
| `countByEstadoFabricacion(Estado)` | Contar por estado |
| `findAllByOrderByFechaInicioDesc()` | Listar ordenadas por fecha |
| `findOrdenesEnProcesoPorMasDias(LocalDateTime)` | Órdenes en proceso antes de cierta fecha |

### `HistorialFabricacionRepository`

| Método | Descripción |
|---|---|
| `findByOrdenFabricacionId(Long)` | Historial de una orden |
| `findByOrdenFabricacionIdOrderByFechaCambioDesc(Long)` | Historial ordenado |
| `findByEstadoNuevo(Estado)` | Filtrar por estado destino |
| `findHistorialForOrdenes(List<Long>)` | Historial de múltiples órdenes (JPQL) |

---

## DDL (Migración V1)

```sql
CREATE TABLE IF NOT EXISTS ordenes_fabricacion (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    pedido_id BIGINT NOT NULL UNIQUE,
    estado_fabricacion VARCHAR(50) NOT NULL DEFAULT 'EN_PROCESO',
    fecha_inicio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_fin TIMESTAMP NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NULL,
    descripcion_estado VARCHAR(500),
    usuario_responsable VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS historial_fabricacion (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    orden_fabricacion_id BIGINT NOT NULL,
    estado_anterior VARCHAR(50),
    estado_nuevo VARCHAR(50) NOT NULL,
    fecha_cambio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id VARCHAR(255),
    motivo VARCHAR(500),
    FOREIGN KEY (orden_fabricacion_id)
        REFERENCES ordenes_fabricacion(id) ON DELETE CASCADE
);
```
