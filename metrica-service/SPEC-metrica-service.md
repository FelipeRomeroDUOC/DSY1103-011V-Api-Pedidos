# SPEC — `metrica-service`

> DSY1103 · Desarrollo FullStack 1 · Proyecto Semestral — Arquitectura de Microservicios  
> Módulo: `metrica-service` | Puerto: `8086` | BD: `db_metricas`

---

## 1. Descripción general

`metrica-service` genera indicadores comerciales consultando datos de `pedido-service` y `cliente-service` en tiempo real mediante Feign.  
Calcula y expone: monto total pagado por cliente, frecuencia de compra, ranking de clientes por monto y productos más vendidos en un período.

Este servicio cubre los requerimientos RF-16, RF-17 y RF-18, y es el principal insumo del **Encargado comercial** para tomar decisiones de producción y marketing segmentado.

---

## 2. Posición en la arquitectura

```
Cliente / Frontend
       │ HTTP REST
       ▼
API Gateway / Load Balancer
       │
       └──► metrica-service (8086)
                 │
                 ├──Feign──► pedido-service (8081)   [agrega pedidos por cliente y por producto]
                 └──Feign──► cliente-service (8082)  [obtiene nombre y datos del cliente]
```

**Comunicación inter-servicio:**

| Dirección | Protocolo | Propósito |
|---|---|---|
| `metrica-service` → `pedido-service` | HTTP GET (Feign) | Obtener pedidos por cliente, por producto y por período |
| `metrica-service` → `cliente-service` | HTTP GET (Feign) | Obtener datos del cliente para enriquecer la respuesta |

---

## 3. Stack tecnológico

| Tecnología | Versión / detalle |
|---|---|
| Java | 21+ |
| Spring Boot | 3.4.x |
| Spring Web | REST controllers |
| Spring Data JPA + Hibernate | Persistencia de snapshots de métricas |
| Lombok | `@Getter`, `@Setter`, `@RequiredArgsConstructor` |
| Bean Validation | `@Valid`, `@NotNull` |
| H2 Database (archivo) | Desarrollo local |
| Spring Cloud OpenFeign + OkHttp | Clientes HTTP hacia `pedido-service` y `cliente-service` |

**Base de datos local (H2 en archivo):**

```
metrica-service/data/metrica_service.mv.db
```

Configurada en `src/main/resources/application-h2.properties`.

> **Nota de diseño:** `metrica-service` opera principalmente como **agregador en tiempo real**: consulta datos desde `pedido-service` y `cliente-service` vía Feign y calcula las métricas al vuelo. Las entidades JPA (`MetricaCliente`, `MetricaProducto`) se usan opcionalmente como caché/snapshot para evitar recalcular en cada request, pero no son la fuente de verdad.

---

## 4. Entidades JPA

### 4.1 `MetricaCliente` (snapshot/caché opcional)

```java
@Entity
@Table(name = "metricas_cliente")
@Getter @Setter
public class MetricaCliente {

    @Id
    private Long clienteId;          // mismo ID que en cliente-service

    private Double montoTotal;
    private Integer cantidadPedidos;
    private Double frecuenciaAnual;  // cantidadPedidos / años desde primer pedido
    private LocalDateTime ultimaActualizacion;
}
```

### 4.2 `MetricaProducto` (snapshot/caché opcional)

```java
@Entity
@Table(name = "metricas_producto")
@Getter @Setter
public class MetricaProducto {

    @Id
    private Long productoId;         // mismo ID que en producto-service

    private String nombre;
    private Integer totalVendido;
    private String periodo;          // ej: "2025-05" o "2025-Q1"
    private LocalDateTime ultimaActualizacion;
}
```

> En la fase actual del proyecto ambas entidades son opcionales. Los endpoints calculan métricas directamente desde `pedido-service`. Las tablas pueden usarse para persistir resultados previos y acelerar consultas repetidas.

---

## 5. Estructura de paquetes

