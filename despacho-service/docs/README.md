# despacho-service

> Módulo del monorepo **Api Pedidos** · Puerto `8084`

## Descripción

`despacho-service` gestiona la información de despacho (envío o entrega) de los pedidos registrados en el sistema. 
Su principal responsabilidad es asegurar que solo los pedidos que hayan alcanzado el estado `LISTO` puedan ser despachados. Para esto, se comunica de forma sincrónica con `pedido-service` (vía OpenFeign) antes de registrar cualquier operación.

## Stack Tecnológico

| Tecnología | Versión / Detalle |
|---|---|
| Java | 21 |
| Spring Boot | 3.4.4 |
| Spring Web | REST Controllers |
| Spring Cloud OpenFeign | Cliente HTTP hacia `pedido-service` (vía OkHttp) |
| Spring Data JPA + Hibernate | Persistencia |
| Lombok | `@Getter`, `@Setter`, `@RequiredArgsConstructor` |
| Bean Validation | Validación de DTOs |
| H2 Database | Base de datos local en archivo |

## Estructura de Paquetes

```
despacho-service/
└── src/main/java/cl/apipedidos/
    ├── DespachoServiceApplication.java
    ├── config/
    │   └── ApiExceptionHandler.java          ← @RestControllerAdvice global
    └── despacho/
        ├── controller/
        │   └── DespachoController.java       ← Endpoints REST
        ├── service/
        │   ├── DespachoService.java          ← Interfaz de negocio
        │   └── DespachoServiceImpl.java      ← Implementación y lógica Feign
        ├── repository/
        │   └── DespachoRepository.java       ← JPA Repository
        ├── entity/
        │   ├── Despacho.java                 ← Entidad de persistencia
        │   └── TipoDespacho.java             ← Enum (RETIRO, RM, REGION)
        ├── client/
        │   ├── PedidoFeignClient.java        ← Cliente para consultar estado de pedidos
        │   └── PedidoResponseDTO.java        ← DTO parcial para leer respuestas del pedido
        └── dto/
            ├── ApiResponse.java              ← Wrapper de respuesta estándar
            ├── DespachoRequestDTO.java       ← Request (Creación)
            ├── DespachoUpdateDTO.java        ← Request (Actualización parcial)
            └── DespachoResponseDTO.java      ← Response unificado
```

## Ejecución

```bash
# Desde la raíz del monorepo
./mvnw spring-boot:run -pl despacho-service

# O con el script de arranque general en Windows
start-all.bat
```

## Convenciones Aplicadas

- ✅ Arquitectura limpia separada en capas (Controller → Service → Repository).
- ✅ Respuestas consistentes bajo el formato estándar `ApiResponse<T>`.
- ✅ Inyección de dependencias por constructor mediante `@RequiredArgsConstructor`.
- ✅ Interconexión HTTP validada contra fallas y errores asimétricos (Feign Client con OkHttp).
- ✅ Reglas de negocio estrictas: bloqueo `409` para despachos duplicados y `422` si el estado es prematuro.
