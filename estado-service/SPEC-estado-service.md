# SPEC — `estado-service`

> DSY1103 · Desarrollo FullStack 1 · Proyecto Semestral — Arquitectura de Microservicios  
> Módulo: `estado-service` | Puerto: `8085` | BD: `db_estados`

---

## 1. Descripción general

`estado-service` registra el historial completo de cambios de estado de cada pedido, proporcionando trazabilidad total del ciclo de vida operativo.  
Es un servicio **receptor**: `pedido-service` le envía un evento cada vez que actualiza el estado de un pedido vía `PATCH /api/pedidos/{id}/estado`. `estado-service` no modifica pedidos; sólo persiste y expone el historial.

---

## 2. Posición en la arquitectura

```
Cliente / Frontend
       │ HTTP REST
       ▼
API Gateway / Load Balancer
       │
       ├──► pedido-service (8081)  ──POST──► estado-service (8085)  [notifica cambio de estado]
       ├──► cliente-service (8082)
       ├──► producto-service (8083)
       ├──► despacho-service (8084)
       └──► estado-service (8085)
```

**Comunicación inter-servicio:**

| Dirección | Protocolo | Propósito |
|---|---|---|
| `pedido-service` → `estado-service` | HTTP POST (Feign) | Notificar cada cambio de estado de un pedido para registrarlo en el historial |

> `estado-service` es **pasivo**: no inicia llamadas hacia otros servicios. Toda escritura la origina `pedido-service`.

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
| Bean Validation | `@Valid`, `@NotNull`, `@NotBlank` |
| H2 Database (archivo) | Desarrollo local |

> Este servicio **no requiere Feign** porque no realiza llamadas salientes. No es necesario `@EnableFeignClients` ni la dependencia `spring-cloud-starter-openfeign`.

**Base de datos local (H2 en archivo):**

```
estado-service/data/estado_service.mv.db
```

Configurada en `src/main/resources/application-h2.properties`.

---

## 4. Entidad JPA

### `CambioEstado`

```java
@Entity
@Table(name = "cambios_estado")
@Getter @Setter
public class CambioEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long pedidoId;

    @NotBlank
    private String estadoAnterior;   // ej: "PENDIENTE"

    @NotBlank
    private String estadoNuevo;      // ej: "EN_FABRICACION"

    private LocalDateTime fechaCambio;

    private Long usuarioId;          // quién realizó el cambio (puede ser null si no hay auth aún)
}
```

> **Nota de diseño:** `estadoAnterior` y `estadoNuevo` se almacenan como `String` en lugar de enum propio, para evitar acoplamiento con las definiciones de estado de `pedido-service`. Si los estados cambian en `pedido-service`, `estado-service` no requiere modificación (RNF-07).

---

## 5. Estructura de paquetes

```
estado-service/
└── src/
    └── main/
        ├── java/com/example/estadoservice/
        │   ├── EstadoServiceApplication.java
        │   ├── controller/
        │   │   └── EstadoController.java
        │   ├── service/
        │   │   ├── EstadoService.java            ← interfaz
        │   │   └── EstadoServiceImpl.java
        │   ├── repository/
        │   │   └── CambioEstadoRepository.java
        │   ├── model/
        │   │   └── CambioEstado.java
        │   ├── dto/
        │   │   ├── CambioEstadoRequestDTO.java
        │   │   └── CambioEstadoResponseDTO.java
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
  "mensaje": "Cambio de estado registrado correctamente",
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

**Base URL:** `http://localhost:8085/api/estados`

| Método | Ruta | Descripción | RF / RNF |
|---|---|---|---|
| `POST` | `/api/estados` | Registrar un cambio de estado | RNF-04 |
| `GET` | `/api/estados/{pedidoId}` | Historial de estados por pedido | RNF-04 |
| `GET` | `/api/estados/ping` | Healthcheck | RNF-03 |

> El diseño intencional es minimalista: este servicio tiene exactamente los dos endpoints definidos en el documento de levantamiento más el healthcheck estándar del proyecto.

---

### 7.1 `POST /api/estados` — Registrar cambio de estado

Llamado por `pedido-service` cada vez que ejecuta `PATCH /api/pedidos/{id}/estado`.  
Persiste el registro de auditoría con estado anterior, estado nuevo, timestamp y usuario.