```
metrica-service/
└── src/
    └── main/
        ├── java/com/example/metricaservice/
        │   ├── MetricaServiceApplication.java
        │   ├── controller/
        │   │   └── MetricaController.java
        │   ├── service/
        │   │   ├── MetricaService.java           ← interfaz
        │   │   └── MetricaServiceImpl.java
        │   ├── repository/
        │   │   ├── MetricaClienteRepository.java
        │   │   └── MetricaProductoRepository.java
        │   ├── model/
        │   │   ├── MetricaCliente.java
        │   │   └── MetricaProducto.java
        │   ├── client/
        │   │   ├── PedidoFeignClient.java        ← consulta pedidos
        │   │   └── ClienteFeignClient.java        ← consulta datos de cliente
        │   ├── dto/
        │   │   ├── MetricaClienteResponseDTO.java
        │   │   ├── MetricaProductoResponseDTO.java
        │   │   ├── ResumenVentasResponseDTO.java
        │   │   ├── PedidoResponseDTO.java         ← DTO de referencia de pedido-service
        │   │   └── ClienteResponseDTO.java        ← DTO de referencia de cliente-service
        │   └── response/
        │       └── ApiResponse.java              ← wrapper estándar del proyecto
        └── resources/
            ├── application.properties
            └── application-h2.properties
```

---

## 6. Wrapper de respuesta estándar (`ApiResponse`)

```json
{
  "mensaje": "Métricas obtenidas correctamente",
  "data": { ... },
  "exitoso": true,
  "timestamp": "2025-05-18T12:00:00"
}
```

---

## 7. Endpoints REST

**Base URL:** `http://localhost:8086/api/metricas`

| Método | Ruta | Descripción | RF / HU |
|---|---|---|---|
| `GET` | `/api/metricas/clientes/{id}` | Métricas de un cliente específico | RF-16, RF-17, HU-06 |
| `GET` | `/api/metricas/clientes/ranking` | Ranking de clientes por monto total | RF-16 |
| `GET` | `/api/metricas/productos/top` | Productos más vendidos (con filtro de período) | RF-18, HU-07 |
| `GET` | `/api/metricas/ventas` | Resumen de ventas por período | RF-16 |
| `GET` | `/api/metricas/ping` | Healthcheck | RNF-03 |

---

### 7.1 `GET /api/metricas/clientes/{id}` — Métricas por cliente

Retorna monto total acumulado, cantidad de pedidos y frecuencia anual de compra de un cliente.  
Si el cliente no tiene pedidos registrados, retorna los campos numéricos en cero (HU-06).

**Flujo interno:**
1. Llama `GET /api/clientes/{id}` en `cliente-service` para validar existencia y obtener nombre.
2. Llama `GET /api/pedidos?clienteId={id}` en `pedido-service` para obtener todos sus pedidos.
3. Calcula `montoTotal`, `cantidadPedidos` y `frecuenciaAnual` en memoria.

**Respuesta exitosa (`200 OK`):**
```json
{
  "mensaje": "Métricas del cliente obtenidas correctamente",
  "data": {
    "clienteId": 5,
    "nombreCliente": "Empresa ABC",
    "montoTotal": 450000.0,
    "cantidadPedidos": 12,
    "frecuenciaAnual": 6.0
  },
  "exitoso": true,
  "timestamp": "2025-05-18T12:00:00"
}
```

**Respuestas:**

| Status | Caso |
|---|---|
| `200 OK` | Métricas calculadas (campos en `0` si no hay pedidos, per HU-06) |
| `404 Not Found` | Cliente no existe en `cliente-service` |

---

### 7.2 `GET /api/metricas/clientes/ranking` — Ranking de clientes por monto

Retorna todos los clientes con pedidos, ordenados de mayor a menor por `montoTotal`.

**Query params opcionales:**

| Parámetro | Tipo | Ejemplo | Default |
|---|---|---|---|
| `limite` | Integer | `10` | `10` |

**Respuesta exitosa (`200 OK`):**
```json
{
  "data": [
    { "clienteId": 5, "nombreCliente": "Empresa ABC", "montoTotal": 450000.0, "cantidadPedidos": 12 },
    { "clienteId": 2, "nombreCliente": "Cliente XYZ", "montoTotal": 310000.0, "cantidadPedidos": 8 }
  ]
}
```

**Respuestas:**

| Status | Caso |
|---|---|
| `200 OK` | Ranking retornado (lista vacía si no hay pedidos) |

---

### 7.3 `GET /api/metricas/productos/top` — Productos más vendidos

Retorna los 10 productos con mayor cantidad vendida en el rango de fechas indicado.  
Si no se pasan filtros de fecha, considera el **último mes** (HU-07).

**Query params opcionales:**

| Parámetro | Tipo | Ejemplo | Default |
|---|---|---|---|
| `desde` | `LocalDate` (ISO) | `2025-01-01` | Primer día del mes actual |
| `hasta` | `LocalDate` (ISO) | `2025-05-31` | Día actual |
| `limite` | Integer | `10` | `10` |

