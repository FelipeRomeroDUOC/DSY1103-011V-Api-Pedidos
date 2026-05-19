# Api Pedidos (Microservicios)

Este proyecto es una arquitectura basada en microservicios desarrollada en Java con Spring Boot. Utiliza un monorepo Maven multi-módulo para separar lógicamente los dominios de la aplicación: gestión de clientes, ubicaciones territoriales, gestión de pedidos y órdenes de fabricación.

## 🚀 Tecnologías utilizadas

- Java 21+
- Spring Boot 3.4.x
- Spring Web
- Spring Cloud OpenFeign (integrado con OkHttp para soporte completo de `PATCH`)
- Spring Data JPA e Hibernate
- Lombok
- Bean Validation
- H2 Database (entorno de desarrollo local)

## 🧱 Estructura del Proyecto (Monorepo)

El proyecto pasó de ser un monolito a una arquitectura multi-módulo real:

```text
Api_Pedidos/
├── pom.xml                    ← POM padre (gestión centralizada de dependencias)
├── cliente-service/           ← Microservicio de Clientes y Ubicaciones (Puerto 8082)
├── producto-service/          ← Microservicio de Catálogo de Productos (Puerto 8083)
├── pedido-service/            ← Microservicio de Pedidos (Puerto 8081)
├── fabricacion-service/       ← Microservicio de Manufactura (Puerto 8086)
├── despacho-service/          ← Microservicio de Despachos (Puerto 8084)
├── estado-service/            ← Microservicio de Auditoría de Estados (Puerto 8085)
└── docs/                      ← Documentación de la API
```

### Interacción de Servicios (Feign)

Los servicios están completamente desacoplados a nivel de código y se comunican a través de peticiones HTTP utilizando clientes Feign. El flujo principal es el siguiente:

- `fabricacion-service (8086)` ──Feign──► `pedido-service (8081)`
- `pedido-service (8081)` ──Feign──► `cliente-service (8082)` (Valida cliente)
- `pedido-service (8081)` ──Feign──► `producto-service (8083)` (Valida catálogo y obtiene precio)
- `pedido-service (8081)` ──Feign──► `estado-service (8085)` (Notifica asincrónicamente el cambio de estado)
- `despacho-service (8084)` ──Feign──► `pedido-service (8081)` (Verifica estado LISTO)

## 🌐 Microservicios y Endpoints

### 1. `cliente-service` (http://localhost:8082)
Gestiona el catálogo completo de clientes y el mapa de ubicaciones (Regiones, Provincias y Comunas de Chile).

**Clientes:**
- `POST /api/clientes`: Crea un cliente nuevo.
- `GET /api/clientes`: Lista clientes (permite filtrar por comuna).
- `GET /api/clientes/{identificador}`: Busca un cliente por ID numérico o por nombre.
- `PUT /api/clientes/{id}`: Actualiza un cliente existente.
- `DELETE /api/clientes/{id}`: Elimina un cliente.

**Ubicaciones:**
- `GET /api/regiones`: Lista todas las regiones.
- `GET /api/regiones/{idRegion}/provincias`: Lista provincias por región.
- `GET /api/regiones/{idRegion}/comunas`: Lista comunas por región.

### 2. `producto-service` (http://localhost:8083)
Gestiona el catálogo de productos disponibles para la venta.

- `POST /api/productos`: Crea un producto nuevo.
- `GET /api/productos`: Lista todos los productos activos (acepta `?incluirInactivos=true`).
- `GET /api/productos/{id}`: Obtiene un producto por ID.
- `PUT /api/productos/{id}`: Actualiza un producto existente.
- `DELETE /api/productos/{id}`: Desactiva un producto (Soft delete).
- `PATCH /api/productos/{id}/activar`: Reactiva un producto desactivado.
- `GET /api/productos/ping`: Healthcheck.

### 3. `pedido-service` (http://localhost:8081)
Gestiona la creación y el ciclo de vida de los pedidos.

