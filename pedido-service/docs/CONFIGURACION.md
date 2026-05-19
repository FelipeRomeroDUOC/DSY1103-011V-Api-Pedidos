# Configuración — pedido-service

## Perfiles de Spring

| Perfil | Archivo | Uso |
|---|---|---|
| `h2` (default) | `application-h2.properties` | Desarrollo local con H2 |
| *(base)* | `application.properties` | Configuración general |

## Propiedades Generales (`application.properties`)

```properties
spring.application.name=pedido-service
server.port=8081
spring.profiles.default=h2
spring.jpa.open-in-view=false

services.cliente-service.url=${CLIENTE_SERVICE_URL:http://127.0.0.1:8082}

feign.client.config.default.connectTimeout=2000
feign.client.config.default.readTimeout=5000
feign.client.config.default.loggerLevel=basic
```

| Propiedad | Valor | Descripción |
|---|---|---|
| `server.port` | `8081` | Puerto HTTP |
| `services.cliente-service.url` | `http://127.0.0.1:8082` | URL de `cliente-service` |
| `feign.*.connectTimeout` | `2000` | Timeout de conexión Feign (ms) |
| `feign.*.readTimeout` | `5000` | Timeout de lectura Feign (ms) |

## Perfil H2 (`application-h2.properties`)

```properties
spring.datasource.url=jdbc:h2:file:./data/pedido_service;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

### Consola H2

URL: `http://localhost:8081/h2-console`

| Parámetro | Valor |
|---|---|
| JDBC URL | `jdbc:h2:file:./data/pedido_service` |
| User Name | `sa` |
| Password | *(vacío)* |

## Variables de Entorno

| Variable | Default | Descripción |
|---|---|---|
| `CLIENTE_SERVICE_URL` | `http://127.0.0.1:8082` | URL del microservicio de clientes |

## Dependencias Maven Específicas

| Dependencia | Propósito |
|---|---|
| `spring-cloud-starter-openfeign` | Cliente HTTP declarativo |
| `feign-jackson` | Serialización/deserialización JSON para Feign |

Las dependencias comunes (Web, JPA, H2, Lombok, Validation, WebFlux) se heredan del POM padre.
