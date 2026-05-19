# Configuración — producto-service

## Archivos de Configuración

| Archivo | Propósito |
|---|---|
| `application.properties` | Configuración general (nombre, puerto, perfil) |
| `application-h2.properties` | Configuración H2 para desarrollo local |

---

## `application.properties`

```properties
spring.application.name=producto-service
server.port=8083
spring.profiles.default=h2
spring.jpa.open-in-view=false
```

| Propiedad | Valor | Descripción |
|---|---|---|
| `spring.application.name` | `producto-service` | Nombre del servicio |
| `server.port` | `8083` | Puerto HTTP |
| `spring.profiles.default` | `h2` | Perfil por defecto (dev local) |
| `spring.jpa.open-in-view` | `false` | Desactiva OSIV (buena práctica) |

---

## `application-h2.properties`

```properties
spring.datasource.url=jdbc:h2:file:./data/producto_service;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

| Propiedad | Valor | Descripción |
|---|---|---|
| `spring.datasource.url` | `jdbc:h2:file:./data/producto_service` | BD H2 en archivo local |
| `spring.jpa.hibernate.ddl-auto` | `update` | Auto-creación/actualización de tablas |
| `spring.jpa.show-sql` | `true` | Log de queries SQL |
| `spring.h2.console.enabled` | `true` | Consola web H2 habilitada |
| `spring.h2.console.path` | `/h2-console` | Ruta de la consola web |

---

## Consola H2

Accesible en: `http://localhost:8083/h2-console`

| Campo | Valor |
|---|---|
| JDBC URL | `jdbc:h2:file:./data/producto_service` |
| User Name | `sa` |
| Password | *(vacío)* |

---

## Ejecución

```bash
# Desde la raíz del monorepo
./mvnw spring-boot:run -pl producto-service

# O compilar todo el monorepo
./mvnw clean compile
```
