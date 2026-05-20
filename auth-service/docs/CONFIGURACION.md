# Configuración - Auth Service

## Propiedades (application.properties)

El servicio `auth-service` comparte la configuración base de la aplicación (Base de datos H2 en modo archivo) y añade configuraciones críticas para la generación y firma de los tokens JWT.

### Ejemplo
```properties
spring.application.name=auth-service
server.port=8090

# Base de datos
spring.datasource.url=jdbc:h2:file:./data/auth_service
spring.jpa.hibernate.ddl-auto=update

# Seguridad JWT
jwt.secret=dHVjbGF2ZXNlY3JldGFkZWJlc2VybXV5bGFyZ2FwYXJhcXVlc2VhcnNlZ3VyYQ==
jwt.expiration-ms=86400000
```

## Secret Key (jwt.secret)

El secreto `jwt.secret` **debe estar codificado en Base64** y poseer el largo suficiente para el algoritmo de cifrado `HMAC-SHA256` (al menos 256 bits / 32 bytes).

Este mismo secreto se ha inyectado de forma unificada en el archivo `.properties` de todos los microservicios del ecosistema, garantizando que un token emitido en el puerto 8090 pueda ser validado matemáticamente en el puerto 8081.

## Inicializador de Datos (`DataInitializer`)
Este proyecto carga automáticamente 4 usuarios de prueba si la base de datos se encuentra vacía, con contraseñas encriptadas nativamente a través de BCrypt.

- `admin@empresa.com` (pass123) - ADMIN
- `ana.garcia@empresa.com` (user123) - ENCARGADO_PEDIDOS
- `carlos.lopez@empresa.com` (user123) - ENCARGADO_DESPACHO
- `maria.fernandez@empresa.com` (user123) - COMERCIAL
