# Configuración — despacho-service

El archivo de configuración principal se encuentra en `src/main/resources/application.properties` (y perfiles específicos como `application-h2.properties`).

## application.properties

| Propiedad | Valor Base | Descripción |
|---|---|---|
| `spring.application.name` | `despacho-service` | Nombre registrado del módulo. |
| `server.port` | `8084` | Puerto fijo reservado en el monorepo. |
| `spring.profiles.active` | `h2` | Activa la configuración y archivo de propiedades específico para base de datos H2. |
| `pedido.service.url` | `http://localhost:8081` | Variable de entorno (o propiedad configurada local) utilizada dinámicamente por Feign Client para alcanzar a `pedido-service`. |
| `spring.cloud.openfeign.okhttp.enabled` | `true` | Habilita el cliente OkHttp en reemplazo del HttpURLConnection nativo por defecto en Feign. Permite soporte para métodos como PATCH si fuesen necesarios, o mejor control de conexiones. |

## application-h2.properties

Define las credenciales y ubicaciones de la capa de persistencia en disco.

| Propiedad | Valor Base | Descripción |
|---|---|---|
| `spring.datasource.url` | `jdbc:h2:file:./data/despacho_service;AUTO_SERVER=TRUE` | Guarda los datos persistiéndolos en la raíz del servicio en un archivo `./data/despacho_service.mv.db`. `AUTO_SERVER=TRUE` permite que una DB tool local pueda inspeccionarlo mientras el MS está corriendo. |
| `spring.datasource.driver-class-name` | `org.h2.Driver` | Driver JDBC para H2. |
| `spring.datasource.username` | `sa` | Usuario root por defecto. |
| `spring.datasource.password` | *(vacío)* | Contraseña por defecto vacía. |
| `spring.jpa.hibernate.ddl-auto` | `update` | Sincroniza el Entity con el Schema sin borrar los datos existentes. Ideal para entorno local de QA. |
| `spring.jpa.show-sql` | `true` | Imprime por consola los queries SQL subyacentes, vital para debugeo en local. |
| `spring.h2.console.enabled` | `true` | Habilita acceso web a la base de datos en `/h2-console`. |
