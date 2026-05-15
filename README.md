# Api Pedidos

Este proyecto es una API REST desarrollada en Java con Spring Boot. El foco actual del repositorio es la gestión de clientes, junto con el modelo real de ubicación de Chile compuesto por regiones, provincias y comunas, todo bajo el paquete raíz `cl.apipedidos`.

## 🚀 Tecnologías utilizadas

- Java 25.
- Spring Boot.
- Spring Web.
- Spring Data JPA.
- Hibernate.
- Lombok (actualizado para compatibilidad con JDK 25).
- Bean Validation.
- H2 Database para desarrollo local.
- MySQL y PostgreSQL como opciones de persistencia alternativa.

## 🧱 Estructura del proyecto (diagrama)

```
Api_Pedidos/
├─ src/
│  ├─ main/
│  │  ├─ java/
│  │  │  └─ cl/apipedidos/
│  │  │     ├─ ApiPedidosApplication.java
│  │  │     ├─ cliente/        # cliente-service (entidades, repos, controller, service, gui)
│  │  │     ├─ pedido/         # pedido-service (entidades, repos, controller, service)
+│  │  │     └─ ubicacion/      # regiones/provincias/comunas
│  │  └─ resources/
│  │     ├─ application.properties
│  │     ├─ application-h2.properties
│  │     └─ data/
│  └─ test/
└─ docs/
```

## 🛠️ Requisitos previos

- Java JDK 25.
- Maven (versión actualizada para compatibilidad con JDK 25).
- Un IDE como VS Code o IntelliJ IDEA.

## ▶️ Cómo ejecutar el proyecto

1. Abrir una terminal en la raíz del proyecto.
2. Ejecutar:

```bash
./mvnw spring-boot:run
```

Si solo quieres validar compilación:

```bash
./mvnw -q -DskipTests compile
```

## ⚙️ Configuración de la base de datos

El archivo `src/main/resources/application.properties` solo define el nombre de la aplicación. La configuración de persistencia se encuentra separada por perfiles:

- `src/main/resources/application-h2.properties`
- `src/main/resources/application-mysql.properties`
- `src/main/resources/application-supabase.properties`
- `src/main/resources/data/chile-divisiones-territoriales.json`

### H2 local

La configuración de H2 usa una base en archivo local y habilita la consola web. Además, ejecuta una migración SQL idempotente para el modelo con provincias:

```properties
spring.datasource.url=jdbc:h2:file:./data/api_pedidos;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:ubicacion-migration-h2.sql
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

Por defecto, la aplicación arranca con el perfil `h2` si no se define otro perfil activo.

## 🌐 Endpoints disponibles

| Método | Endpoint | Descripción |
| --- | --- | --- |
| `POST` | `/api/clientes` | Crear un cliente. |
| `GET` | `/api/clientes` | Listar clientes o filtrar por comuna. |
| `GET` | `/api/clientes/{identificador}` | Buscar por ID numérico o por nombre. |
| `PUT` | `/api/clientes/{id}` | Actualizar un cliente existente. |
| `DELETE` | `/api/clientes/{id}` | Eliminar un cliente. |
| `GET` | `/api/regiones` | Listar regiones. |
| `GET` | `/api/regiones/{idRegion}/provincias` | Listar provincias por región. |
| `GET` | `/api/regiones/{idRegion}/comunas` | Listar comunas por región. |
| `POST` | `/api/pedidos` | Crear un pedido validando cliente y productos. |
| `GET` | `/api/pedidos` | Listar pedidos (filtros: estado, tipo, clienteId). |
| `GET` | `/api/pedidos/{id}` | Obtener pedido por ID. |
| `GET` | `/api/pedidos/numero/{numeroPedido}` | Obtener pedido por número único. |
| `PATCH` | `/api/pedidos/{id}/estado` | Actualizar estado de un pedido. |
| `DELETE` | `/api/pedidos/{id}` | Eliminar un pedido (según reglas de negocio). |
| `POST` | `/api/pedidos/{id}/items` | Agregar un item a un pedido existente. |

## 🧪 Comportamiento funcional

- El POST y el PUT validan campos con Bean Validation.
- El POST y el PUT exigen nombre, RUT, DV, dirección, email, teléfono y comuna.
- No se permiten nombres duplicados.
- No se permiten RUT duplicados.
- La comuna se valida contra la base real de comunas de Chile y permite coincidencia por nombre ignorando mayúsculas/minúsculas y acentos.
- El ID del cliente se asigna de forma secuencial tomando el mayor ID existente y sumando uno.
- Si una búsqueda falla, la API responde con `404 Cliente no encontrado`.
- Si existe conflicto por unicidad, la API responde con `409`.
- Al iniciar la app con una base vacía, se cargan automáticamente las regiones, provincias y comunas reales de Chile.
- Al iniciar la app con una tabla vacía, se cargan clientes de ejemplo automáticamente.
- Al iniciar la app, se eliminan filas de clientes incompletas y se reordenan los IDs para que queden consecutivos desde 1.
- Los datos ingresados desde la interfaz se conservan mientras la aplicación está en ejecución.

## 🧩 Carga de datos de ejemplo

La clase `cl.apipedidos.ubicacion.config.UbicacionDataLoader` inserta las 16 regiones, las provincias y las comunas reales de Chile desde el recurso local `data/chile-divisiones-territoriales.json` si las tablas están vacías. La clase `cl.apipedidos.cliente.config.ClienteDataLoader` inserta registros de prueba solo si no hay clientes almacenados.

## 🖥️ Interfaz visual

El archivo `cl.apipedidos.cliente.gui.RegistroUsuarios` es un formulario Swing con campos para registrar usuarios campo a campo mediante solicitudes `POST`. Carga regiones y comunas desde la API (y puede consultar provincias por región), se abre automáticamente al terminar el arranque de Spring Boot y ya no depende de datos fijos para la ubicación.

## 🧭 Ejemplo de uso en Postman

### POST `/api/clientes`

```json
{
   "nombreCl": "Ana Pérez",
   "rutCl": 12345678,
   "divCl": "9",
   "direccionCl": "Av. Providencia 123",
   "emailCl": "ana@ejemplo.cl",
   "telefonoCl": "+56912345678",
   "comuna": "Providencia"
}
```

### PUT `/api/clientes/1`

```json
{
   "nombreCl": "Ana Pérez Actualizada",
   "rutCl": 12345678,
   "divCl": "9",
   "direccionCl": "Av. Providencia 456",
   "emailCl": "ana.nueva@ejemplo.cl",
   "telefonoCl": "+56987654321",
   "comuna": "Las Condes"
}
```

La respuesta de la API incluye la comuna, la provincia y la región asociadas al cliente.

## 📚 Documentación

- [entidad_cliente.md](docs/entidad_cliente.md): descripción del módulo de clientes, ubicación, API REST y Swing.
- [http_client.md](docs/http_client.md): diseño del cliente HTTP externo y el flujo de consumo.
- [registro_usuarios.md](docs/registro_usuarios.md): detalle del formulario Swing `RegistroUsuarios`.

## 📌 Nota de alcance

El proyecto ahora incluye dos módulos principales:

- **Cliente-service**: gestión completa de clientes, GUI Swing y carga de ubicaciones.
- **Pedido-service**: microservicio para gestión de pedidos (creación, consulta, actualización de estado, eliminación) que valida clientes y productos mediante llamadas HTTP a los servicios correspondientes.

La integración básica entre `pedido-service` y `cliente-service` ya está implementada y usa el cliente HTTP compartido del proyecto (`AbstractHttpClient`).

---
Desarrollado como parte de un proyecto para la asignatura "Desarrollo Fullstack" de DuocUC.