**Ejemplo de request:**
```
GET /api/metricas/productos/top?desde=2025-01-01&hasta=2025-05-31
```

**Respuesta exitosa (`200 OK`):**
```json
{
  "data": [
    { "productoId": 3, "nombre": "Silla ergonómica", "totalVendido": 48 },
    { "productoId": 7, "nombre": "Mesa escritorio", "totalVendido": 31 }
  ]
}
```

**Respuestas:**

| Status | Caso |
|---|---|
| `200 OK` | Lista de productos (vacía si no hay ventas en el período) |
| `400 Bad Request` | `desde` posterior a `hasta` |

---

### 7.4 `GET /api/metricas/ventas` — Resumen de ventas por período

Retorna el monto total vendido y cantidad de pedidos en el rango de fechas indicado.

**Query params:**

| Parámetro | Tipo | Ejemplo | Default |
|---|---|---|---|
| `desde` | `LocalDate` (ISO) | `2025-01-01` | Primer día del mes actual |
| `hasta` | `LocalDate` (ISO) | `2025-05-31` | Día actual |

**Respuesta exitosa (`200 OK`):**
```json
{
  "data": {
    "desde": "2025-01-01",
    "hasta": "2025-05-31",
    "montoTotal": 1250000.0,
    "cantidadPedidos": 47
  }
}
```

**Respuestas:**

| Status | Caso |
|---|---|
| `200 OK` | Resumen calculado |
| `400 Bad Request` | `desde` posterior a `hasta` |

---

### 7.5 `GET /api/metricas/ping` — Healthcheck

```json
{ "mensaje": "metrica-service activo", "data": null, "exitoso": true, "timestamp": "..." }
```

---

## 8. DTOs

### `MetricaClienteResponseDTO`

```java
public class MetricaClienteResponseDTO {
    private Long clienteId;
    private String nombreCliente;
    private Double montoTotal;
    private Integer cantidadPedidos;
    private Double frecuenciaAnual;
}
```

### `MetricaProductoResponseDTO`

```java
public class MetricaProductoResponseDTO {
    private Long productoId;
    private String nombre;
    private Integer totalVendido;
}
```

### `ResumenVentasResponseDTO`

```java
public class ResumenVentasResponseDTO {
    private LocalDate desde;
    private LocalDate hasta;
    private Double montoTotal;
    private Integer cantidadPedidos;
}
```

### DTOs de referencia (recibidos desde otros servicios)

```java
// De pedido-service
public class PedidoResponseDTO {
    private Long id;
    private Long clienteId;
    private Double monto;
    private LocalDateTime fechaCreacion;
    private List<ItemPedidoDTO> items;  // contiene productoId y cantidad
}

public class ItemPedidoDTO {
    private Long productoId;
    private String nombreProducto;
    private Integer cantidad;
    private Double precioUnitario;
}

// De cliente-service
public class ClienteResponseDTO {
    private Long id;
    private String nombre;
}
```

---

## 9. Clientes Feign

### `PedidoFeignClient`

```java
@FeignClient(name = "pedido-service", url = "${pedido.service.url}")
public interface PedidoFeignClient {

    @GetMapping("/api/pedidos")
    ApiResponse<List<PedidoResponseDTO>> listarPedidos(
        @RequestParam(required = false) Long clienteId,
        @RequestParam(required = false) String desde,
        @RequestParam(required = false) String hasta
    );
}
```

### `ClienteFeignClient`

```java
@FeignClient(name = "cliente-service", url = "${cliente.service.url}")
public interface ClienteFeignClient {

    @GetMapping("/api/clientes/{id}")
    ApiResponse<ClienteResponseDTO> obtenerCliente(@PathVariable Long id);
}
```

---

## 10. Lógica de cálculo

### Frecuencia anual

```
frecuenciaAnual = cantidadPedidos / añosDesde(fechaPrimerPedido, hoy)
```

Si el cliente lleva menos de un año, se usa `1.0` como denominador mínimo para evitar división por cero.

### Productos más vendidos

Agregar todos los `ItemPedido` de los pedidos en el período, agrupar por `productoId`, sumar `cantidad`, ordenar descendente y tomar los primeros `limite`.

### Resumen de ventas

Sumar el campo `monto` de todos los pedidos cuya `fechaCreacion` esté dentro del rango `[desde, hasta]`.

---

## 11. Comportamiento de negocio

