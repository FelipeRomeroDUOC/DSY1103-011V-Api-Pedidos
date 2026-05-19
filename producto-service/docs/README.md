# producto-service

> Módulo del monorepo **Api Pedidos** · Puerto `8083`

## Descripción

`producto-service` mantiene el catálogo de productos disponibles para incluir en pedidos. Es consultado por `pedido-service` vía OpenFeign para verificar la existencia y el precio base de un producto antes de crear o confirmar un pedido.

## Stack Tecnológico

| Tecnología | Versión / Detalle |
|---|---|
| Java | 21 |
| Spring Boot | 3.4.4 |
| Spring Web | REST Controllers |
| Spring Data JPA + Hibernate | Persistencia |
| Lombok | `@Getter`, `@Setter`, `@RequiredArgsConstructor` |
| Bean Validation | `@Valid`, `@NotBlank`, `@NotNull`, `@DecimalMin` |
| H2 Database (archivo) | Desarrollo local |

## Estructura de Paquetes

```
producto-service/
└── src/main/java/cl/apipedidos/
    ├── ProductoServiceApplication.java
    ├── config/
    │   └── ApiExceptionHandler.java
    └── producto/
        ├── controller/
        │   └── ProductoController.java
        ├── service/
        │   ├── ProductoService.java          ← interfaz
        │   └── ProductoServiceImpl.java
        ├── repository/
        │   └── ProductoRepository.java
        ├── entity/
        │   └── Producto.java
        └── dto/
            ├── ApiResponse.java
            ├── ProductoRequestDTO.java
            └── ProductoResponseDTO.java
```

## Ejecución

```bash
# Desde la raíz del monorepo
./mvnw spring-boot:run -pl producto-service

# O con el script de arranque general
start-all.bat
```

## Convenciones Aplicadas

- ✅ Respuestas envueltas en `ApiResponse<T>`
- ✅ Inyección de dependencias con `@RequiredArgsConstructor`
- ✅ Entidades con `@Getter`/`@Setter` (sin `@Data`)
- ✅ IDs auto-generados con `GenerationType.IDENTITY`
- ✅ Lifecycle callbacks: `beforeCreate()` / `beforeUpdate()`
- ✅ Soft delete (`activo = false`) en lugar de eliminación física
