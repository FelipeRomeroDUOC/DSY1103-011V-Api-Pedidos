# SPEC — `producto-service`

> DSY1103 · Desarrollo FullStack 1 · Proyecto Semestral — Arquitectura de Microservicios  
> Módulo: `producto-service` | Puerto: `8083` | BD: `db_productos`

---

## 1. Descripción general

`producto-service` mantiene el catálogo de productos disponibles para incluir en pedidos.  
Es consultado por `pedido-service` para verificar la existencia y el precio base de un producto antes de crear o confirmar un pedido.

---

## 2. Posición en la arquitectura

```
Cliente / Frontend
       │ HTTP REST
       ▼
API Gateway / Load Balancer
       │
       ├──► pedido-service (8081)  ──Feign──► producto-service (8083)  [verifica existencia]
       ├──► cliente-service (8082)
       └──► producto-service (8083)
```

**Comunicación inter-servicio:**

| Dirección | Protocolo | Propósito |
|---|---|---|
| `pedido-service` → `producto-service` | HTTP GET (Feign) | Verificar existencia y precio de un producto al crear un pedido |

---

## 3. Stack tecnológico

Consistente con el resto del monorepo:

| Tecnología | Versión / detalle |
|---|---|
| Java | 21+ |
| Spring Boot | 3.4.x |
| Spring Web | REST controllers |
| Spring Data JPA + Hibernate | Persistencia |
| Lombok | `@Getter`, `@Setter`, `@RequiredArgsConstructor` |
| Bean Validation | `@Valid`, `@NotBlank`, `@NotNull`, etc. |
| H2 Database (archivo) | Desarrollo local |
| Spring Cloud OpenFeign + OkHttp | Cliente HTTP hacia otros servicios (si aplica) |

**Base de datos local (H2 en archivo):**

```
producto-service/data/producto_service.mv.db
```

Configurada en `src/main/resources/application-h2.properties`.

---

## 4. Entidad JPA

### `Producto`

```java
@Entity
@Table(name = "productos")
@Getter @Setter
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nombre;

    private String descripcion;

    @NotBlank
    private String categoria;

    @NotNull
    @DecimalMin("0.0")
    private Double precioBase;

    private boolean activo = true;
}
```

> **Nota JPA:** La relación `@OneToMany → ItemPedido` es una referencia externa.  
> `producto-service` **no** posee la entidad `ItemPedido`; esta reside en `pedido-service`.  
> La referencia se mantiene únicamente por `productoId` (Long) en el lado del pedido.

---

## 5. Estructura de paquetes

```
producto-service/
└── src/
    └── main/
        ├── java/com/example/productoservice/
        │   ├── ProductoServiceApplication.java
        │   ├── controller/
        │   │   └── ProductoController.java
        │   ├── service/
        │   │   ├── ProductoService.java          ← interfaz
        │   │   └── ProductoServiceImpl.java
        │   ├── repository/
        │   │   └── ProductoRepository.java
        │   ├── model/
        │   │   └── Producto.java
        │   ├── dto/
        │   │   ├── ProductoRequestDTO.java
        │   │   └── ProductoResponseDTO.java
        │   └── response/
        │       └── ApiResponse.java              ← wrapper estándar del proyecto
        └── resources/
            ├── application.properties
            └── application-h2.properties
```

---

## 6. Wrapper de respuesta estándar (`ApiResponse`)

Todos los endpoints devuelven el mismo envoltorio genérico que usa el resto del proyecto:

```json
{
  "mensaje": "Producto obtenido correctamente",
  "data": { ... },
  "exitoso": true,
  "timestamp": "2025-05-18T12:00:00"
}
```

```java
public class ApiResponse<T> {
    private String mensaje;
    private T data;
    private boolean exitoso;
    private LocalDateTime timestamp;
}
```

---

## 7. Endpoints REST

**Base URL:** `http://localhost:8083/api/productos`

| Método | Ruta | Descripción | RF |
|---|---|---|---|
| `POST` | `/api/productos` | Crear un nuevo producto | RF-09 |
| `GET` | `/api/productos` | Listar todos los productos activos | RF-09 |
| `GET` | `/api/productos/{id}` | Obtener un producto por ID | RF-09 |
| `PUT` | `/api/productos/{id}` | Actualizar datos de un producto | RF-09 |
| `DELETE` | `/api/productos/{id}` | Desactivar un producto (soft delete) | RF-09 |
| `GET` | `/api/productos/ping` | Healthcheck | RNF-03 |

### 7.1 `POST /api/productos` — Crear producto

**Request body:**
```json
{
  "nombre": "Silla ergonómica",
  "descripcion": "Silla de oficina con soporte lumbar",
  "categoria": "Mobiliario",
  "precioBase": 89990.0
}
```

**Respuestas:**

| Status | Caso |
|---|---|
| `201 Created` | Producto creado exitosamente. Retorna `ApiResponse<ProductoResponseDTO>` |
| `400 Bad Request` | Campo obligatorio faltante o inválido |

---

### 7.2 `GET /api/productos` — Listar productos

Retorna todos los productos con `activo = true`.

**Respuestas:**

| Status | Caso |
|---|---|
| `200 OK` | Lista de productos (puede ser vacía) |

---

### 7.3 `GET /api/productos/{id}` — Obtener producto por ID

Usado por `pedido-service` vía Feign para verificar existencia antes de crear un pedido.

**Respuestas:**

| Status | Caso |
|---|---|
| `200 OK` | Producto encontrado |
| `404 Not Found` | Producto no existe o está inactivo |

---

