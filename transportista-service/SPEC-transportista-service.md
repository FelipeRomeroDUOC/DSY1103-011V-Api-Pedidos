# SPEC — `transportista-service`

> DSY1103 · Desarrollo FullStack 1 · Proyecto Semestral — Arquitectura de Microservicios  
> Módulo: `transportista-service` | Puerto: `8088` | BD: `db_transportistas`

---

## 1. Descripción general

`transportista-service` gestiona el catálogo de transportistas disponibles para el despacho a regiones.  
Es consultado por `despacho-service` para asignar un transportista válido y activo cuando el tipo de despacho es `REGION`.

---

## 2. Posición en la arquitectura

```
Cliente / Frontend
       │ HTTP REST
       ▼
API Gateway / Load Balancer
       │
       └──► despacho-service (8084) ──Feign──► transportista-service (8088) [asigna transportista regional]
```

**Comunicación inter-servicio:**

| Dirección | Protocolo | Propósito |
|---|---|---|
| `despacho-service` → `transportista-service` | HTTP GET (Feign) | Verificar existencia y obtener datos de un transportista antes de registrar un despacho regional |

> `transportista-service` es **pasivo**: no inicia llamadas hacia otros servicios.

---

## 3. Stack tecnológico

Las dependencias comunes (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `lombok`, `h2`, `webflux`) ya están declaradas en el **`pom.xml` padre** del monorepo y se heredan automáticamente. El `pom.xml` de este módulo solo necesita declarar su `artifactId` y puerto.

| Tecnología | Versión / detalle |
|---|---|
| Java | 21 |
| Spring Boot | 3.4.4 |
| Spring Web | REST controllers (heredado del padre) |
| Spring Data JPA + Hibernate | Persistencia (heredado del padre) |
| Lombok 1.18.46 | `@Getter`, `@Setter`, `@RequiredArgsConstructor` (heredado) |
| Bean Validation | `@Valid`, `@NotBlank`, `@NotNull` (heredado) |
| H2 Database (archivo) | Desarrollo local (heredado) |

> Este servicio **no requiere Feign** ni OkHttp porque no realiza llamadas salientes.

**Base de datos local (H2 en archivo):**
```
transportista-service/data/transportista_service.mv.db
```

---

## 4. Entidad JPA

### `Transportista`

```java
@Entity
@Table(name = "transportistas")
@Getter @Setter
public class Transportista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nombre;              // ej: "Starken", "Paket"

    @NotBlank
    @Column(unique = true)
    private String codigoInterno;       // ej: "STK", "PKT"

    private String contacto;            // teléfono o email de contacto

    private String regionesCobertura;   // ej: "I,II,III,RM" — almacenado como String delimitado

    private boolean activo = true;
}
```

> **Nota de diseño:** `regionesCobertura` se almacena como `String` con valores separados por coma para simplificar la implementación en esta fase. Puede evolucionar a una colección `@ElementCollection` sin rediseño estructural del resto del sistema (RNF-07).

---

## 5. Estructura de paquetes

```
transportista-service/
└── src/
    └── main/
        ├── java/cl/apipedidos/transportistaservice/
        │   ├── TransportistaServiceApplication.java
        │   ├── controller/
        │   │   └── TransportistaController.java
        │   ├── service/
        │   │   ├── TransportistaService.java          ← interfaz
        │   │   └── TransportistaServiceImpl.java
        │   ├── repository/
        │   │   └── TransportistaRepository.java
        │   ├── model/
        │   │   └── Transportista.java
        │   ├── dto/
        │   │   ├── TransportistaRequestDTO.java
        │   │   └── TransportistaResponseDTO.java
        │   └── response/
        │       └── ApiResponse.java                   ← wrapper estándar del proyecto
        └── resources/
            ├── application.properties
            └── application-h2.properties
```

> El `groupId` base del proyecto es `cl.apipedidos`, consistente con el `pom.xml` raíz.

---

## 6. Wrapper de respuesta estándar (`ApiResponse`)

Todos los endpoints devuelven el mismo envoltorio genérico del proyecto:

```json
{
  "mensaje": "Transportista registrado correctamente",
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

**Base URL:** `http://localhost:8088/api/transportistas`

| Método | Ruta | Descripción | RF |
|---|---|---|---|
| `POST` | `/api/transportistas` | Registrar un nuevo transportista | RF-15 |
| `GET` | `/api/transportistas` | Listar transportistas activos | RF-15 |
| `GET` | `/api/transportistas/{id}` | Obtener un transportista por ID | RF-15 |
| `PUT` | `/api/transportistas/{id}` | Actualizar datos del transportista | RF-15 |
| `GET` | `/api/transportistas/ping` | Healthcheck | RNF-03 |

