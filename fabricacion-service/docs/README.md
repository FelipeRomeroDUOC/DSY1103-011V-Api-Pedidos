# Fabricacion Service

> Microservicio de gestión de órdenes de fabricación — parte del ecosistema **Api Pedidos**.

## Descripción General

`fabricacion-service` es el microservicio encargado de orquestar el ciclo de vida de las **órdenes de fabricación** asociadas a pedidos. Actúa como puente entre la solicitud comercial (pedido) y el proceso productivo (manufactura), manteniendo trazabilidad completa de cada cambio de estado a través de un historial auditable.

### Responsabilidades Principales

| Responsabilidad | Descripción |
|---|---|
| **Crear órdenes de fabricación** | Asocia una orden productiva a un pedido existente, validando su existencia vía `pedido-service`. |
| **Gestionar estados** | Controla la transición de estados (`EN_PROCESO` → `TERMINADO` / `PAUSADO`) con registro histórico. |
| **Notificar al pedido-service** | Al crear o finalizar una orden, notifica automáticamente al servicio de pedidos para sincronizar estados. |
| **Auditoría de cambios** | Cada transición de estado queda registrada en la tabla `historial_fabricacion` con usuario, motivo y timestamp. |

## Stack Tecnológico

| Tecnología | Versión / Detalle |
|---|---|
| Java | 21+ |
| Spring Boot | 3.4.x |
| Spring Data JPA | Hibernate como ORM |
| Spring Cloud OpenFeign | Comunicación HTTP con `pedido-service` |
| OkHttp | Transport para Feign (soporte completo de `PATCH`) |
| Lombok | Reducción de boilerplate |
| Bean Validation (Jakarta) | Validación declarativa de DTOs |
| H2 Database | Base de datos embebida (perfil de desarrollo) |

## Puerto por Defecto

```
http://localhost:8086
```

## Inicio Rápido

### Desde la raíz del monorepo (`Api_Pedidos/`)

```bash
./mvnw spring-boot:run -pl fabricacion-service
```

### Requisitos previos

- **Java 21** o superior instalado
- `pedido-service` levantado en `http://localhost:8081` (o configurar la variable `PEDIDO_SERVICE_URL`)

### Verificar que el servicio está activo

```bash
curl http://localhost:8086/api/fabricacion/ping
```

Respuesta esperada:
```json
{
  "mensaje": "pong",
  "data": "fabricacion-service",
  "exitoso": true,
  "timestamp": "2026-05-18T12:00:00"
}
```

## Estructura del Módulo

```text
fabricacion-service/
├── pom.xml                                  ← Dependencias del módulo
├── data/                                    ← Base de datos H2 en archivo
│   └── fabricacion_service.mv.db
├── docs/                                    ← Esta documentación
└── src/main/
    ├── java/cl/apipedidos/
    │   ├── FabricacionServiceApplication.java   ← Entry point
    │   └── fabricacion/
    │       ├── client/        ← Clientes Feign para pedido-service
    │       ├── config/        ← Configuraciones (Kafka placeholder, WebClient)
    │       ├── controller/    ← REST Controllers
    │       ├── dto/           ← Objetos de transferencia (Request/Response)
    │       ├── entity/        ← Entidades JPA
    │       ├── exception/     ← Excepciones de dominio
    │       ├── handler/       ← Manejo global de errores
    │       ├── repository/    ← Repositorios Spring Data JPA
    │       └── service/       ← Lógica de negocio
    └── resources/
        ├── application.properties              ← Config general
        ├── application-h2.properties            ← Perfil H2
        └── db/migration/
            └── V1__fabricacion_initial_schema.sql  ← Schema DDL
```

## Documentación Detallada

| Documento | Descripción |
|---|---|
| [API.md](API.md) | Referencia completa de endpoints REST con ejemplos `curl` |
| [ARQUITECTURA.md](ARQUITECTURA.md) | Arquitectura interna, paquetes y flujos de comunicación |
| [MODELO_DATOS.md](MODELO_DATOS.md) | Esquema de base de datos, entidades JPA y relaciones |
| [CONFIGURACION.md](CONFIGURACION.md) | Propiedades, perfiles y variables de entorno |

## Interacción con Otros Servicios

```text
fabricacion-service (8086) ──Feign──► pedido-service (8081) ──Feign──► cliente-service (8082)
```

El servicio se comunica **exclusivamente** con `pedido-service` a través de OpenFeign para:
1. **Validar existencia** del pedido antes de crear una orden.
2. **Notificar cambio de estado** del pedido a `EN_FABRICACION` (al crear la orden).
3. **Notificar finalización** del pedido a `LISTO` (al marcar la orden como `TERMINADO`).

---

*Proyecto académico — Arquitectura Fullstack, DuocUC.*
