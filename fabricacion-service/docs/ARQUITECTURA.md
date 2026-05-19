# Arquitectura — fabricacion-service

## Visión General

`fabricacion-service` sigue una arquitectura **en capas** (Layered Architecture) con separación clara de responsabilidades. Se comunica con `pedido-service` mediante **OpenFeign** sobre HTTP REST.

## Diagrama de Capas

```
┌─────────────────────────────────────────────────┐
│                   Controller                     │
│         OrdenFabricacionController               │
│  (Recibe requests, delega al Service)            │
├─────────────────────────────────────────────────┤
│                    Service                       │
│          OrdenFabricacionService                 │
│  (Lógica de negocio, transacciones)              │
├──────────────────┬──────────────────────────────┤
│    Repository    │         Client                │
│  OrdenFabricacion│   PedidoServiceClient          │
│  HistorialFabric.│   PedidoFeignClient            │
│  (Spring Data)   │   (Comunicación HTTP)          │
├──────────────────┴──────────────────────────────┤
│                   Entity                         │
│  OrdenFabricacion, HistorialFabricacion           │
│  EstadoFabricacion (enum)                        │
├─────────────────────────────────────────────────┤
│              H2 Database (file)                  │
└─────────────────────────────────────────────────┘
```

## Estructura de Paquetes

```
cl.apipedidos
├── FabricacionServiceApplication.java    ← @SpringBootApplication + @EnableFeignClients
└── fabricacion/
    ├── client/          ← Clientes HTTP hacia otros microservicios
    │   ├── PedidoFeignClient.java        ← Interface Feign declarativa
    │   └── PedidoServiceClient.java      ← Wrapper con manejo de errores
    ├── config/          ← Beans de configuración
    │   ├── KafkaConfig.java              ← Placeholder para Kafka
    │   └── WebClientConfig.java          ← Bean WebClient
    ├── controller/      ← REST Controllers
    │   └── OrdenFabricacionController.java
    ├── dto/             ← Data Transfer Objects
    │   ├── ApiResponse.java              ← Wrapper genérico de respuesta
    │   ├── OrdenFabricacionRequest.java  ← Request para crear orden
    │   ├── OrdenFabricacionResponse.java ← Response de orden
    │   ├── PedidoDTO.java                ← DTO para deserializar pedido
    │   └── UpdateEstadoFabricacionRequest.java
    ├── entity/          ← Entidades JPA
    │   ├── EstadoFabricacion.java        ← Enum de estados
    │   ├── HistorialFabricacion.java     ← Auditoría de cambios
    │   └── OrdenFabricacion.java         ← Entidad principal
    ├── exception/       ← Excepciones de dominio
    │   ├── FabricacionException.java     ← Excepción genérica
    │   └── PedidoNoEncontradoException.java
    ├── handler/         ← Manejo global de excepciones
    │   └── GlobalExceptionHandler.java   ← @RestControllerAdvice
    ├── repository/      ← Repositorios Spring Data JPA
    │   ├── HistorialFabricacionRepository.java
    │   └── OrdenFabricacionRepository.java
    └── service/         ← Lógica de negocio
        └── OrdenFabricacionService.java
```

## Flujo: Crear Orden de Fabricación

```
Cliente HTTP (Postman, Frontend, etc.)
    │
    ▼
OrdenFabricacionController.crearOrden()
    │
    ▼
OrdenFabricacionService.crearOrden()
    ├── 1. PedidoServiceClient.validarExistencia(pedidoId)
    │       └── PedidoFeignClient.obtenerPedido() → GET pedido-service
    │
    ├── 2. Verificar duplicado en OrdenFabricacionRepository
    │
    ├── 3. Persistir OrdenFabricacion (estado: EN_PROCESO)
    │
    ├── 4. Registrar HistorialFabricacion (null → EN_PROCESO)
    │
    └── 5. PedidoServiceClient.notificarPedidoEnFabricacion()
            └── PedidoFeignClient.actualizarEstado() → PATCH pedido-service
```

## Flujo: Actualizar Estado (TERMINADO)

```
PATCH /api/fabricacion/{id}/estado
    │
    ▼
OrdenFabricacionService.actualizarEstado()
    ├── 1. Buscar orden por ID
    ├── 2. Cambiar estado + fechaFin
    ├── 3. PedidoServiceClient.notificarPedidoListo()
    │       └── PATCH pedido-service → estado "LISTO"
    ├── 4. Persistir cambios
    └── 5. Registrar HistorialFabricacion
```

## Comunicación Inter-Servicio

### Arquitectura del Cliente Feign

El servicio implementa un patrón de **doble capa** para la comunicación con `pedido-service`:

1. **`PedidoFeignClient`** (Interface): Declaración Feign pura con las operaciones HTTP.
2. **`PedidoServiceClient`** (Component): Wrapper que encapsula `PedidoFeignClient` y agrega manejo granular de errores Feign.

```
OrdenFabricacionService
    └── PedidoServiceClient (manejo de errores)
            └── PedidoFeignClient (declaración Feign)
                    └── pedido-service (HTTP)
```

### Manejo de errores Feign

`PedidoServiceClient` traduce las excepciones Feign a excepciones del dominio:

| FeignException | Excepción local | HTTP resultante |
|---|---|---|
| `FeignException.NotFound` | `PedidoNoEncontradoException` | 404 |
| `FeignException.BadRequest` | `FabricacionException` | 400 |
| `FeignException.Unauthorized/Forbidden` | `ResponseStatusException` | 502 |
| `RetryableException` | `ResponseStatusException` | 502 |
| `FeignException` (genérica) | `ResponseStatusException` | 502 |

### Configuración OkHttp

Feign usa **OkHttp** como transporte HTTP en lugar de `HttpURLConnection` de Java, habilitado en `application-h2.properties`:

```properties
spring.cloud.openfeign.okhttp.enabled=true
```

Esto es **necesario** porque `HttpURLConnection` no soporta el verbo `PATCH`.

## Transaccionalidad

Las operaciones de escritura (`crearOrden`, `actualizarEstado`) están marcadas con `@Transactional`. Si la llamada Feign falla **después** de persistir la orden, los cambios locales se revierten automáticamente.

## Configuraciones Preparadas (No activas)

| Componente | Estado | Descripción |
|---|---|---|
| `KafkaConfig` | Placeholder | Preparado para mensajería asíncrona futura |
| `WebClientConfig` | Bean registrado | WebClient disponible para llamadas reactivas |
