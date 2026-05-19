# Cliente Service

> Microservicio de gestión de clientes y ubicaciones territoriales — parte del ecosistema **Api Pedidos**.

## Descripción General

`cliente-service` es el microservicio base del ecosistema, encargado de dos dominios:

1. **Clientes**: CRUD completo de clientes con validación de RUT chileno, unicidad de nombre/RUT y resolución inteligente de comunas (con soporte de acentos).
2. **Ubicaciones**: Catálogo completo de regiones, provincias y comunas de Chile, cargado automáticamente desde un JSON al iniciar.

### Responsabilidades Principales

| Responsabilidad | Descripción |
|---|---|
| **CRUD de clientes** | Crear, listar, buscar, actualizar y eliminar clientes |
| **Búsqueda flexible** | Buscar por ID numérico o por nombre (case-insensitive) |
| **Validación RUT** | Rango 1.000.000 - 999.999.999 + dígito verificador |
| **Resolución de comunas** | Búsqueda exacta e insensible a acentos |
| **Catálogo territorial** | Regiones, provincias y comunas de Chile |
| **Auto-carga de datos** | Las ubicaciones se importan desde JSON al primer inicio |

## Stack Tecnológico

| Tecnología | Versión / Detalle |
|---|---|
| Java | 21+ |
| Spring Boot | 3.4.4 |
| Spring Data JPA | Hibernate como ORM |
| Lombok | Reducción de boilerplate |
| Bean Validation (Jakarta) | Validación declarativa |
| H2 Database | Base de datos embebida (desarrollo) |
| MySQL Connector | Driver disponible (producción) |
| PostgreSQL Driver | Driver disponible (producción) |

## Puerto por Defecto

```
http://localhost:8082
```

## Inicio Rápido

```bash
./mvnw spring-boot:run -pl cliente-service
```

> Este servicio **no depende** de otros microservicios. Puede levantarse de forma independiente.

## Estructura del Módulo

```text
cliente-service/
├── pom.xml
├── data/                                ← Base H2 en archivo
└── src/main/
    ├── java/cl/apipedidos/
    │   ├── ClienteServiceApplication.java
    │   ├── config/
    │   │   └── ApiExceptionHandler.java
    │   ├── cliente/                     ← Dominio de Clientes
    │   │   ├── config/                  ← (vacío, reservado)
    │   │   ├── controller/
    │   │   │   └── ClienteController.java
    │   │   ├── dto/
    │   │   │   ├── ClienteCreateRequestDTO.java
    │   │   │   ├── ClienteUpdateRequestDTO.java
    │   │   │   └── ClienteResponseDTO.java
    │   │   ├── entity/
    │   │   │   └── Cliente.java
    │   │   ├── repository/
    │   │   │   └── ClienteRepository.java
    │   │   └── service/
    │   │       └── ClienteService.java
    │   └── ubicacion/                   ← Dominio de Ubicaciones
    │       ├── config/
    │       │   ├── UbicacionDataLoader.java
    │       │   ├── UbicacionSchemaMigrator.java
    │       │   └── ProvinciaCatalog.java
    │       ├── controller/
    │       │   └── UbicacionController.java
    │       ├── dto/
    │       │   ├── RegionResponseDTO.java
    │       │   ├── ProvinciaResponseDTO.java
    │       │   └── ComunaResponseDTO.java
    │       ├── entity/
    │       │   ├── Region.java
    │       │   ├── Provincia.java
    │       │   └── Comuna.java
    │       ├── repository/
    │       │   ├── RegionRepository.java
    │       │   ├── ProvinciaRepository.java
    │       │   └── ComunaRepository.java
    │       └── service/
    │           └── UbicacionService.java
    └── resources/
        ├── application.properties
        ├── application-h2.properties
        └── data/
            └── chile-divisiones-territoriales.json
```

## Documentación Detallada

| Documento | Descripción |
|---|---|
| [API.md](API.md) | Endpoints de Clientes y Ubicaciones |
| [ARQUITECTURA.md](ARQUITECTURA.md) | Arquitectura interna y auto-carga de datos |
| [MODELO_DATOS.md](MODELO_DATOS.md) | Entidades, tablas y relaciones |
| [CONFIGURACION.md](CONFIGURACION.md) | Propiedades y perfiles de base de datos |

## Posición en el Ecosistema

```text
fabricacion-service (8086) ──Feign──► pedido-service (8081) ──Feign──► cliente-service (8082)
```

`cliente-service` es el servicio **más interno** de la cadena. No depende de ningún otro servicio y es consumido por `pedido-service` para validar clientes.

---

*Proyecto académico — Arquitectura Fullstack, DuocUC.*
