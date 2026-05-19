# SPEC — `log-service`

> DSY1103 · Desarrollo FullStack 1 · Proyecto Semestral — Arquitectura de Microservicios  
> Servicio: **log-service** · Puerto: `8089` · BD: `db_logs`

---

## 1. Propósito

`log-service` registra de forma centralizada los eventos operativos generados por todos los demás microservicios del sistema. Cumple el requerimiento no funcional **RNF-04** ("El sistema debe registrar logs de operaciones para trazabilidad y auditoría") y actúa como receptor pasivo: cualquier servicio puede enviarle un log de forma asíncrona sin depender del resultado.

---

## 2. Posición en la arquitectura

```
pedido-service  ──POST /api/logs──►
cliente-service ──POST /api/logs──►  log-service (8089)  ──► db_logs (H2)
fabricacion-service ──POST /api/logs──►
(resto de servicios)
```

- **No llama a ningún otro servicio** (sin clientes Feign salientes).  
- Todos los servicios le envían logs usando `RestTemplate` o Feign de forma *fire-and-forget* (no bloquean su flujo principal ante un fallo del log-service).

---

## 3. Stack tecnológico

| Elemento | Valor |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.4.x |
| Persistencia | Spring Data JPA + Hibernate |
| Base de datos (dev) | H2 en archivo (`log-service/data/log_service.mv.db`) |
| Lombok | `@Getter`, `@Setter`, `@RequiredArgsConstructor`, `@Builder` |
| Validación | Bean Validation (`jakarta.validation`) |
| Módulo Maven | `log-service` (hijo del POM padre raíz) |

---

## 4. Estructura de carpetas

```
log-service/
├── pom.xml
└── src/
    └── main/
        ├── java/com/duoc/log_service/
        │   ├── LogServiceApplication.java
        │   ├── controller/
        │   │   └── LogController.java
        │   ├── dto/
        │   │   ├── LogRequestDTO.java
        │   │   └── ApiResponse.java          ← mismo wrapper genérico del resto del proyecto
        │   ├── entity/
        │   │   └── LogEntrada.java
        │   ├── repository/
        │   │   └── LogRepository.java
        │   └── service/
        │       ├── LogService.java            ← interfaz
        │       └── LogServiceImpl.java
        └── resources/
            ├── application.properties
            └── application-h2.properties
```

---

## 5. Entidad JPA — `LogEntrada`

```java
@Entity
@Table(name = "logs")
@Getter @Setter
public class LogEntrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String servicio;       // "pedido-service", "cliente-service", etc.

    @NotBlank
    private String operacion;      // "CREAR_PEDIDO", "ACTUALIZAR_ESTADO", etc.

    private String usuarioId;      // ID del usuario que disparó la acción (nullable)

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @NotBlank
    private String resultado;      // "EXITO" | "ERROR"

    @Column(length = 1000)
    private String detalle;        // Descripción libre, mensaje de error, etc.
}
```

**Notas:**
- `timestamp` se asigna automáticamente en la capa de servicio (`LocalDateTime.now()`), no viene del cliente.
- `usuarioId` es opcional; puede ser `null` si el llamante es otro servicio sin usuario autenticado.

---

## 6. DTO de entrada — `LogRequestDTO`

```java
public class LogRequestDTO {
    @NotBlank String servicio;
    @NotBlank String operacion;
    String usuarioId;        // opcional
    @NotBlank String resultado;  // "EXITO" | "ERROR"
    String detalle;          // opcional
}
```

---

## 7. Wrapper de respuesta — `ApiResponse<T>`

Igual al resto del proyecto:

```java
{
  "mensaje": "...",
  "data": { ... },
  "exitoso": true,
  "timestamp": "2025-05-19T12:00:00"
}
```

---

## 8. Endpoints REST

### `POST /api/logs`
Registra una nueva entrada de log.

| Campo | Valor |
|---|---|
| Método | `POST` |
| Ruta | `/api/logs` |
| Body | `LogRequestDTO` (JSON) |
| Respuesta OK | `201 Created` + `ApiResponse<LogEntrada>` |
| Respuesta error validación | `400 Bad Request` + `ApiResponse` con descripción |

**Ejemplo de request:**
```json
{
  "servicio": "pedido-service",
  "operacion": "CREAR_PEDIDO",
  "usuarioId": "42",
  "resultado": "EXITO",
  "detalle": "Pedido PED-007 creado para clienteId=3"
}
```