- `POST /api/pedidos`: Crea un pedido (valida que el cliente exista en `cliente-service`).
- `GET /api/pedidos`: Lista todos los pedidos.
- `GET /api/pedidos/{id}`: Obtiene un pedido por su ID interno.
- `GET /api/pedidos/numero/{numeroPedido}`: Obtiene un pedido por su correlativo (ej. `PED-003`).
- `PATCH /api/pedidos/{id}/estado`: Actualiza el estado de un pedido (PENDIENTE, EN_FABRICACION, LISTO, etc).
- `DELETE /api/pedidos/{id}`: Elimina un pedido.

### 4. `fabricacion-service` (http://localhost:8086)
Orquesta el ensamblaje de los pedidos de manufactura.

- `POST /api/fabricacion`: Crea una orden de fabricación asociada a un ID de pedido.
- `GET /api/fabricacion/{id}`: Consulta una orden de fabricación.
- `PATCH /api/fabricacion/{id}/estado`: Cambia el estado de una orden (ej. a `TERMINADO`) y notifica automáticamente al `pedido-service`.
- `GET /api/fabricacion/ping`: Healthcheck.

### 5. `despacho-service` (http://localhost:8084)
Gestiona el envío o retiro de los pedidos terminados.

- `POST /api/despachos`: Registra un despacho validando que el pedido esté `LISTO`.
- `GET /api/despachos`: Lista todos los despachos (filtra por `tipo=REGION`, etc.).
- `GET /api/despachos/{pedidoId}`: Obtiene el despacho asociado a un pedido.
- `PUT /api/despachos/{id}`: Actualiza transportista o tracking.
- `GET /api/despachos/ping`: Healthcheck.

### 6. `estado-service` (http://localhost:8085)
Gestiona la auditoría histórica de los cambios de estado de todos los pedidos. **Servicio pasivo**.

- `POST /api/estados`: Registra un cambio (usado internamente por `pedido-service`).
- `GET /api/estados/{pedidoId}`: Devuelve el historial inmutable de saltos de estado del pedido especificado.
- `GET /api/estados/ping`: Healthcheck.

## ▶️ Cómo ejecutar el proyecto

Para correr la plataforma en desarrollo local, debes levantar los microservicios en terminales separadas.

Desde la raíz del proyecto (`Api_Pedidos/`), ejecuta:

**Terminal 1 (Clientes):**
```bash
./mvnw spring-boot:run -pl cliente-service
```

**Terminal 2 (Pedidos):**
```bash
./mvnw spring-boot:run -pl pedido-service
```

**Terminal 3 (Productos):**
```bash
./mvnw spring-boot:run -pl producto-service
```

**Terminal 4 (Fabricación):**
```bash
./mvnw spring-boot:run -pl fabricacion-service
```

**Terminal 5 (Despachos):**
```bash
./mvnw spring-boot:run -pl despacho-service
```

**Terminal 6 (Estados):**
```bash
./mvnw spring-boot:run -pl estado-service
```

O si prefieres utilizar el script de conveniencia para Windows:
```cmd
start-all.bat
```

## ⚙️ Configuración y Bases de Datos

Cada microservicio mantiene su propia base de datos independiente (Base de Datos por Servicio). Por defecto, utilizan H2 en archivo local, almacenando su persistencia en:
- `cliente-service/data/cliente_service.mv.db`
- `producto-service/data/producto_service.mv.db`
- `pedido-service/data/pedido_service.mv.db`
- `fabricacion-service/data/fabricacion_service.mv.db`
- `despacho-service/data/despacho_service.mv.db`
- `estado-service/data/estado_service.mv.db`

La configuración de H2 se realiza mediante los archivos `application-h2.properties` ubicados en el bloque `src/main/resources/` de cada módulo.

## 🧭 Ejemplos de uso (Postman)

A continuación, la secuencia de flujo completo a través de los microservicios:

### 1. Crear un Cliente (`cliente-service`)
**POST** `http://localhost:8082/api/clientes`
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

