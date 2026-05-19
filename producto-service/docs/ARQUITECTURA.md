# Arquitectura — producto-service

## Diagrama de Capas

```
┌────────────────────────────────────────────────┐
│                   REST Layer                    │
│              ProductoController                 │
│  (POST, GET, PUT, DELETE, PATCH /api/productos) │
├────────────────────────────────────────────────┤
│                 Service Layer                   │
│  ProductoService (interface)                    │
│  ProductoServiceImpl (implementación)           │
├────────────────────────────────────────────────┤
│              Repository Layer                   │
│            ProductoRepository (JPA)             │
├────────────────────────────────────────────────┤
│              Persistence Layer                  │
│            Producto (Entity)                    │
│          H2 Database (file-based)               │
└────────────────────────────────────────────────┘
```

## Estructura de Paquetes

```
cl.apipedidos/
├── ProductoServiceApplication.java
├── config/
│   └── ApiExceptionHandler.java            ← @RestControllerAdvice global
└── producto/
    ├── controller/
    │   └── ProductoController.java         ← 7 endpoints REST
    ├── service/
    │   ├── ProductoService.java            ← Contrato (interfaz)
    │   └── ProductoServiceImpl.java        ← Lógica de negocio
    ├── repository/
    │   └── ProductoRepository.java         ← JPA queries derivadas
    ├── entity/
    │   └── Producto.java                   ← @Entity con soft delete
    └── dto/
        ├── ApiResponse.java                ← Wrapper estándar
        ├── ProductoRequestDTO.java         ← Request (record)
        └── ProductoResponseDTO.java        ← Response (record)
```

## Flujo de una Petición

```
Cliente HTTP
    │
    ▼
ProductoController.crear()
    │ @Valid @RequestBody
    ▼
ProductoServiceImpl.crear()
    │ Validación de unicidad de nombre
    │ Mapeo DTO → Entity
    ▼
ProductoRepository.save()
    │ @PrePersist → beforeCreate()
    ▼
H2 Database (INSERT)
    │
    ▼
Entity → DTO → ApiResponse.success()
    │
    ▼
ResponseEntity<ApiResponse<ProductoResponseDTO>>
```

## Comunicación Inter-Servicio

`producto-service` es un **proveedor** de datos. No consume otros servicios.

| Consumidor | Dirección | Verbo | Ruta | Propósito |
|---|---|---|---|---|
| `pedido-service` | → `producto-service` | GET | `/api/productos/{id}` | Verificar existencia y precio al crear pedido |

## Patrones Aplicados

| Patrón | Implementación |
|---|---|
| **Controller → Service → Repository** | Capas desacopladas con inyección vía `@RequiredArgsConstructor` |
| **DTO Pattern** | Records inmutables para request/response, separados de la entidad JPA |
| **Soft Delete** | `DELETE` establece `activo = false`; el listado filtra por `activo = true` |
| **ApiResponse Wrapper** | Todas las respuestas envueltas en formato estándar del monorepo |
| **Global Exception Handler** | `@RestControllerAdvice` centralizado en `ApiExceptionHandler` |
