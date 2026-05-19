# SPEC — `despacho-service`

> DSY1103 · Desarrollo FullStack 1 · Proyecto Semestral — Arquitectura de Microservicios  
> Módulo: `despacho-service` | Puerto: `8084` | BD: `db_despachos`

---

## 1. Descripción general

`despacho-service` registra y gestiona la información de despacho de los pedidos, incluyendo el tipo de envío y el transportista asignado.  
Antes de registrar un despacho, consulta a `pedido-service` para verificar que el pedido existe **y** se encuentra en estado `LISTO`.

---

## 2. Posición en la arquitectura

```
Cliente / Frontend
       │ HTTP REST
       ▼
API Gateway / Load Balancer
       │
       ├──► pedido-service (8081)
       ├──► cliente-service (8082)
       ├──► producto-service (8083)
       └──► despacho-service (8084)  ──Feign──► pedido-service (8081) [verifica estado LISTO]
```

**Comunicación inter-servicio:**

| Dirección | Protocolo | Propósito |
|---|---|---|
| `despacho-service` → `pedido-service` | HTTP GET (Feign) | Verificar que el pedido existe y está en estado `LISTO` antes de registrar un despacho |

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
| Bean Validation | `@Valid`, `@NotNull`, `@NotBlank`, etc. |
| H2 Database (archivo) | Desarrollo local |
| Spring Cloud OpenFeign + OkHttp | Cliente HTTP hacia `pedido-service` |

**Base de datos local (H2 en archivo):**

```
despacho-service/data/despacho_service.mv.db
```

Configurada en `src/main/resources/application-h2.properties`.

---

## 4. Entidades JPA

### 4.1 `Despacho`

```java
@Entity
@Table(name = "despachos")
@Getter @Setter
public class Despacho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long pedidoId;

    @Enumerated(EnumType.STRING)
    @NotNull
    private TipoDespacho tipoDespacho;

    // Nombre del transportista (ej: "Starken", "Paket") o null si es RETIRO/RM
    private String transportista;

    private LocalDate fechaDespacho;

    private String trackingCode;
}
```

### 4.2 `TipoDespacho` (enum)

```java
public enum TipoDespacho {
    RETIRO,
    RM,
    REGION
}
```

> **Nota de diseño:** `Transportista` se modela como `String` en esta entidad para mantener el servicio simple y desacoplado de `transportista-service` (módulo futuro). Si `transportista-service` se implementa, este campo puede evolucionar a una referencia por ID sin rediseño estructural (RNF-07).

---

## 5. Estructura de paquetes

```
despacho-service/
└── src/
    └── main/
        ├── java/com/example/despachoservice/
        │   ├── DespachoServiceApplication.java
        │   ├── controller/
        │   │   └── DespachoController.java
        │   ├── service/
        │   │   ├── DespachoService.java          ← interfaz
        │   │   └── DespachoServiceImpl.java
        │   ├── repository/
        │   │   └── DespachoRepository.java
        │   ├── model/
        │   │   ├── Despacho.java
        │   │   └── TipoDespacho.java
        │   ├── dto/
        │   │   ├── DespachoRequestDTO.java
        │   │   └── DespachoResponseDTO.java
        │   ├── client/
        │   │   └── PedidoFeignClient.java        ← cliente hacia pedido-service
        │   └── response/
        │       └── ApiResponse.java              ← wrapper estándar del proyecto
        └── resources/
            ├── application.properties
            └── application-h2.properties
```

---

## 6. Wrapper de respuesta estándar (`ApiResponse`)

Todos los endpoints devuelven el mismo envoltorio genérico del proyecto:

```json
{
  "mensaje": "Despacho registrado correctamente",
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

**Base URL:** `http://localhost:8084/api/despachos`

| Método | Ruta | Descripción | RF |
|---|---|---|---|
| `POST` | `/api/despachos` | Registrar un nuevo despacho | RF-15, HU-05 |
| `GET` | `/api/despachos` | Listar todos los despachos | RF-11 |
| `GET` | `/api/despachos/{pedidoId}` | Obtener despacho por ID de pedido | RF-13 |
| `GET` | `/api/despachos?tipo={tipo}` | Filtrar despachos por tipo | RF-11, RF-13 |
| `PUT` | `/api/despachos/{id}` | Actualizar información de un despacho | RF-15 |
| `GET` | `/api/despachos/ping` | Healthcheck | RNF-03 |

---

### 7.1 `POST /api/despachos` — Registrar despacho

Verifica mediante Feign que el pedido exista y esté en estado `LISTO` antes de persistir.  
Si el pedido ya tiene un despacho registrado, retorna `409`.

**Request body:**
```json
{
  "pedidoId": 1,
  "tipoDespacho": "REGION",
  "transportista": "Starken",
  "fechaDespacho": "2025-05-20",
  "trackingCode": "STK-00123456"
}
```