**Request body:**
```json
{
  "pedidoId": 1,
  "estadoAnterior": "PENDIENTE",
  "estadoNuevo": "EN_FABRICACION",
  "usuarioId": 3
}
```

> `usuarioId` es opcional mientras `auth-service` no esté implementado; puede enviarse `null`.

**Respuestas:**

| Status | Caso |
|---|---|
| `201 Created` | Cambio registrado. Retorna `ApiResponse<CambioEstadoResponseDTO>` |
| `400 Bad Request` | `pedidoId`, `estadoAnterior` o `estadoNuevo` faltantes o vacíos |

---

### 7.2 `GET /api/estados/{pedidoId}` — Historial de estados por pedido

Retorna todos los cambios de estado de un pedido, ordenados cronológicamente de más antiguo a más reciente.

**Respuesta exitosa (`200 OK`):**
```json
{
  "mensaje": "Historial obtenido correctamente",
  "data": [
    {
      "id": 1,
      "pedidoId": 1,
      "estadoAnterior": "PENDIENTE",
      "estadoNuevo": "EN_FABRICACION",
      "fechaCambio": "2025-05-18T09:00:00",
      "usuarioId": 3
    },
    {
      "id": 2,
      "pedidoId": 1,
      "estadoAnterior": "EN_FABRICACION",
      "estadoNuevo": "LISTO",
      "fechaCambio": "2025-05-18T14:30:00",
      "usuarioId": 3
    }
  ],
  "exitoso": true,
  "timestamp": "2025-05-18T15:00:00"
}
```

**Respuestas:**

| Status | Caso |
|---|---|
| `200 OK` | Historial retornado (lista vacía si el pedido no tiene cambios registrados) |

> No retorna `404` cuando no hay registros: una lista vacía con `200` es la respuesta correcta, ya que `estado-service` no tiene conocimiento de si el pedido existe o no (ese dato pertenece a `pedido-service`).

---

### 7.3 `GET /api/estados/ping` — Healthcheck

```json
{ "mensaje": "estado-service activo", "data": null, "exitoso": true, "timestamp": "..." }
```

---

## 8. DTOs

### `CambioEstadoRequestDTO`

```java
public class CambioEstadoRequestDTO {
    @NotNull   private Long pedidoId;
    @NotBlank  private String estadoAnterior;
    @NotBlank  private String estadoNuevo;
               private Long usuarioId;        // opcional
}
```

### `CambioEstadoResponseDTO`

```java
public class CambioEstadoResponseDTO {
    private Long id;
    private Long pedidoId;
    private String estadoAnterior;
    private String estadoNuevo;
    private LocalDateTime fechaCambio;
    private Long usuarioId;
}
```

---

## 9. Comportamiento de negocio

| Regla | Detalle |
|---|---|
| **`fechaCambio` automática** | El servicio asigna `LocalDateTime.now()` al persistir; el cliente no la envía |
| **Sin validación de estados** | `estado-service` acepta cualquier valor de `estadoAnterior`/`estadoNuevo` como `String`. La validación del flujo de estados es responsabilidad de `pedido-service` |
| **Sin validación de `pedidoId`** | No consulta a `pedido-service` para verificar existencia. Es un registro de auditoría puro: si `pedido-service` envió el evento, el pedido existe |
| **Historial vacío = 200** | `GET /api/estados/{pedidoId}` sin resultados retorna lista vacía con `200`, no `404` |
| **Orden cronológico** | El historial se ordena por `fechaCambio ASC` (más antiguo primero) |
| **Inmutable** | Los registros de historial **no se pueden modificar ni eliminar**. No hay `PUT`, `PATCH` ni `DELETE`. Garantiza integridad de auditoría (RNF-04) |

---

## 10. Integración con `pedido-service` (cliente Feign en el lado emisor)

El cliente Feign vive en `pedido-service`, no en `estado-service`:

```java
// En pedido-service
@FeignClient(name = "estado-service", url = "${estado.service.url}")
public interface EstadoFeignClient {

    @PostMapping("/api/estados")
    ApiResponse<CambioEstadoResponseDTO> registrarCambio(
        @RequestBody CambioEstadoRequestDTO dto
    );
}
```

**Propiedad en `pedido-service/application.properties`:**
```properties
estado.service.url=http://localhost:8085
```

