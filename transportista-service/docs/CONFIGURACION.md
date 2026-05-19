# Configuración: Transportista Service

La configuración principal del microservicio está administrada mediante las propiedades de Spring Boot. 

## Variables y Puertos

- **Puerto del Servidor:** `8088`
- **Nombre de la Aplicación:** `transportista-service`
- **Perfil Activo:** `h2`

## Base de Datos (H2)

El servicio utiliza una base de datos H2 basada en archivos para mantener persistencia entre reinicios del servidor.

### application-h2.properties

```properties
spring.datasource.url=jdbc:h2:file:./data/transportista_service;AUTO_SERVER=TRUE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=SA
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# Estrategia de creación de esquema
spring.jpa.hibernate.ddl-auto=update

# Habilitar consola H2 en /h2-console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Inicialización de Datos
spring.sql.init.mode=always
spring.jpa.defer-datasource-initialization=true
```

## Configuración de Directorio de Datos

El archivo de la base de datos se crea de manera automática dentro de la carpeta local `./data` del microservicio con el nombre `transportista_service.mv.db`. Esta carpeta se encuentra excluida del control de versiones (`.gitignore`).