**Ejemplo de response `201`:**
```json
{
  "mensaje": "Log registrado correctamente",
  "data": {
    "id": 1,
    "servicio": "pedido-service",
    "operacion": "CREAR_PEDIDO",
    "usuarioId": "42",
    "timestamp": "2025-05-19T14:23:00",
    "resultado": "EXITO",
    "detalle": "Pedido PED-007 creado para clienteId=3"
  },
  "exitoso": true,
  "timestamp": "2025-05-19T14:23:00"
}
```

---

### `GET /api/logs`
Lista logs con filtros opcionales.

| Campo | Valor |
|---|---|
| Método | `GET` |
| Ruta | `/api/logs` |
| Query params | `servicio` (opcional), `desde` (opcional, `yyyy-MM-ddTHH:mm:ss`) |
| Respuesta OK | `200 OK` + `ApiResponse<List<LogEntrada>>` |
| Respuesta error param | `400 Bad Request` + `ApiResponse` con descripción |

**Ejemplos:**
```
GET /api/logs
GET /api/logs?servicio=pedido-service
GET /api/logs?desde=2025-05-01T00:00:00
GET /api/logs?servicio=fabricacion-service&desde=2025-05-01T00:00:00
```

Sin resultados → `200 OK` con `data: []`.

---

### `GET /api/logs/ping`
Healthcheck del servicio.

| Campo | Valor |
|---|---|
| Método | `GET` |
| Ruta | `/api/logs/ping` |
| Respuesta | `200 OK` + `"log-service OK"` |

---

## 9. Lógica del servicio (`LogServiceImpl`)

```
registrarLog(LogRequestDTO dto):
  1. Mapear DTO → entidad LogEntrada
  2. Asignar timestamp = LocalDateTime.now()
  3. Guardar con logRepository.save(entrada)
  4. Retornar ApiResponse exitoso con entidad guardada

consultarLogs(String servicio, LocalDateTime desde):
  1. Si ambos parámetros son null → logRepository.findAll()
  2. Si solo servicio → findByServicio(servicio)
  3. Si solo desde    → findByTimestampAfter(desde)
  4. Si ambos        → findByServicioAndTimestampAfter(servicio, desde)
  5. Retornar ApiResponse con lista (puede ser vacía)
```

---

## 10. Repository (`LogRepository`)

```java
public interface LogRepository extends JpaRepository<LogEntrada, Long> {
    List<LogEntrada> findByServicio(String servicio);
    List<LogEntrada> findByTimestampAfter(LocalDateTime desde);
    List<LogEntrada> findByServicioAndTimestampAfter(String servicio, LocalDateTime desde);
}
```

---

## 11. Configuración

### `application.properties`
```properties
spring.application.name=log-service
server.port=8089
spring.profiles.active=h2
```

### `application-h2.properties`
```properties
spring.datasource.url=jdbc:h2:file:./data/log_service;AUTO_SERVER=TRUE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

---

## 12. `pom.xml` — fragmento relevante

Módulo hijo del POM padre. Hereda versiones de Spring Boot, Lombok y Java. Solo necesita añadir:

```xml
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
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
</dependencies>
```

También registrar el módulo en el `pom.xml` raíz:
```xml
<modules>
    <module>cliente-service</module>
    <module>pedido-service</module>
    <module>fabricacion-service</module>
    <module>log-service</module>   <!-- ← añadir -->
</modules>
```

---

## 13. Cómo levantar el servicio

Desde la raíz del proyecto:

```bash
./mvnw spring-boot:run -pl log-service
```

Consola H2 disponible en: `http://localhost:8089/h2-console`

Agregar al `start-all.bat`:
```bat
start "log-service" cmd /k "mvnw spring-boot:run -pl log-service"
```

---

## 14. Checklist de implementación

- [ ] Crear carpeta `log-service/` con estructura Maven estándar
- [ ] Registrar módulo en `pom.xml` raíz
- [ ] Crear entidad `LogEntrada` con anotaciones JPA y Lombok
- [ ] Crear `LogRequestDTO` con Bean Validation
- [ ] Reutilizar clase `ApiResponse<T>` del proyecto (o replicarla en el paquete local)
- [ ] Implementar `LogRepository` con métodos de filtro
- [ ] Implementar `LogService` + `LogServiceImpl`
- [ ] Implementar `LogController` con los 3 endpoints
- [ ] Configurar `application.properties` y `application-h2.properties`
- [ ] Verificar `POST /api/logs` retorna `201` con body correcto
- [ ] Verificar `GET /api/logs?servicio=X` filtra correctamente
- [ ] Verificar `GET /api/logs/ping` responde `200`
- [ ] Agregar llamada a `log-service` en al menos un endpoint de `pedido-service` o `fabricacion-service`