**Flujo al actualizar estado de un pedido:**
1. `pedido-service` recibe `PATCH /api/pedidos/{id}/estado` con el nuevo estado.
2. Lee el estado actual del pedido (`estadoAnterior`).
3. Valida que la transición de estado sea permitida.
4. Persiste el nuevo estado en `db_pedidos`.
5. Llama `POST /api/estados` en `estado-service` con `{pedidoId, estadoAnterior, estadoNuevo, usuarioId}`.
6. El fallo de `estado-service` **no debe bloquear** la respuesta de `pedido-service` (tolerancia a fallos): usar `try/catch` en la llamada Feign o implementar un patrón de fire-and-forget.

> **Recomendación:** Llamar a `estado-service` de forma asíncrona o con tolerancia a fallos (`try/catch` simple en esta fase) para que una caída del servicio de auditoría no impida actualizar pedidos.

---

## 11. Configuración del módulo Maven

Agregar en el `pom.xml` raíz del monorepo:

```xml
<modules>
    <module>cliente-service</module>
    <module>pedido-service</module>
    <module>fabricacion-service</module>
    <module>producto-service</module>
    <module>despacho-service</module>
    <module>estado-service</module>   <!-- ← agregar -->
</modules>
```

**`estado-service/pom.xml` (fragmento):**

```xml
<parent>
    <groupId>com.example</groupId>
    <artifactId>api-pedidos</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</parent>

<artifactId>estado-service</artifactId>
<name>estado-service</name>

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
    <!-- Sin Feign: este servicio no realiza llamadas salientes -->
</dependencies>
```

---

## 12. Configuración de properties

**`application.properties`:**
```properties
spring.application.name=estado-service
server.port=8085
spring.profiles.active=h2
```

**`application-h2.properties`:**
```properties
spring.datasource.url=jdbc:h2:file:./data/estado_service;AUTO_SERVER=TRUE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

---

## 13. Aplicación principal

```java
// Sin @EnableFeignClients — este servicio no hace llamadas salientes
@SpringBootApplication
public class EstadoServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EstadoServiceApplication.class, args);
    }
}
```

---

## 14. Script de arranque

Agregar al `start-all.bat` existente:

```bat
start "estado-service" cmd /k "mvnw.cmd spring-boot:run -pl estado-service"
```

Y en terminal Linux/Mac:
```bash
./mvnw spring-boot:run -pl estado-service
```

---

## 15. Requerimientos cubiertos

| RF / RNF | Cobertura |
|---|---|
| RF-04 | Actualizar el estado de un pedido (historial generado por cada cambio) |
| RNF-04 | Registrar logs/historial de operaciones para trazabilidad y auditoría |
| RNF-01 | Códigos HTTP adecuados en todos los endpoints |
| RNF-02 | Bean Validation + mensajes descriptivos vía `@ControllerAdvice` |
| RNF-03 | Módulo independiente, desplegable de forma autónoma en puerto 8085 |
| RNF-05 | Consultas simples sobre H2 con JPA — latencia esperada < 50 ms |
| RNF-06 | Verbos HTTP estándar REST, rutas de recursos, respuesta JSON |
| RNF-07 | Estados como `String` permiten evolución sin rediseño estructural |

---

## 16. Checklist de implementación

- [ ] Crear módulo `estado-service/` en el monorepo
- [ ] Agregar `<module>estado-service</module>` en `pom.xml` raíz
- [ ] Implementar entidad `CambioEstado` con asignación automática de `fechaCambio`
- [ ] Implementar `CambioEstadoRepository` con método `findByPedidoIdOrderByFechaCambioAsc`
- [ ] Implementar `EstadoService` / `EstadoServiceImpl`
- [ ] Implementar `EstadoController` con los 3 endpoints
- [ ] Implementar `ApiResponse<T>` (copiar desde otro módulo)
- [ ] Agregar `@ControllerAdvice` para manejo de excepciones (400)
- [ ] Configurar `application.properties` y `application-h2.properties`
- [ ] Agregar `EstadoFeignClient` en `pedido-service` con llamada tolerante a fallos
- [ ] Agregar propiedad `estado.service.url` en `pedido-service/application.properties`
- [ ] Actualizar `start-all.bat` con el nuevo servicio
- [ ] Probar flujo completo: crear pedido → cambiar estado → verificar historial en `GET /api/estados/{pedidoId}`
