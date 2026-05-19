# Modelo de Datos — cliente-service

## Diagrama Entidad-Relación

```
┌──────────────────────────┐
│        regiones          │
├──────────────────────────┤
│ PK id_region    VARCHAR2 │
│    nombre_region VARCHAR │
└──────────┬───────────────┘
           │ 1
           │ N
┌──────────┴───────────────┐
│       provincias         │
├──────────────────────────┤
│ PK id_provincia VARCHAR3 │
│    nombre_provincia VCHAR│
│ FK id_region    VARCHAR2 │
└──────────┬───────────────┘
           │ 1
           │ N
┌──────────┴───────────────┐
│        comunas           │
├──────────────────────────┤
│ PK id_comuna    VARCHAR5 │
│    nombre_comuna VARCHAR │
│ FK id_provincia VARCHAR3 │
└──────────┬───────────────┘
           │ 1
           │ N
┌──────────┴───────────────┐
│        clientes          │
├──────────────────────────┤
│ PK id_cliente    BIGINT  │
│    nombre_cl     VARCHAR │
│    rut_cl        BIGINT  │  ← UNIQUE
│    div_cl        VARCHAR │
│    direccion_cl  VARCHAR │
│    email_cl      VARCHAR │
│    telefono_cl   VARCHAR │
│ FK id_comuna     VARCHAR │
│    fecha_registro DATE   │
└──────────────────────────┘
```

---

## Entidades de Ubicación

### `Region`

| Campo | Tipo | Columna | PK/FK | Notas |
|---|---|---|---|---|
| `idRegion` | `String` | `id_region` | PK | VARCHAR(2), asignado |
| `nombreRegion` | `String` | `nombre_region` | — | UNIQUE, max 120 |
| `provincias` | `List<Provincia>` | — | — | @OneToMany, @JsonIgnore |

### `Provincia`

| Campo | Tipo | Columna | PK/FK | Notas |
|---|---|---|---|---|
| `idProvincia` | `String` | `id_provincia` | PK | VARCHAR(3), asignado |
| `nombreProvincia` | `String` | `nombre_provincia` | — | UNIQUE, max 120 |
| `region` | `Region` | `id_region` | FK | EAGER |
| `comunas` | `List<Comuna>` | — | — | @OneToMany, @JsonIgnore |

### `Comuna`

| Campo | Tipo | Columna | PK/FK | Notas |
|---|---|---|---|---|
| `idComuna` | `String` | `id_comuna` | PK | VARCHAR(5), asignado |
| `nombreComuna` | `String` | `nombre_comuna` | — | UNIQUE, max 120 |
| `provincia` | `Provincia` | `id_provincia` | FK | EAGER |
| `clientes` | `List<Cliente>` | — | — | @OneToMany, @JsonIgnore |

> Las entidades de ubicación usan **IDs String** derivados del estándar de codificación territorial chileno.

---

## Entidad `Cliente`

| Campo | Tipo | Columna | Nullable | Validación |
|---|---|---|---|---|
| `idCliente` | `Long` | `id_cliente` | NO | PK, IDENTITY |
| `nombreCl` | `String` | `nombre_cl` | NO | `@NotBlank`, max 150 |
| `rutCl` | `Long` | `rut_cl` | NO | UNIQUE, 1M-999M |
| `divCl` | `String` | `div_cl` | NO | regex `^[0-9Kk]{1,2}$` |
| `direccionCl` | `String` | `direccion_cl` | NO | max 200 |
| `emailCl` | `String` | `email_cl` | NO | max 254 |
| `telefonoCl` | `String` | `telefono_cl` | NO | max 30 |
| `comuna` | `Comuna` | `id_comuna` | NO | FK, EAGER |
| `fechaRegistro` | `LocalDate` | `fecha_registro` | NO | Auto @PrePersist |

**Lifecycle:**
- `@PrePersist`: Asigna `fechaRegistro`, trim de campos, DV a uppercase
- `@PreUpdate`: Trim de campos, DV a uppercase

---

## Repositorios

### `ClienteRepository`

| Método | Descripción |
|---|---|
| `findByNombreClIgnoreCase(String)` | Buscar por nombre exacto |
| `findByComuna_NombreComunaIgnoreCase(String)` | Filtrar por comuna |
| `existsByNombreClIgnoreCase(String)` | Verificar nombre duplicado |
| `existsByNombreClIgnoreCaseAndIdClienteNot(String, Long)` | Unicidad excluyendo propio |
| `existsByRutCl(Long)` | Verificar RUT duplicado |
| `existsByRutClAndIdClienteNot(Long, Long)` | Unicidad excluyendo propio |

### `RegionRepository`

| Método | Descripción |
|---|---|
| `findAllByOrderByIdRegionAsc()` | Listar regiones ordenadas |

### `ProvinciaRepository`

| Método | Descripción |
|---|---|
| `findAllByRegion_IdRegionOrderByNombreProvinciaAsc(String)` | Provincias por región |

### `ComunaRepository`

| Método | Descripción |
|---|---|
| `findByNombreComunaIgnoreCase(String)` | Buscar comuna exacta |
| `findByProvincia_Region_IdRegionOrderByNombreComunaAsc(String)` | Comunas por región |