### 7.4 `PUT /api/productos/{id}` — Actualizar producto

**Request body:** mismo esquema que `POST`.

**Respuestas:**

| Status | Caso |
|---|---|
| `200 OK` | Producto actualizado |
| `400 Bad Request` | Datos inválidos |
| `404 Not Found` | Producto no existe |

---

### 7.5 `DELETE /api/productos/{id}` — Desactivar producto (soft delete)

No elimina el registro de la base de datos; establece `activo = false`.  
Esto preserva la integridad referencial con los `ItemPedido` existentes en `pedido-service`.

**Respuestas:**

| Status | Caso |
|---|---|
| `200 OK` | Producto desactivado exitosamente |
| `404 Not Found` | Producto no existe |

---

### 7.6 `GET /api/productos/ping` — Healthcheck

```json
{ "mensaje": "producto-service activo", "data": null, "exitoso": true, "timestamp": "..." }
```

---

## 8. DTOs

### `ProductoRequestDTO`

```java
public class ProductoRequestDTO {
    @NotBlank private String nombre;
    private String descripcion;
    @NotBlank private String categoria;
    @NotNull @DecimalMin("0.0") private Double precioBase;
}
```

### `ProductoResponseDTO`

```java
public class ProductoResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private String categoria;
    private Double precioBase;
    private boolean activo;
}
```

---

## 9. Comportamiento de negocio

| Regla | Detalle |
|---|---|
| **Soft delete** | `DELETE` nunca borra el registro; sólo pone `activo = false` |
| **Listado filtrado** | `GET /api/productos` sólo retorna productos con `activo = true` |
| **Verificación externa** | `GET /api/productos/{id}` retorna `404` si el producto no existe **o** está inactivo, para que `pedido-service` rechace ítems inválidos |
| **Precio no negativo** | `precioBase` debe ser `≥ 0.0`; se valida con Bean Validation |

---

## 10. Integración con `pedido-service` (cliente Feign)

`pedido-service` declara un cliente Feign para consultar este servicio:

```java
// En pedido-service
@FeignClient(name = "producto-service", url = "${producto.service.url}")
public interface ProductoFeignClient {

    @GetMapping("/api/productos/{id}")
    ApiResponse<ProductoResponseDTO> obtenerProducto(@PathVariable Long id);
}
```

**Propiedad en `pedido-service/application.properties`:**
```properties
producto.service.url=http://localhost:8083
```

**Flujo al crear un pedido:**
1. `pedido-service` recibe `POST /api/pedidos` con lista de `items` (`productoId`, `cantidad`).
2. Por cada ítem, llama `GET /api/productos/{productoId}` en `producto-service`.
3. Si algún producto retorna `404` o `activo = false`, `pedido-service` rechaza la creación con `400`.
4. Si todos existen, el pedido se persiste con `precioUnitario` tomado del `precioBase` retornado.

---

## 11. Configuración del módulo Maven

Agregar en el `pom.xml` raíz del monorepo:

```xml
<modules>
    <module>cliente-service</module>
    <module>pedido-service</module>
    <module>fabricacion-service</module>
    <module>producto-service</module>   <!-- ← agregar -->
</modules>
```

**`producto-service/pom.xml` (fragmento):**

```xml
<parent>
    <groupId>com.example</groupId>
    <artifactId>api-pedidos</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</parent>

<artifactId>producto-service</artifactId>
<name>producto-service</name>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

---

## 12. Configuración de properties

**`application.properties`:**
```properties
spring.application.name=producto-service
server.port=8083
spring.profiles.active=h2
```

**`application-h2.properties`:**
```properties
spring.datasource.url=jdbc:h2:file:./data/producto_service;AUTO_SERVER=TRUE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

---

## 13. Script de arranque

Agregar al `start-all.bat` existente:

```bat
start "producto-service" cmd /k "mvnw.cmd spring-boot:run -pl producto-service"
```

Y en terminal Linux/Mac:
```bash
./mvnw spring-boot:run -pl producto-service
```

---

## 14. Requerimientos cubiertos

| RF / RNF | Cobertura |
|---|---|
| RF-09 | Registrar productos dentro de un pedido (catálogo base) |
| RF-18 | Consultar productos más vendidos (provee datos de referencia a `metrica-service`) |
| RNF-01 | Códigos HTTP adecuados en todos los endpoints |
| RNF-02 | Bean Validation + mensajes de error descriptivos vía `@ControllerAdvice` |
| RNF-03 | Módulo independiente, desplegable de forma autónoma en puerto 8083 |
| RNF-05 | Consultas simples sobre H2 con JPA — latencia esperada < 50 ms |
| RNF-06 | Verbos HTTP estándar REST, rutas de recursos, respuesta JSON |

---

## 15. Checklist de implementación

- [ ] Crear módulo `producto-service/` en el monorepo
- [ ] Agregar `<module>producto-service</module>` en `pom.xml` raíz
- [ ] Implementar entidad `Producto` con soft delete (`activo`)
- [ ] Implementar `ProductoRepository` (JPA)
- [ ] Implementar `ProductoService` / `ProductoServiceImpl`
- [ ] Implementar `ProductoController` con los 6 endpoints
- [ ] Implementar `ApiResponse<T>` (o copiar desde otro módulo)
- [ ] Configurar `application.properties` y `application-h2.properties`
- [ ] Agregar `ProductoFeignClient` en `pedido-service` y propiedad `producto.service.url`
- [ ] Actualizar `start-all.bat` con el nuevo servicio
- [ ] Probar flujo completo: crear producto → crear pedido que lo referencia
