# Configuración — fabricacion-service

## Perfiles de Spring

| Perfil | Archivo | Uso |
|---|---|---|
| `h2` (default) | `application-h2.properties` | Desarrollo local con H2 embebida |
| *(base)* | `application.properties` | Configuración general compartida |

El perfil por defecto es `h2`, definido en `application.properties`:
```properties
spring.profiles.default=h2
```

---

## Propiedades Generales (`application.properties`)

```properties
# Identificación del servicio
spring.application.name=fabricacion-service
server.port=8086

# Perfil por defecto
spring.profiles.default=h2

# JPA
spring.jpa.open-in-view=false

# URL del pedido-service (override con variable de entorno)
services.pedido-service.url=${PEDIDO_SERVICE_URL:http://127.0.0.1:8081}

# Feign - Timeouts y logging
feign.client.config.default.connectTimeout=2000
feign.client.config.default.readTimeout=5000
feign.client.config.default.loggerLevel=basic
```

### Detalle de propiedades

| Propiedad | Valor | Descripción |
|---|---|---|
| `server.port` | `8086` | Puerto HTTP del servicio |
| `spring.jpa.open-in-view` | `false` | Desactiva OSIV para evitar queries fuera de transacción |
| `services.pedido-service.url` | `http://127.0.0.1:8081` | URL de `pedido-service` |
| `feign.*.connectTimeout` | `2000` | Timeout de conexión Feign (ms) |
| `feign.*.readTimeout` | `5000` | Timeout de lectura Feign (ms) |
| `feign.*.loggerLevel` | `basic` | Nivel de log: `NONE`, `BASIC`, `HEADERS`, `FULL` |

---

## Perfil H2 (`application-h2.properties`)

```properties
# DataSource H2 en archivo
spring.datasource.url=jdbc:h2:file:./data/fabricacion_service;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# H2 Console (accesible en /h2-console)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Feign OkHttp (necesario para soporte de PATCH)
spring.cloud.openfeign.okhttp.enabled=true
```

### Acceso a la Consola H2

URL: `http://localhost:8086/h2-console`

| Parámetro | Valor |
|---|---|
| JDBC URL | `jdbc:h2:file:./data/fabricacion_service` |
| User Name | `sa` |
| Password | *(vacío)* |

---

## Variables de Entorno

| Variable | Default | Descripción |
|---|---|---|
| `PEDIDO_SERVICE_URL` | `http://127.0.0.1:8081` | URL del microservicio de pedidos |

Ejemplo de uso:
```bash
PEDIDO_SERVICE_URL=http://pedido-service:8081 ./mvnw spring-boot:run -pl fabricacion-service
```

---

## Dependencias Maven

Dependencias específicas del módulo (heredadas del POM padre):

| Dependencia | GroupId | Propósito |
|---|---|---|
| `spring-cloud-starter-openfeign` | `org.springframework.cloud` | Cliente HTTP declarativo |
| `feign-jackson` | `io.github.openfeign` | Serialización JSON |
| `feign-okhttp` | `io.github.openfeign` | Transporte OkHttp (soporte PATCH) |

Las dependencias comunes (Spring Boot Starter Web, JPA, H2, Lombok, Validation) se heredan del POM padre `api-pedidos-parent`.

---

## Notas Importantes

- **`spring.jpa.hibernate.ddl-auto=update`**: Hibernate sincroniza el schema automáticamente. En producción, cambiar a `validate` y usar migraciones (Flyway/Liquibase).
- **`AUTO_SERVER=TRUE`**: Permite que múltiples procesos accedan a la misma base H2 simultáneamente.
- **`DB_CLOSE_DELAY=-1`**: Mantiene la base de datos abierta mientras el proceso esté activo.
- **OkHttp obligatorio**: Sin `spring.cloud.openfeign.okhttp.enabled=true`, las llamadas `PATCH` a `pedido-service` fallarán con `ProtocolException`.