---

### 7.1 `POST /api/transportistas` — Registrar transportista

**Request body:**
```json
{
  "nombre": "Starken",
  "codigoInterno": "STK",
  "contacto": "+56800123456",
  "regionesCobertura": "I,II,III,IV,V,VI,VII,VIII,IX,X,XI,XII,RM,XIV,XV,XVI"
}
```

**Respuestas:**

| Status | Caso |
|---|---|
| `201 Created` | Transportista registrado. Retorna `ApiResponse<TransportistaResponseDTO>` |
| `400 Bad Request` | Campo obligatorio faltante o inválido |
| `409 Conflict` | `codigoInterno` ya existe |

---

### 7.2 `GET /api/transportistas` — Listar transportistas activos

Retorna únicamente transportistas con `activo = true`.

**Respuestas:**

| Status | Caso |
|---|---|
| `200 OK` | Lista de transportistas activos (puede ser vacía) |

---

### 7.3 `GET /api/transportistas/{id}` — Obtener transportista por ID

Usado por `despacho-service` vía Feign para verificar existencia y obtener datos antes de crear un despacho regional.  
Retorna `404` si el transportista no existe **o** está inactivo.

**Respuestas:**

| Status | Caso |
|---|---|
| `200 OK` | Transportista encontrado y activo |
| `404 Not Found` | Transportista no existe o está inactivo |

---

### 7.4 `PUT /api/transportistas/{id}` — Actualizar transportista

Permite modificar nombre, contacto, regiones de cobertura y estado `activo`.

**Request body:**
```json
{
  "nombre": "Starken",
  "contacto": "+56800999000",
  "regionesCobertura": "I,II,III,RM",
  "activo": true
}
```

**Respuestas:**

| Status | Caso |
|---|---|
| `200 OK` | Transportista actualizado |
| `400 Bad Request` | Datos inválidos |
| `404 Not Found` | Transportista no existe |

---

### 7.5 `GET /api/transportistas/ping` — Healthcheck

```json
{ "mensaje": "transportista-service activo", "data": null, "exitoso": true, "timestamp": "..." }
```

---

## 8. DTOs

### `TransportistaRequestDTO`

```java
public class TransportistaRequestDTO {
    @NotBlank  private String nombre;
    @NotBlank  private String codigoInterno;
               private String contacto;
               private String regionesCobertura;
}
```

### `TransportistaUpdateDTO` (para `PUT`)

```java
public class TransportistaUpdateDTO {
    private String nombre;
    private String contacto;
    private String regionesCobertura;
    private Boolean activo;
}
```

### `TransportistaResponseDTO`

```java
public class TransportistaResponseDTO {
    private Long id;
    private String nombre;
    private String codigoInterno;
    private String contacto;
    private String regionesCobertura;
    private boolean activo;
}
```

---

## 9. Comportamiento de negocio

| Regla | Detalle |
|---|---|
| **Soft delete vía `activo`** | No existe `DELETE`. Para desactivar un transportista se usa `PUT` con `activo: false`. Esto preserva el historial de despachos asociados en `despacho-service` |
| **Listado filtrado** | `GET /api/transportistas` solo retorna registros con `activo = true` |
| **Verificación externa** | `GET /api/transportistas/{id}` retorna `404` si el transportista no existe **o** tiene `activo = false`, para que `despacho-service` rechace asignaciones inválidas |
| **`codigoInterno` único** | Se valida a nivel de BD con `@Column(unique = true)`. Un `codigoInterno` duplicado retorna `409 Conflict` |
| **Sin DELETE expuesto** | La desactivación es siempre lógica (soft). No se expone endpoint `DELETE` |

---

## 10. Integración con `despacho-service` (cliente Feign en el lado emisor)

El cliente Feign vive en `despacho-service`, no en `transportista-service`:

```java
// En despacho-service
@FeignClient(name = "transportista-service", url = "${transportista.service.url}")
public interface TransportistaFeignClient {

    @GetMapping("/api/transportistas/{id}")
    ApiResponse<TransportistaResponseDTO> obtenerTransportista(@PathVariable Long id);
}
```

**Propiedad a agregar en `despacho-service/application.properties`:**
```properties
transportista.service.url=http://localhost:8088
```

**Flujo al registrar un despacho de tipo `REGION`:**
1. `despacho-service` recibe `POST /api/despachos` con `tipoDespacho: REGION` y un `transportistaId`.
2. Llama `GET /api/transportistas/{transportistaId}` en `transportista-service` vía Feign.
3. Si retorna `404` → rechaza el despacho con `400` (transportista inválido o inactivo).
4. Si retorna `200` → procede a registrar el despacho con el nombre del transportista.