| Regla | Detalle |
|---|---|
| **Sin pedidos → ceros** | `GET /api/metricas/clientes/{id}` retorna `montoTotal=0`, `cantidadPedidos=0`, `frecuenciaAnual=0.0` con `200` si el cliente existe pero no tiene pedidos (HU-06) |
| **Default último mes** | Si `desde`/`hasta` no se pasan en `/productos/top` y `/ventas`, se calcula desde el primer día del mes hasta hoy |
| **`desde` > `hasta`** | Retorna `400` con mensaje descriptivo |
| **Tolerancia a fallos Feign** | Si `cliente-service` no responde, retornar las métricas sin nombre del cliente en lugar de propagar el error |
| **Read-only** | Este servicio no modifica datos en ningún otro servicio. Solo lectura vía Feign |

---

## 12. Configuración del módulo Maven

Agregar en el `pom.xml` raíz:

```xml
<modules>
    ...
    <module>metrica-service</module>   <!-- ← agregar -->
</modules>
```

**`metrica-service/pom.xml` (fragmento):**

```xml
<parent>
    <groupId>com.example</groupId>
    <artifactId>api-pedidos</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</parent>

<artifactId>metrica-service</artifactId>
<name>metrica-service</name>

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
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.openfeign</groupId>
        <artifactId>feign-okhttp</artifactId>
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

## 13. Configuración de properties

**`application.properties`:**
```properties
spring.application.name=metrica-service
server.port=8086
spring.profiles.active=h2

pedido.service.url=http://localhost:8081
cliente.service.url=http://localhost:8082

spring.cloud.openfeign.okhttp.enabled=true
```

**`application-h2.properties`:**
```properties
spring.datasource.url=jdbc:h2:file:./data/metrica_service;AUTO_SERVER=TRUE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

---

## 14. Aplicación principal

```java
@SpringBootApplication
@EnableFeignClients
public class MetricaServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MetricaServiceApplication.class, args);
    }
}
```

---

## 15. Script de arranque

Agregar al `start-all.bat` existente:

```bat
start "metrica-service" cmd /k "mvnw.cmd spring-boot:run -pl metrica-service"
```

Y en terminal Linux/Mac:
```bash
./mvnw spring-boot:run -pl metrica-service
```

---

## 16. Requerimientos cubiertos

| RF / RNF | Cobertura |
|---|---|
| RF-16 | Consultar el monto total pagado por cliente |
| RF-17 | Consultar frecuencia de compra por cliente |
| RF-18 | Consultar productos más vendidos |
| HU-06 | Métricas por cliente con campos en cero si no hay pedidos |
| HU-07 | Top productos más vendidos con filtro de período y default último mes |
| RNF-01 | Códigos HTTP adecuados en todos los endpoints |
| RNF-02 | Validación de parámetros con mensajes descriptivos (`desde > hasta` → 400) |
| RNF-03 | Módulo independiente, desplegable de forma autónoma en puerto 8086 |
| RNF-05 | Agregación en memoria sobre datos Feign — latencia controlada |
| RNF-06 | Verbos HTTP estándar REST, rutas de recursos, respuesta JSON |

---

## 17. Checklist de implementación

- [ ] Crear módulo `metrica-service/` en el monorepo
- [ ] Agregar `<module>metrica-service</module>` en `pom.xml` raíz
- [ ] Implementar entidades `MetricaCliente` y `MetricaProducto` (opcionales/caché)
- [ ] Implementar `PedidoFeignClient` con endpoint de listado filtrado
- [ ] Implementar `ClienteFeignClient` con obtención por ID
- [ ] Implementar `MetricaService` / `MetricaServiceImpl` con lógica de agregación
- [ ] Implementar `MetricaController` con los 5 endpoints
- [ ] Manejar tolerancia a fallos en llamadas Feign (`try/catch` o fallback)
- [ ] Validar que `desde` no sea posterior a `hasta` en los endpoints de período
- [ ] Aplicar default de último mes cuando no se pasan parámetros de fecha
- [ ] Implementar `ApiResponse<T>` (copiar desde otro módulo)
- [ ] Agregar `@ControllerAdvice` para manejo de excepciones (400, 404)
- [ ] Anotar `MetricaServiceApplication` con `@EnableFeignClients`
- [ ] Configurar `application.properties` y `application-h2.properties`
- [ ] Actualizar `start-all.bat` con el nuevo servicio
- [ ] Probar flujo completo: crear pedidos → consultar métricas por cliente → verificar ranking → verificar top productos