### 2. Crear un Producto (`producto-service`)
**POST** `http://localhost:8083/api/productos`
```json
{
  "nombre": "Silla Ergonómica Pro",
  "descripcion": "Silla de oficina con soporte lumbar ajustable",
  "categoria": "Mobiliario",
  "precioBase": 149990.0
}
```

### 3. Crear un Pedido (`pedido-service`)
**POST** `http://localhost:8081/api/pedidos`
```json
{
  "numeroPedido": "PED-101",
  "clienteId": 1,
  "tipoDespacho": "RM",
  "items": [
    {
      "productoId": 1,
      "cantidad": 2,
      "precioUnitario": 149990.0
    }
  ]
}
```
*(Valida que el cliente 1 exista en `cliente-service` y que el producto 1 exista en `producto-service`)*

### 4. Iniciar Fabricación (`fabricacion-service`)
**POST** `http://localhost:8086/api/fabricacion`
```json
{
  "numeroPedido": 1,
  "usuarioResponsable": "Operador 1"
}
```
*(Cambiará el estado del pedido a `EN_FABRICACION` internamente en `pedido-service`)*

### 5. Finalizar Fabricación (`fabricacion-service`)
**PATCH** `http://localhost:8086/api/fabricacion/1/estado`
```json
{
  "nuevoEstado": "TERMINADO"
}
```
*(Notifica a `pedido-service` vía HTTP PATCH que debe actualizar el estado del pedido a `LISTO`)*

### 6. Registrar Despacho (`despacho-service`)
**POST** `http://localhost:8084/api/despachos`
```json
{
  "pedidoId": 1,
  "tipoDespacho": "RM",
  "fechaDespacho": "2026-05-20"
}
```
*(Requiere que el pedido esté en estado `LISTO` previo en `pedido-service`)*

### 7. Ver historial de estados (`estado-service`)
**GET** `http://localhost:8085/api/estados/1`
*(Retornará el salto a `EN_FABRICACION` y a `LISTO` que fueron registrados tras bambalinas)*

## 🧪 Notas Técnicas de la Refactorización

- **Soporte de PATCH en Feign:** Para soportar el verbo HTTP `PATCH` en la comunicación entre microservicios, el framework OpenFeign fue configurado para utilizar **OkHttp** en lugar de `HttpURLConnection` de Java (el cual bloquea métodos PATCH). Esto se habilita con `spring.cloud.openfeign.okhttp.enabled=true`.
- **Estandarización de Respuesta (ApiResponse<T>):** Todos los endpoints REST ahora devuelven un objeto genérico uniforme (`{mensaje, data, exitoso, timestamp}`), garantizando una estructura predecible para los consumidores del API. Las interfaces de Feign también esperan este envoltorio y extraen los datos en tiempo de ejecución.
- **Inyección de Dependencias Limpia:** Se adoptó la anotación `@RequiredArgsConstructor` de Lombok a nivel de clase para inyectar dependencias (vía `private final`), eliminando el código repetitivo de constructores manuales.
- **Optimización JPA y Generación de IDs:** Se reemplazó el uso riesgoso de `@Data` en las entidades por `@Getter` y `@Setter` explícitos. Toda generación de identificador primario (`@Id`) fue delegada 100% a la base de datos a través de `@GeneratedValue(strategy = GenerationType.IDENTITY)`.
- **Atomicidad y Transacciones:** Las operaciones de cambio de estado en `fabricacion-service` están resguardadas por `@Transactional`. Si la llamada Feign hacia `pedido-service` falla, los cambios en la base de datos de fabricación realizan un *rollback* para mantener la consistencia eventual.
- **Auto-Carga de Datos:** Al iniciar `cliente-service` con la base vacía, las regiones y comunas de Chile se importan automáticamente desde el recurso JSON provisto para disponer de las ubicaciones.

---
Desarrollado como parte de un proyecto de arquitectura para la asignatura "Desarrollo Fullstack" de DuocUC.
