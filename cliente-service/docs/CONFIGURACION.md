# Configuración — cliente-service

## Perfiles de Spring

| Perfil | Archivo | Uso |
|---|---|---|
| `h2` (default) | `application-h2.properties` | Desarrollo local con H2 |
| *(base)* | `application.properties` | Configuración general |

## Propiedades Generales (`application.properties`)

```properties
spring.application.name=cliente-service
server.port=8082
spring.profiles.default=h2
spring.jpa.open-in-view=false
```

| Propiedad | Valor | Descripción |
|---|---|---|
| `server.port` | `8082` | Puerto HTTP |
| `spring.jpa.open-in-view` | `false` | Desactiva OSIV |

> Este servicio **no tiene configuración Feign** ni dependencias inter-servicio.

## Perfil H2 (`application-h2.properties`)

```properties
spring.datasource.url=jdbc:h2:file:./data/cliente_service;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1
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

URL: `http://localhost:8082/h2-console`

| Parámetro | Valor |
|---|---|
| JDBC URL | `jdbc:h2:file:./data/cliente_service` |
| User Name | `sa` |
| Password | *(vacío)* |

## Dependencias Maven Específicas

| Dependencia | Versión | Propósito |
|---|---|---|
| `mysql-connector-j` | 8.4.0 | Driver MySQL (producción) |
| `postgresql` | 42.7.4 | Driver PostgreSQL (producción) |

Las dependencias comunes (Web, JPA, H2, Lombok, Validation, WebFlux) se heredan del POM padre.

## Auto-Carga de Datos

Al iniciar el servicio con la base de datos vacía, `UbicacionDataLoader` importa automáticamente las divisiones territoriales de Chile desde:

```
src/main/resources/data/chile-divisiones-territoriales.json
```

Este proceso solo se ejecuta **una vez** (cuando `regionRepository.count() == 0`).

## Perfiles de Producción

Para usar MySQL o PostgreSQL, crear un perfil `application-mysql.properties` o `application-postgres.properties` con la configuración correspondiente y activarlo con:

```bash
SPRING_PROFILES_ACTIVE=mysql ./mvnw spring-boot:run -pl cliente-service
```

Los drivers ya están incluidos en el `pom.xml` del módulo.
