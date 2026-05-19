# Arquitectura — cliente-service

## Visión General

`cliente-service` gestiona dos dominios separados dentro del mismo módulo:
- **`cliente`**: CRUD de clientes con validación de negocio
- **`ubicacion`**: Catálogo territorial de Chile (solo lectura + auto-carga)

## Diagrama de Capas

```
┌─────────────────────────────────────────────────────┐
│                  Controllers                         │
│    ClienteController         UbicacionController     │
├─────────────────────────────────────────────────────┤
│                   Services                           │
│    ClienteService            UbicacionService        │
├─────────────────────────────────────────────────────┤
│                 Repositories                         │
│  ClienteRepository    RegionRepository               │
│                       ProvinciaRepository             │
│                       ComunaRepository                │
├─────────────────────────────────────────────────────┤
│                   Entities                           │
│  Cliente    Region    Provincia    Comuna             │
├─────────────────────────────────────────────────────┤
│           H2 Database (file)                         │
└─────────────────────────────────────────────────────┘

                    ┌────────────────┐
                    │ UbicacionData  │  ← CommandLineRunner
                    │ Loader         │  ← Carga JSON al inicio
                    └────────────────┘
```

## Estructura de Paquetes

```
cl.apipedidos
├── ClienteServiceApplication.java
├── config/
│   └── ApiExceptionHandler.java         ← @RestControllerAdvice
├── cliente/
│   ├── controller/ClienteController.java
│   ├── dto/
│   │   ├── ClienteCreateRequestDTO.java (record)
│   │   ├── ClienteUpdateRequestDTO.java (record)
│   │   └── ClienteResponseDTO.java      (record)
│   ├── entity/Cliente.java
│   ├── repository/ClienteRepository.java
│   └── service/ClienteService.java
└── ubicacion/
    ├── config/
    │   ├── UbicacionDataLoader.java     ← Auto-carga de datos
    │   ├── UbicacionSchemaMigrator.java ← Migración de schema
    │   └── ProvinciaCatalog.java        ← Mapa de nombres de provincia
    ├── controller/UbicacionController.java
    ├── dto/
    │   ├── RegionResponseDTO.java       (record)
    │   ├── ProvinciaResponseDTO.java    (record)
    │   └── ComunaResponseDTO.java       (record)
    ├── entity/
    │   ├── Region.java
    │   ├── Provincia.java
    │   └── Comuna.java
    ├── repository/
    │   ├── RegionRepository.java
    │   ├── ProvinciaRepository.java
    │   └── ComunaRepository.java
    └── service/UbicacionService.java
```

## Auto-Carga de Ubicaciones

`UbicacionDataLoader` implementa `CommandLineRunner` con `Ordered.HIGHEST_PRECEDENCE`:

```
Inicio de la aplicación
    │
    ▼
UbicacionDataLoader.run()
    ├── ¿regionRepository.count() > 0? → SKIP (datos ya existen)
    │
    └── Leer data/chile-divisiones-territoriales.json
        ├── Parsear regiones
        ├── Derivar provincias (ID = primeros 3 dígitos de comuna)
        │   └── Nombres via ProvinciaCatalog
        ├── Crear comunas
        └── Guardar: regiones → provincias → comunas (orden estricto)
```

**Orden de guardado**: Es crítico respetar `regiones → provincias → comunas` porque JPA con IDs asignados usa `merge()`, y las FK deben existir.

## Flujo: Crear Cliente

```
POST /api/clientes
    │
    ▼
ClienteController.crearCliente()
    │
    ▼
ClienteService.crear()
    ├── 1. Construir entidad Cliente desde DTO
    │       └── resolverComuna(): búsqueda exacta → fallback sin acentos
    ├── 2. Validar unicidad (nombre + RUT)
    └── 3. Persistir y retornar
```

## Resolución de Comunas (Búsqueda Fuzzy)

```
Entrada: "Ñuñoa"
    │
    ├── 1. findByNombreComunaIgnoreCase("Ñuñoa") → ✅ encontrada
    │
    └── (si no) 2. Normalizar NFD + eliminar diacríticos
         "nunoa" → buscar en todas las comunas normalizadas
```

## Relaciones entre Entidades

```
Region (PK: idRegion VARCHAR(2))
   │
   └──< Provincia (PK: idProvincia VARCHAR(3), FK: id_region)
           │
           └──< Comuna (PK: idComuna VARCHAR(5), FK: id_provincia)
                   │
                   └──< Cliente (PK: id_cliente BIGINT, FK: id_comuna)
```

**Notas:**
- Las entidades de ubicación usan **IDs String asignados** (no auto-generados)
- `Cliente` usa **IDENTITY** para auto-generación
- Todas las relaciones son `FetchType.EAGER` en este servicio
- `@PrePersist`/`@PreUpdate` normalizan texto (trim, uppercase para DV)

## Independencia

Este servicio **no tiene dependencias** hacia otros microservicios. Es puramente un proveedor de datos consumido por `pedido-service`.
