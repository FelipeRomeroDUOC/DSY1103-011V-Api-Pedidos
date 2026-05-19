# Configuración — log-service

## Perfiles de Spring

| Perfil | Archivo | Uso |
|---|---|---|
| `h2` | `application-h2.properties` | Desarrollo local con base file-based |
| base | `application.properties` | Configuracion general |

## Propiedades Generales (`application.properties`)

```properties
spring.application.name=log-service
server.port=8089
spring.profiles.active=h2
```

## Perfil H2 (`application-h2.properties`)

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

### Consola H2

URL: `http://localhost:8089/h2-console`

| Parametro | Valor |
|---|---|
| JDBC URL | `jdbc:h2:file:./data/log_service` |
| User Name | `sa` |
| Password | *(vacio)* |

## Base de Datos Local

La persistencia se almacena en archivo dentro de `log-service/data/`.

Si el modelo cambia de manera incompatible con `ddl-auto=update`, puede ser necesario eliminar la carpeta `data/` para reconstruir el esquema desde cero.
