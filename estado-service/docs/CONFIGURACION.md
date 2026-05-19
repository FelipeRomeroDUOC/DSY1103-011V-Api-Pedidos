# Configuración — estado-service

El microservicio utiliza el puerto por defecto `8085` y su propia base H2 aislada para adherir a la regla de *Microservices Database-Per-Service*.

## application.properties

| Propiedad | Valor Base | Rol |
|---|---|---|
| `spring.application.name` | `estado-service` | Nombre de la aplicación (útil para Eureka a futuro). |
| `server.port` | `8085` | Asignación estática según arquitectura del monorepo. |
| `spring.profiles.active` | `h2` | Llama al archivo `-h2` para montar la BD persistente. |

## application-h2.properties

| Propiedad | Valor | Rol |
|---|---|---|
| `spring.datasource.url` | `jdbc:h2:file:./data/estado_service;AUTO_SERVER=TRUE` | Genera un archivo `.mv.db` real de auditoría en la carpeta `/data` interna del módulo. |
| `spring.jpa.hibernate.ddl-auto` | `update` | Creación y actualización automática del DDL. |
| `spring.h2.console.enabled` | `true` | Expone un cliente web de base de datos en `http://localhost:8085/h2-console`. |
| `spring.jpa.show-sql` | `true` | Para debug e inspección en consola de las escrituras del logger interno. |
