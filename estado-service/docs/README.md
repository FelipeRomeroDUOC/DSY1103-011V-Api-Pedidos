# estado-service

> Módulo del monorepo **Api Pedidos** · Puerto `8085`

## Descripción

`estado-service` es un microservicio pasivo orientado exclusivamente a la auditoría. Registra el historial inmutable de cambios de estado de cada pedido, proporcionando trazabilidad total del ciclo de vida operativo.
Cada vez que el servicio orquestador (`pedido-service`) realiza una transición de estado en un pedido, emite una notificación HTTP asíncrona hacia este servicio para que almacene el movimiento.

## Stack Tecnológico

| Tecnología | Versión / Detalle |
|---|---|
| Java | 21 |
| Spring Boot | 3.4.4 |
| Spring Web | REST Controllers |
| Spring Data JPA + Hibernate | Persistencia |
| Lombok | `@Getter`, `@Setter`, `@RequiredArgsConstructor` |
| Bean Validation | Validación estricta de DTOs |
| H2 Database | Base de datos local en archivo (`estado_service.mv.db`) |

> A diferencia de otros servicios del ecosistema, este **no** implementa un cliente OpenFeign, ya que es un servicio puramente receptor y no requiere extraer información de otras capas.

## Estructura de Paquetes

```
estado-service/
└── src/main/java/cl/apipedidos/
    ├── EstadoServiceApplication.java
    ├── config/
    │   └── ApiExceptionHandler.java          ← Centraliza errores 400 y 500
    └── estado/
        ├── controller/
        │   └── EstadoController.java         ← Endpoints GET/POST
        ├── service/
        │   ├── EstadoService.java
        │   └── EstadoServiceImpl.java        ← Lógica de persistencia de trazabilidad
        ├── repository/
        │   └── CambioEstadoRepository.java   ← Consultas ordenadas por timestamp
        ├── entity/
        │   └── CambioEstado.java             ← Entidad inmutable
        └── dto/
            ├── ApiResponse.java              ← Wrapper de respuesta genérico
            ├── CambioEstadoRequestDTO.java   ← Inbound payload
            └── CambioEstadoResponseDTO.java  ← Outbound payload
```

## Ejecución

```bash
# Desde la raíz del monorepo
./mvnw spring-boot:run -pl estado-service

# O con el script de arranque general en Windows
start-all.bat
```

## Convenciones Aplicadas

- ✅ El diseño respeta el principio Fire-And-Forget desde el lado emisor, evitando cuellos de botella en el ecosistema.
- ✅ Los registros son inmutables (Append-Only): la API no expone métodos PUT, PATCH o DELETE, asegurando la fiabilidad de la auditoría.
- ✅ Los estados persisten como Strings para lograr desacoplamiento total: si `pedido-service` añade un nuevo estado futuro, `estado-service` no requiere recompilación.
