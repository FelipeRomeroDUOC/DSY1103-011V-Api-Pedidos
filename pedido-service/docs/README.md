# Pedido Service

> Microservicio de gestión de pedidos — parte del ecosistema **Api Pedidos**.

## Descripción General

`pedido-service` es el microservicio central encargado del ciclo de vida completo de los **pedidos**. Gestiona la creación, consulta, actualización de estado y eliminación de pedidos, validando la existencia del cliente asociado contra `cliente-service` mediante OpenFeign.

### Responsabilidades Principales

| Responsabilidad | Descripción |
|---|---|
| **Crear pedidos** | Registra pedidos con sus ítems, calculando monto total automáticamente |
| **Validar clientes** | Verifica existencia del cliente en `cliente-service` antes de crear |
| **Gestionar estados** | Controla la máquina de estados con validación de transiciones |
| **Consultar pedidos** | Por ID, número de pedido, o filtros (estado, tipo, cliente) |
| **Eliminar pedidos** | Con restricciones (no permite eliminar despachados/entregados) |

## Stack Tecnológico

| Tecnología | Versión / Detalle |
|---|---|
| Java | 21+ |
| Spring Boot | 3.4.4 |
| Spring Data JPA | Hibernate como ORM |
| Spring Cloud OpenFeign | Comunicación HTTP con `cliente-service` |
| Lombok | Reducción de boilerplate |
| Bean Validation (Jakarta) | Validación declarativa de DTOs |
| H2 Database | Base de datos embebida (desarrollo) |

## Puerto por Defecto

```
http://localhost:8081
```

## Inicio Rápido

```bash
./mvnw spring-boot:run -pl pedido-service
```

**Requisito:** `cliente-service` levantado en `http://localhost:8082` (o configurar `CLIENTE_SERVICE_URL`).

## Estructura del Módulo

```text
pedido-service/
├── pom.xml
├── data/                            ← Base H2 en archivo
└── src/main/
    ├── java/cl/apipedidos/
    │   ├── PedidoServiceApplication.java    ← Entry point + @EnableFeignClients
    │   ├── config/
    │   │   └── ApiExceptionHandler.java     ← Manejo global de errores
    │   ├── http/                            ← Infraestructura HTTP compartida
    │   │   ├── client/feign/
    │   │   │   ├── ClienteFeignClient.java      ← Interface Feign
    │   │   │   ├── ClienteFeignAdapter.java     ← Wrapper con manejo de errores
    │   │   │   ├── FeignClientConfig.java       ← Config Feign (ErrorDecoder, headers)
    │   │   │   └── FeignErrorDecoder.java       ← Decodificador custom de errores
    │   │   ├── dto/
    │   │   │   └── ApiErrorResponse.java        ← DTO de error estándar
    │   │   └── error/
    │   │       └── HttpClientException.java     ← Excepción HTTP estructurada
    │   └── pedido/
    │       ├── client/
    │       │   └── ClienteServiceClient.java    ← Client de dominio
    │       ├── controller/
    │       │   └── PedidoController.java
    │       ├── dto/
    │       │   ├── CreatePedidoRequest.java
    │       │   ├── CreateItemRequest.java
    │       │   ├── PedidoDTO.java
    │       │   ├── ItemPedidoDTO.java
    │       │   └── UpdateEstadoRequest.java
    │       ├── entity/
    │       │   ├── Pedido.java
    │       │   ├── ItemPedido.java
    │       │   ├── EstadoPedido.java (enum)
    │       │   └── TipoDespacho.java (enum)
    │       ├── repository/
    │       │   ├── PedidoRepository.java
    │       │   └── ItemPedidoRepository.java
    │       └── service/
    │           └── PedidoService.java
    └── resources/
        ├── application.properties
        └── application-h2.properties
```

## Documentación Detallada

| Documento | Descripción |
|---|---|
| [API.md](API.md) | Referencia completa de endpoints REST |
| [ARQUITECTURA.md](ARQUITECTURA.md) | Arquitectura interna y flujos |
| [MODELO_DATOS.md](MODELO_DATOS.md) | Entidades, tablas y relaciones |
| [CONFIGURACION.md](CONFIGURACION.md) | Propiedades y variables de entorno |

## Interacción con Otros Servicios

```text
fabricacion-service (8086) ──Feign──► pedido-service (8081) ──Feign──► cliente-service (8082)
```

`pedido-service` se comunica con `cliente-service` para **validar la existencia del cliente** antes de crear un pedido, y es consumido por `fabricacion-service` para cambios de estado.

---

*Proyecto académico — Arquitectura Fullstack, DuocUC.*