---

## 11. `pom.xml` del módulo

Al heredar del padre todas las dependencias comunes, el `pom.xml` de este módulo es mínimo:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>cl.apipedidos</groupId>
        <artifactId>api-pedidos-parent</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>transportista-service</artifactId>
    <name>transportista-service</name>
    <description>Microservicio de gestión de transportistas</description>

    <!-- Sin dependencias adicionales: web, jpa, validation, lombok, h2 vienen del padre -->

</project>
```

---

## 12. Agregar módulo al `pom.xml` raíz

```xml
<modules>
    <module>cliente-service</module>
    <module>pedido-service</module>
    <module>fabricacion-service</module>
    <module>producto-service</module>
    <module>despacho-service</module>
    <module>estado-service</module>
    <module>metrica-service</module>
    <module>transportista-service</module>   <!-- ← agregar -->
</modules>
```

---

## 13. Configuración de properties

**`application.properties`:**
```properties
spring.application.name=transportista-service
server.port=8088
spring.profiles.active=h2
```

**`application-h2.properties`:**
```properties
spring.datasource.url=jdbc:h2:file:./data/transportista_service;AUTO_SERVER=TRUE
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
// Sin @EnableFeignClients — este servicio no realiza llamadas salientes
@SpringBootApplication
public class TransportistaServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TransportistaServiceApplication.class, args);
    }
}
```

---

## 15. Script de arranque

Agregar al `start-all.bat` existente:

```bat
start "transportista-service" cmd /k "mvnw.cmd spring-boot:run -pl transportista-service"
```

Y en terminal Linux/Mac:
```bash
./mvnw spring-boot:run -pl transportista-service
```

---

## 16. Datos de ejemplo (data.sql opcional)

Para tener transportistas disponibles desde el inicio sin necesidad de POST manual:

```sql
INSERT INTO transportistas (nombre, codigo_interno, contacto, regiones_cobertura, activo)
VALUES ('Starken', 'STK', '+56800123456', 'I,II,III,IV,V,VI,VII,VIII,IX,X,XI,XII,RM,XIV,XV,XVI', true);

INSERT INTO transportistas (nombre, codigo_interno, contacto, regiones_cobertura, activo)
VALUES ('Paket', 'PKT', '+56800654321', 'RM,V,VI,VII,VIII', true);
```

Colocar en `src/main/resources/data.sql` y agregar en `application-h2.properties`:
```properties
spring.sql.init.mode=always
```

---

## 17. Requerimientos cubiertos

| RF / RNF | Cobertura |
|---|---|
| RF-11 | Clasificar pedidos según tipo de despacho — provee el catálogo de transportistas para REGION |
| RF-15 | Registrar información de despacho — base para asignar transportista en `despacho-service` |
| RNF-01 | Códigos HTTP adecuados en todos los endpoints |
| RNF-02 | Bean Validation + mensajes descriptivos vía `@ControllerAdvice` |
| RNF-03 | Módulo independiente, desplegable de forma autónoma en puerto 8088 |
| RNF-05 | Consultas simples sobre H2 con JPA — latencia esperada < 50 ms |
| RNF-06 | Verbos HTTP estándar REST, rutas de recursos, respuesta JSON |
| RNF-07 | `regionesCobertura` como String permite evolución sin rediseño estructural |

---

## 18. Checklist de implementación

- [ ] Crear módulo `transportista-service/` en el monorepo
- [ ] Agregar `<module>transportista-service</module>` en `pom.xml` raíz
- [ ] Crear `pom.xml` mínimo heredando de `api-pedidos-parent`
- [ ] Implementar entidad `Transportista` con `@Column(unique = true)` en `codigoInterno`
- [ ] Implementar `TransportistaRepository` (JPA)
- [ ] Implementar `TransportistaService` / `TransportistaServiceImpl`
- [ ] Implementar `TransportistaController` con los 5 endpoints
- [ ] Implementar `ApiResponse<T>` (copiar desde otro módulo)
- [ ] Agregar `@ControllerAdvice` para manejo de excepciones (400, 404, 409)
- [ ] Configurar `application.properties` y `application-h2.properties`
- [ ] Agregar `data.sql` con Starken y Paket para desarrollo
- [ ] Agregar `TransportistaFeignClient` en `despacho-service`
- [ ] Agregar propiedad `transportista.service.url` en `despacho-service/application.properties`
- [ ] Actualizar `start-all.bat` con el nuevo servicio
- [ ] Probar flujo completo: registrar transportista → crear despacho REGION usando su ID → intentar con ID inactivo (404)