**Respuestas:**

| Status | Caso |
|---|---|
| `201 Created` | Despacho registrado. Retorna `ApiResponse<DespachoResponseDTO>` |
| `400 Bad Request` | Campo obligatorio faltante o `tipoDespacho` inválido |
| `404 Not Found` | Pedido no encontrado en `pedido-service` |
| `409 Conflict` | El pedido ya tiene un despacho registrado |
| `422 Unprocessable Entity` | El pedido existe pero no está en estado `LISTO` |

---

### 7.2 `GET /api/despachos` — Listar despachos

Retorna todos los despachos registrados. Acepta filtro opcional por tipo.

**Query params opcionales:**

| Parámetro | Tipo | Ejemplo |
|---|---|---|
| `tipo` | `TipoDespacho` | `RETIRO`, `RM`, `REGION` |

**Ejemplos:**
```
GET /api/despachos              → todos los despachos
GET /api/despachos?tipo=RETIRO  → sólo despachos de tipo RETIRO
GET /api/despachos?tipo=REGION  → sólo despachos a regiones
```

**Respuestas:**

| Status | Caso |
|---|---|
| `200 OK` | Lista de despachos (puede ser vacía) |
| `400 Bad Request` | Valor de `tipo` no pertenece al enum `TipoDespacho` |

---

### 7.3 `GET /api/despachos/{pedidoId}` — Despacho por pedido

Busca el despacho asociado a un `pedidoId` específico.

**Respuestas:**

| Status | Caso |
|---|---|
| `200 OK` | Despacho encontrado |
| `404 Not Found` | No existe despacho para ese `pedidoId` |

---

### 7.4 `PUT /api/despachos/{id}` — Actualizar despacho

Permite corregir el transportista, código de tracking o fecha de despacho.  
No permite cambiar `pedidoId` ni `tipoDespacho` una vez registrado.

**Request body:**
```json
{
  "transportista": "Paket",
  "fechaDespacho": "2025-05-21",
  "trackingCode": "PKT-00987654"
}
```

**Respuestas:**

| Status | Caso |
|---|---|
| `200 OK` | Despacho actualizado |
| `400 Bad Request` | Datos inválidos |
| `404 Not Found` | Despacho no existe |

---

### 7.5 `GET /api/despachos/ping` — Healthcheck

```json
{ "mensaje": "despacho-service activo", "data": null, "exitoso": true, "timestamp": "..." }
```

---

## 8. DTOs

### `DespachoRequestDTO`

```java
public class DespachoRequestDTO {
    @NotNull  private Long pedidoId;
    @NotNull  private TipoDespacho tipoDespacho;
              private String transportista;   // requerido sólo si tipoDespacho = REGION
              private LocalDate fechaDespacho;
              private String trackingCode;
}
```

### `DespachoResponseDTO`

```java
public class DespachoResponseDTO {
    private Long id;
    private Long pedidoId;
    private TipoDespacho tipoDespacho;
    private String transportista;
    private LocalDate fechaDespacho;
    private String trackingCode;
}
```

### `DespachoUpdateDTO` (para `PUT`)

```java
public class DespachoUpdateDTO {
    private String transportista;
    private LocalDate fechaDespacho;
    private String trackingCode;
}
```

---

## 9. Comportamiento de negocio

| Regla | Detalle |
|---|---|
| **Verificación de estado** | Antes de crear un despacho, consultar `GET /api/pedidos/{id}` en `pedido-service`. Si el estado no es `LISTO`, rechazar con `422` |
| **Un despacho por pedido** | Si `pedidoId` ya tiene un despacho, retornar `409 Conflict` (HU-05) |
| **Transportista requerido para REGION** | Si `tipoDespacho = REGION`, el campo `transportista` es obligatorio; para `RETIRO` y `RM` es opcional |
| **Inmutabilidad parcial** | `pedidoId` y `tipoDespacho` no pueden modificarse tras la creación |
| **Filtro por tipo** | `GET /api/despachos?tipo=` con valor inválido retorna `400` con mensaje descriptivo |

---

## 10. Integración con `pedido-service` (cliente Feign)

```java
// En despacho-service
@FeignClient(name = "pedido-service", url = "${pedido.service.url}")
public interface PedidoFeignClient {

    @GetMapping("/api/pedidos/{id}")
    ApiResponse<PedidoResponseDTO> obtenerPedido(@PathVariable Long id);
}
```

**DTO de referencia (sólo los campos necesarios):**
```java
public class PedidoResponseDTO {
    private Long id;
    private String estado;   // "PENDIENTE", "EN_FABRICACION", "LISTO", "DESPACHADO"
    private String tipoDespacho;
}
```

**Propiedad en `despacho-service/application.properties`:**
```properties
pedido.service.url=http://localhost:8081
```

