# Arquitectura — pedido-service

## Diagrama de Capas

```
┌─────────────────────────────────────────────────┐
│                   Controller                     │
│              PedidoController                    │
├─────────────────────────────────────────────────┤
│                    Service                       │
│               PedidoService                      │
│  (Lógica de negocio, transacciones, validación)  │
├──────────────────┬──────────────────────────────┤
│    Repository    │     Client (dominio)           │
│  PedidoRepository│     ClienteServiceClient       │
│  ItemPedidoRepo  │         │                      │
│  (Spring Data)   │     ClienteFeignAdapter         │
│                  │         │                      │
│                  │     ClienteFeignClient (Feign)  │
├──────────────────┴──────────────────────────────┤
│              Entity / Enum                       │
│  Pedido, ItemPedido, EstadoPedido, TipoDespacho  │
├─────────────────────────────────────────────────┤
│              H2 Database (file)                  │
└─────────────────────────────────────────────────┘
```

## Estructura de Paquetes

```
cl.apipedidos
├── PedidoServiceApplication.java       ← @SpringBootApplication + @EnableFeignClients
├── config/
│   └── ApiExceptionHandler.java        ← @RestControllerAdvice
├── http/                               ← Infraestructura HTTP reutilizable
│   ├── client/feign/
│   │   ├── ClienteFeignClient.java     ← Interface Feign declarativa
│   │   ├── ClienteFeignAdapter.java    ← Wrapper que captura FeignException
│   │   ├── FeignClientConfig.java      ← ErrorDecoder + RequestInterceptor
│   │   └── FeignErrorDecoder.java      ← Deserializa errores a HttpClientException
│   ├── dto/
│   │   └── ApiErrorResponse.java       ← DTO de error estándar
│   └── error/
│       └── HttpClientException.java    ← Excepción con status + body + error parseado
└── pedido/
    ├── client/
    │   └── ClienteServiceClient.java   ← Client de dominio (valida cliente)
    ├── controller/                     ← REST API
    ├── dto/                            ← Records de Request/Response
    ├── entity/                         ← JPA Entities + Enums
    ├── repository/                     ← Spring Data JPA
    └── service/                        ← Lógica de negocio
```

## Patrón de Comunicación Feign (Triple Capa)

El servicio implementa un patrón de **triple capa** para la comunicación HTTP:

```
PedidoService
  └── ClienteServiceClient      (3. Capa de dominio: traduce a ResponseStatusException)
        └── ClienteFeignAdapter  (2. Capa de adaptación: captura FeignException → HttpClientException)
              └── ClienteFeignClient (1. Capa Feign: interface declarativa)
```

1. **`ClienteFeignClient`**: Interface Feign pura con operaciones CRUD sobre `/api/clientes`.
2. **`ClienteFeignAdapter`**: Captura `FeignException` y traduce a `HttpClientException` con status, body y error parseado.
3. **`ClienteServiceClient`**: Capa de dominio que traduce `HttpClientException` a `ResponseStatusException` con semántica de negocio.

### FeignErrorDecoder

Un `ErrorDecoder` custom deserializa el body de error a `ApiErrorResponse` (si es JSON válido), preservando el mensaje original del servicio remoto en la excepción.

### FeignClientConfig

Registra beans globales:
- `ErrorDecoder` → `FeignErrorDecoder`
- `RequestInterceptor` → Headers `Accept: application/json` y `Content-Type: application/json`

## Flujo: Crear Pedido

```
POST /api/pedidos
    │
    ▼
PedidoController.crearPedido()
    │
    ▼
PedidoService.crearPedido()
    ├── 1. Verificar que numeroPedido no esté duplicado
    ├── 2. ClienteServiceClient.validarCliente(clienteId)
    │       └── ClienteFeignAdapter → ClienteFeignClient
    │           └── GET cliente-service/api/clientes/{id}
    ├── 3. Construir Pedido + Items
    ├── 4. Calcular monto total (Σ subtotales)
    └── 5. Persistir y retornar PedidoDTO
```

## Máquina de Estados

```
                  ┌────────────┐
           ┌──────│  PENDIENTE │──────┐
           │      └────────────┘      │
           ▼                          ▼
    ┌──────────────┐          ┌───────────┐
    │EN_FABRICACION│          │ CANCELADO │
    └──────┬───────┘          └───────────┘
           │         ▲
           ▼         │
      ┌─────────┐    │
      │  LISTO  │────┘ (cancelar desde EN_FABRICACION)
      └────┬────┘
           ▼
    ┌──────────────┐
    │  DESPACHADO  │
    └──────┬───────┘
           ▼
    ┌──────────────┐
    │  ENTREGADO   │
    └──────────────┘
```

La validación se implementa con `switch` expressions en `PedidoService.validarTransicionEstado()`. Los estados `CANCELADO` y `ENTREGADO` son **estados finales** sin transiciones permitidas.

## Transaccionalidad

- La clase `PedidoService` está anotada con `@Transactional` a nivel de clase.
- Las operaciones de lectura usan `@Transactional(readOnly = true)` para optimización.
- La eliminación valida restricciones de negocio antes de ejecutar.