**Flujo al registrar un despacho:**
1. `despacho-service` recibe `POST /api/despachos` con `pedidoId`.
2. Llama `GET /api/pedidos/{pedidoId}` en `pedido-service` vía Feign.
3. Si el pedido no existe → `404`.
4. Si el pedido existe pero `estado ≠ LISTO` → `422`.
5. Si ya existe un `Despacho` con ese `pedidoId` → `409`.
6. Si todas las validaciones pasan → persiste el despacho y retorna `201`.

> **Nota:** La actualización del estado del pedido a `DESPACHADO` puede realizarse en esta misma operación mediante una llamada `PATCH /api/pedidos/{id}/estado` a `pedido-service`, o delegarse al encargado manualmente. Se recomienda implementarlo en la fase de desarrollo para mantener consistencia eventual.

---

## 11. Configuración del módulo Maven

Agregar en el `pom.xml` raíz del monorepo:

```xml
<modules>
    <module>cliente-service</module>
    <module>pedido-service</module>
    <module>fabricacion-service</module>
    <module>producto-service</module>
    <module>despacho-service</module>   <!-- ← agregar -->
</modules>
```

**`despacho-service/pom.xml` (fragmento):**

```xml
<parent>
    <groupId>com.example</groupId>
    <artifactId>api-pedidos</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</parent>

<artifactId>despacho-service</artifactId>
<name>despacho-service</name>

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
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    <!-- OkHttp para soporte de PATCH en Feign -->
    <dependency>
        <groupId>io.github.openfeign</groupId>
        <artifactId>feign-okhttp</artifactId>
    </dependency>
</dependencies>
```

---

## 12. Configuración de properties

**`application.properties`:**
```properties
spring.application.name=despacho-service
server.port=8084
spring.profiles.active=h2

pedido.service.url=http://localhost:8081

# Habilitar OkHttp en Feign (igual que los otros módulos)
spring.cloud.openfeign.okhttp.enabled=true
```

**`application-h2.properties`:**
```properties
spring.datasource.url=jdbc:h2:file:./data/despacho_service;AUTO_SERVER=TRUE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

---

## 13. Habilitación de Feign en la aplicación principal

```java
@SpringBootApplication
@EnableFeignClients
public class DespachoServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DespachoServiceApplication.class, args);
    }
}
```

---

## 14. Script de arranque

Agregar al `start-all.bat` existente:

```bat
start "despacho-service" cmd /k "mvnw.cmd spring-boot:run -pl despacho-service"
```

Y en terminal Linux/Mac:
```bash
./mvnw spring-boot:run -pl despacho-service
```

---

## 15. Requerimientos cubiertos

| RF / RNF | Cobertura |
|---|---|
| RF-11 | Clasificar pedidos según tipo de despacho (RETIRO, RM, REGION) |
| RF-13 | Filtrar pedidos listos según ubicación logística |
| RF-14 | Generar listado de pedidos listos para retiro (`GET /api/despachos?tipo=RETIRO`) |
| RF-15 | Registrar información de despacho |
| HU-04 | Filtrar pedidos listos por tipo de despacho |
| HU-05 | Registrar despacho con validación de estado LISTO y control de duplicados (409) |
| RNF-01 | Códigos HTTP adecuados en todos los endpoints |
| RNF-02 | Bean Validation + mensajes descriptivos vía `@ControllerAdvice` |
| RNF-03 | Módulo independiente, desplegable de forma autónoma en puerto 8084 |
| RNF-05 | Consultas simples sobre H2 con JPA — latencia esperada < 50 ms |
| RNF-06 | Verbos HTTP estándar REST, rutas de recursos, respuesta JSON |

---

## 16. Checklist de implementación

- [ ] Crear módulo `despacho-service/` en el monorepo
- [ ] Agregar `<module>despacho-service</module>` en `pom.xml` raíz
- [ ] Implementar enum `TipoDespacho` (RETIRO / RM / REGION)
- [ ] Implementar entidad `Despacho` con validación de unicidad por `pedidoId`
- [ ] Implementar `DespachoRepository` (JPA)
- [ ] Implementar `PedidoFeignClient` para verificar estado del pedido
- [ ] Implementar `DespachoService` / `DespachoServiceImpl` con lógica de negocio
- [ ] Implementar `DespachoController` con los 6 endpoints
- [ ] Implementar `ApiResponse<T>` (copiar desde otro módulo)
- [ ] Agregar `@ControllerAdvice` para manejo de excepciones (404, 409, 422, 400)
- [ ] Anotar `DespachoServiceApplication` con `@EnableFeignClients`
- [ ] Configurar `application.properties` y `application-h2.properties`
- [ ] Actualizar `start-all.bat` con el nuevo servicio
- [ ] Probar flujo completo: crear pedido → marcar LISTO → registrar despacho → intentar duplicado (409)
