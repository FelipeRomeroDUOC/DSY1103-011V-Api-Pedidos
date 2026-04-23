# entidad_cliente

Este módulo implementa la capa de cliente del proyecto bajo el nuevo paquete raíz `cl.apipedidos`. La documentación refleja la estructura real del código, incluyendo persistencia, negocio, API REST, DTOs y el formulario Swing que quedó generado en el árbol del proyecto.

## 📦 Archivos creados

| Archivo | Propósito |
| :--- | :--- |
| `src/main/java/cl/apipedidos/ApiPedidosApplication.java` | Punto de entrada de Spring Boot. Arranca la aplicación y habilita el escaneo de componentes bajo `cl.apipedidos`. |
| `src/main/java/cl/apipedidos/cliente/entity/Cliente.java` | Entidad JPA que representa a un cliente en la base de datos. |
| `src/main/java/cl/apipedidos/ubicacion/entity/Region.java` | Entidad JPA que representa una región de Chile. |
| `src/main/java/cl/apipedidos/ubicacion/entity/Comuna.java` | Entidad JPA que representa una comuna de Chile y su región asociada. |
| `src/main/java/cl/apipedidos/ubicacion/repository/RegionRepository.java` | Repositorio Spring Data JPA para regiones. |
| `src/main/java/cl/apipedidos/ubicacion/repository/ComunaRepository.java` | Repositorio Spring Data JPA para comunas. |
| `src/main/java/cl/apipedidos/ubicacion/config/UbicacionDataLoader.java` | Inicializador que carga regiones y comunas reales de Chile desde un recurso local. |
| `src/main/java/cl/apipedidos/cliente/repository/ClienteRepository.java` | Repositorio Spring Data JPA con consultas derivadas para clientes. |
| `src/main/java/cl/apipedidos/cliente/service/ClienteService.java` | Capa de negocio con validaciones, normalización y operaciones CRUD. |
| `src/main/java/cl/apipedidos/cliente/controller/ClienteController.java` | Capa REST que expone los endpoints de clientes. |
| `src/main/java/cl/apipedidos/cliente/dto/ClienteCreateRequestDTO.java` | DTO de entrada para crear clientes. |
| `src/main/java/cl/apipedidos/cliente/dto/ClienteUpdateRequestDTO.java` | DTO de entrada para actualizar clientes. |
| `src/main/java/cl/apipedidos/cliente/dto/ClienteResponseDTO.java` | DTO de salida para responder a la API. |
| `src/main/java/cl/apipedidos/cliente/config/ClienteDataLoader.java` | Inicializador que carga clientes de ejemplo si la tabla está vacía. |
| `src/main/java/cl/apipedidos/cliente/config/ClienteSchemaMigrator.java` | Ajusta el esquema al iniciar, elimina filas incompletas y renumera los clientes para que queden consecutivos. |
| `src/main/java/cl/apipedidos/cliente/gui/RegistroUsuariosStartupListener.java` | Listener que abre el JFrame `RegistroUsuarios` cuando Spring Boot termina de iniciar. |
| `src/main/java/cl/apipedidos/cliente/gui/RegistroUsuarios.java` | Formulario Swing generado por NetBeans para registrar usuarios campo a campo. Actualmente está orientado a envíos `POST` y ya consume el backend HTTP. |

## 🧩 Modelo de datos

### Entidad: `Region`

Archivo: `src/main/java/cl/apipedidos/ubicacion/entity/Region.java`

#### Atributos
- `idRegion: String` - Identificador oficial de la región de Chile.
- `nombreRegion: String` - Nombre de la región.
- `comunas: List<Comuna>` - Comunas asociadas a la región.

#### Métodos
- `normalize()` - Recorta espacios de los campos antes de persistir o actualizar.

#### Qué hace
Representa la tabla `regiones` y sirve como padre de la relación `One-to-Many` con comunas.

### Entidad: `Comuna`

Archivo: `src/main/java/cl/apipedidos/ubicacion/entity/Comuna.java`

#### Atributos
- `idComuna: String` - Identificador oficial de la comuna de Chile.
- `nombreComuna: String` - Nombre de la comuna.
- `region: Region` - Región a la que pertenece la comuna.
- `clientes: List<Cliente>` - Clientes asociados a la comuna.

#### Métodos
- `normalize()` - Recorta espacios de los campos antes de persistir o actualizar.

#### Qué hace
Representa la tabla `comunas` y materializa la relación `One-to-Many` con `Region` y con `Cliente`.

### Entidad: `Cliente`

Archivo: `src/main/java/cl/apipedidos/cliente/entity/Cliente.java`

#### Atributos
- `idCliente: Long` - Identificador numérico del cliente, asignado de forma secuencial a partir del último ID registrado.
- `nombreCl: String` - Nombre del cliente.
- `rutCl: Long` - RUT numérico.
- `divCl: String` - Dígito verificador del RUT.
- `direccionCl: String` - Dirección del cliente.
- `emailCl: String` - Correo electrónico del cliente.
- `telefonoCl: String` - Teléfono de contacto del cliente.
- `comuna: Comuna` - Comuna asociada al cliente.
- `fechaRegistro: LocalDate` - Fecha en que se registró el cliente.

#### Métodos
- `beforeCreate()` - Se ejecuta antes de persistir. Si `fechaRegistro` viene vacía, la asigna con la fecha actual y normaliza texto, DV, email y teléfono.
- `beforeUpdate()` - Se ejecuta antes de actualizar. Normaliza texto, DV, email y teléfono.

#### Qué hace
Representa la tabla `clientes` y concentra el mapeo entre base de datos y dominio.

## 🗂️ Acceso a datos

### Repository: `RegionRepository`

Archivo: `src/main/java/cl/apipedidos/ubicacion/repository/RegionRepository.java`

#### Métodos
- `findByNombreRegionIgnoreCase(String nombreRegion)` - Busca una región por nombre.
- `existsByNombreRegionIgnoreCase(String nombreRegion)` - Verifica si ya existe una región con ese nombre.

#### Qué hace
Permite consultar regiones persistidas sin exponer lógica adicional en controladores o servicios.

### Repository: `ComunaRepository`

Archivo: `src/main/java/cl/apipedidos/ubicacion/repository/ComunaRepository.java`

#### Métodos
- `findByNombreComunaIgnoreCase(String nombreComuna)` - Busca una comuna por nombre.
- `findByRegion_NombreRegionIgnoreCase(String nombreRegion)` - Lista comunas por nombre de región.
- `existsByNombreComunaIgnoreCase(String nombreComuna)` - Verifica si ya existe una comuna con ese nombre.

#### Qué hace
Expone consultas derivadas para trabajar con la relación entre región y comuna.

### Repository: `ClienteRepository`

Archivo: `src/main/java/cl/apipedidos/cliente/repository/ClienteRepository.java`

#### Métodos
- `findByNombreClIgnoreCase(String nombreCl)` - Busca un cliente por nombre sin distinguir mayúsculas/minúsculas.
- `findByComuna_NombreComunaIgnoreCase(String nombreComuna)` - Devuelve los clientes filtrados por nombre de comuna.
- `existsByNombreClIgnoreCase(String nombreCl)` - Verifica si ya existe un cliente con ese nombre.
- `existsByNombreClIgnoreCaseAndIdClienteNot(String nombreCl, Long idCliente)` - Verifica duplicidad de nombre al editar, excluyendo el cliente actual.
- `existsByRutCl(Long rutCl)` - Verifica si ya existe un RUT registrado.
- `existsByRutClAndIdClienteNot(Long rutCl, Long idCliente)` - Verifica duplicidad de RUT al editar, excluyendo el cliente actual.
- `findMaxIdCliente()` - Devuelve el mayor ID registrado para calcular el siguiente correlativo.

#### Qué hace
Extiende `JpaRepository` y deja a Spring Data JPA la generación automática de consultas.

## 🧠 Lógica de negocio

### Service: `ClienteService`

Archivo: `src/main/java/cl/apipedidos/cliente/service/ClienteService.java`

#### Métodos públicos
- `crear(ClienteCreateRequestDTO request)`
	- Parámetro: `request` con los datos recibidos desde la capa REST.
	- Qué hace: valida que el nombre y el RUT no estén duplicados, asigna el siguiente ID disponible, resuelve la comuna real y guarda el registro.
- `listar(String comuna)`
  - Parámetro: `comuna` opcional.
  - Qué hace: devuelve todos los clientes o solo los filtrados por comuna.
- `obtenerPorId(Long id)`
  - Parámetro: `id` del cliente.
  - Qué hace: busca un cliente por ID o lanza `404` si no existe.
- `obtenerPorIdentificador(String identificador)`
  - Parámetro: `identificador`, que puede ser un ID numérico o un nombre.
  - Qué hace: resuelve la búsqueda por ID o por nombre, con comparación case-insensitive.
- `actualizar(Long id, ClienteUpdateRequestDTO request)`
	- Parámetros: `id` del cliente a modificar y `request` con los nuevos datos.
	- Qué hace: valida que exista, verifica unicidad de nombre y RUT, resuelve la comuna real, aplica los cambios y persiste.
- `eliminar(Long id)`
  - Parámetro: `id` del cliente.
  - Qué hace: elimina el cliente si existe; si no, responde con `404`.

#### Métodos privados
- `validarUnicidadParaCreacion(Cliente cliente)` - Evita duplicados por nombre o RUT al crear.
- `validarUnicidadParaActualizacion(Long id, Cliente cliente)` - Evita duplicados por nombre o RUT al editar.
- `construirCliente(ClienteCreateRequestDTO request)` - Construye la entidad a partir de los datos de alta.
- `construirCliente(ClienteUpdateRequestDTO request)` - Construye la entidad auxiliar a partir de los datos de edición.
- `normalizarTexto(String texto)` - Recorta espacios de un texto si no es nulo.
- `normalizarDv(String divCl)` - Recorta espacios y normaliza el DV a mayúsculas.
- `resolverComuna(String nombreComuna)` - Busca la comuna real y devuelve su entidad.
- `esNumerico(String valor)` - Detecta si el identificador solo contiene dígitos.
- `buscarClientePorId(Long id)` - Centraliza la búsqueda por ID y la respuesta 404.
- `siguienteIdCliente()` - Consulta el mayor ID actual y retorna el correlativo siguiente.

#### Qué hace
Es la capa que concentra la lógica del módulo: validaciones de negocio, normalización, control de duplicados y traducción de errores HTTP.

## 🌐 Exposición REST

### Controller: `ClienteController`

Archivo: `src/main/java/cl/apipedidos/cliente/controller/ClienteController.java`

#### Endpoints y métodos
- `crearCliente(ClienteCreateRequestDTO request)`
  - Método HTTP: `POST`
  - Ruta: `/api/clientes`
	- Parámetro: `request` con `nombre`, `rut`, `dv`, `direccion`, `email`, `telefono` y `comuna`.
	- Qué hace: delega al service y devuelve el recurso creado con `201 Created`.
- `listarClientes(String comuna)`
  - Método HTTP: `GET`
  - Ruta: `/api/clientes`
  - Parámetro: `comuna` como query param opcional.
	- Qué hace: obtiene la lista de clientes y la convierte a DTO de respuesta.
- `obtenerCliente(String identificador)`
  - Método HTTP: `GET`
  - Ruta: `/api/clientes/{identificador}`
  - Parámetro: `identificador` como path variable.
  - Qué hace: busca por ID o por nombre.
- `actualizarCliente(Long id, ClienteUpdateRequestDTO request)`
  - Método HTTP: `PUT`
  - Ruta: `/api/clientes/{id}`
	- Parámetros: `id` y `request` con `nombre`, `rut`, `dv`, `direccion`, `email`, `telefono` y `comuna`.
	- Qué hace: delega la actualización al service.
- `eliminarCliente(Long id)`
  - Método HTTP: `DELETE`
  - Ruta: `/api/clientes/{id}`
  - Parámetro: `id`.
  - Qué hace: elimina el cliente y responde `204 No Content`.

#### Qué hace
Actúa como puente entre HTTP y negocio. Recibe DTOs, delega en el service y devuelve DTOs de salida.

## 🧾 DTOs

### `ClienteCreateRequestDTO`

Archivo: `src/main/java/cl/apipedidos/cliente/dto/ClienteCreateRequestDTO.java`

#### Parámetros
- `nombreCl: String`
- `rutCl: Long`
- `divCl: String`
- `direccionCl: String`
- `emailCl: String`
- `telefonoCl: String`
- `comuna: String`

#### Qué hace
Define el payload de creación y aplica validaciones de Bean Validation. En esta versión no se compara el DV con el RUT; solo se exige que el RUT llegue como número, sin puntos ni guión. Email y Teléfono forman parte obligatoria del POST.

### `ClienteUpdateRequestDTO`

Archivo: `src/main/java/cl/apipedidos/cliente/dto/ClienteUpdateRequestDTO.java`

#### Parámetros
- `nombreCl: String`
- `rutCl: Long`
- `divCl: String`
- `direccionCl: String`
- `emailCl: String`
- `telefonoCl: String`
- `comuna: String`

#### Qué hace
Define el payload de actualización. Repite la estructura del alta para permitir edición completa del cliente. Igual que en creación, el DV no se valida matemáticamente contra el RUT. Email y Teléfono también son obligatorios.

### `ClienteResponseDTO`

Archivo: `src/main/java/cl/apipedidos/cliente/dto/ClienteResponseDTO.java`

#### Parámetros
- `idCliente: Long`
- `nombreCl: String`
- `rutCl: Long`
- `divCl: String`
- `direccionCl: String`
- `emailCl: String`
- `telefonoCl: String`
- `comuna: String`
- `region: String`
- `fechaRegistro: LocalDate`

#### Qué hace
Encapsula la respuesta pública de la API, incluyendo el identificador, la comuna y la región asociadas, y la fecha de registro.

## 🧰 Carga de datos iniciales

### `ClienteDataLoader`

Archivo: `src/main/java/cl/apipedidos/cliente/config/ClienteDataLoader.java`

#### Método principal
- `run(String... args)`
  - Parámetro: `args` con los argumentos de arranque.
  - Qué hace: si la tabla está vacía, inserta clientes de ejemplo al iniciar la aplicación.

#### Método auxiliar
- `crearCliente(String nombre, Long rut, String dv, String direccion, String email, String telefono, String comuna, LocalDate fechaRegistro)`
  - Parámetros: datos base de cada cliente de ejemplo.
	- Qué hace: construye una instancia de `Cliente` para persistirla con IDs secuenciales explícitos.

#### Qué hace
Permite levantar la app con datos de prueba listos para consultar desde Postman o desde una UI. Resuelve la comuna real desde la base de datos antes de guardar cada cliente.

### `ClienteSchemaMigrator`

Archivo: `src/main/java/cl/apipedidos/cliente/config/ClienteSchemaMigrator.java`

#### Método principal
- `run(String... args)`
	- Parámetro: `args` con los argumentos de arranque.
	- Qué hace: asegura las columnas `email_cl` y `telefono_cl`, elimina clientes con datos incompletos y renumera los registros para que los IDs queden consecutivos desde 1.

#### Qué hace
Actúa como saneamiento automático al iniciar la aplicación. Conserva el esquema existente, limpia filas inválidas y deja los IDs de cliente ordenados sin huecos.

### `UbicacionDataLoader`

Archivo: `src/main/java/cl/apipedidos/ubicacion/config/UbicacionDataLoader.java`

#### Método principal
- `run(String... args)`
	- Parámetro: `args` con los argumentos de arranque.
	- Qué hace: si la base está vacía, carga las 16 regiones y las comunas reales de Chile desde el recurso local `data/chile-divisiones-territoriales.json`.

#### Qué hace
Inicializa las tablas de ubicación con datos reales, fijos e inmutables.

## 🖥️ Interfaz Swing

### `RegistroUsuarios`

Archivo: `src/main/java/cl/apipedidos/cliente/gui/RegistroUsuarios.java`

#### Métodos

- `RegistroUsuarios()` - Inicializa el formulario llamando a `initComponents()`.
- `main(String[] args)` - Ejecuta la ventana principal con el look and feel Nimbus cuando esté disponible.
- `initComponents()` - Método generado por NetBeans que arma la interfaz visual.

#### Campos visibles en la ventana

- `nombreClTxt`
- `rutClTxt`
- `rutDVClTxt`
- `emailClTxt`
- `telefonoClTxt`
- `direccionClTxt`
- `comunaClComboBox`
- `regionClComboBox`
- `registroClBtn`

#### Qué hace
Es un formulario visual para registrar usuarios campo a campo mediante solicitudes `POST`. La captura de comuna y región ya no se hace por texto libre: ahora se selecciona desde combos, lo que prepara la interfaz para trabajar con las entidades reales `Comuna` y `Region`. Email y Teléfono también son obligatorios en la validación local del formulario y ahora viajan en el body del POST. Actualmente ya está enlazado al backend REST, carga catálogos dinámicos y se abre automáticamente al completar el arranque de Spring Boot. En esta versión solo se valida que el RUT sea numérico y se omite la comparación del DV.

## ✅ Estado actual del módulo

- El paquete raíz del proyecto ya quedó en `cl.apipedidos`.
- La API REST de clientes está disponible y compila correctamente.
- El módulo de clientes usa DTOs de entrada y salida.
- La relación geográfica real está modelada como `Region -> Comuna -> Cliente`.
- El POST de clientes exige `nombre`, `rut`, `dv`, `direccion`, `email`, `telefono` y `comuna`.
- El proyecto carga datos de ejemplo al arrancar si la tabla está vacía.
- El ID del cliente se asigna de forma secuencial tomando el mayor ID existente y sumando uno.
- Al iniciar la app, el esquema de clientes se corrige si faltan columnas y las filas incompletas se eliminan antes de renumerar.
- La interfaz Swing `RegistroUsuarios` existe, usa combos para región y comuna, se conecta al servicio REST y se abre automáticamente al terminar el arranque de Spring Boot.

## 🧪 Especificación funcional actual

### Endpoints disponibles
| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `POST` | `/api/clientes` | Registrar un nuevo cliente. |
| `GET` | `/api/clientes` | Obtener todos los clientes o filtrar por comuna. |
| `GET` | `/api/clientes/{identificador}` | Obtener un cliente por ID numérico o por nombre. |
| `PUT` | `/api/clientes/{id}` | Actualizar un cliente existente. |
| `DELETE` | `/api/clientes/{id}` | Eliminar un cliente por ID. |

### Reglas de negocio implementadas
- No se permiten nombres duplicados.
- No se permiten RUT duplicados.
- El RUT se valida solo como valor numérico, sin puntos ni guión.
- El DV se normaliza a mayúsculas, pero no se verifica contra el RUT por ahora.
- La comuna se resuelve contra la base de datos real de comunas de Chile.
- Si una búsqueda falla, la API responde con `404 Cliente no encontrado`.
- Si hay conflicto de unicidad, la API responde con `409`.

---

## 🧪 Especificación Gherkin

> Nota: `Pedidos` es un servicio aparte (`pedido-service`) y todavía no está implementado. Los escenarios que lo mencionan deben interpretarse como integración futura entre servicios.

```gherkin
Feature: Gestión de clientes

	Background:
		Given la base de datos db_clientes está disponible
		And el servicio pedido-service está operativo
		And existe la región "Metropolitana" con comunas "Providencia" y "Las Condes"

	Scenario: Registrar un cliente nuevo exitosamente
		Given no existe un cliente con RUT "12.345.678-9"
		When se envía POST /api/clientes con:
			| nombre    | Ana Pérez       |
			| rut       | 12.345.678-9    |
			| email     | ana@ejemplo.cl  |
			| telefono  | +56912345678    |
			| direccion | Av. Siempre Viva|
			| comuna    | Providencia     |
		Then la respuesta tiene código 201
		And el body contiene el id generado y la fechaRegistro
		And el body contiene el campo comuna con valor "Providencia"
		And el body contiene el campo región con valor "Metropolitana"

	Scenario: Registrar cliente con RUT duplicado
		Given ya existe un cliente con RUT "12.345.678-9"
		When se envía POST /api/clientes con RUT "12.345.678-9"
		Then la respuesta tiene código 409
		And el body contiene el mensaje "RUT ya registrado"

	Scenario: Listar todos los clientes
		Given existen 3 clientes registrados en el sistema
		When se envía GET /api/clientes
		Then la respuesta tiene código 200
		And el body es una lista con 3 elementos
		And cada elemento contiene los campos "comuna" y "región"

	Scenario: Listar clientes filtrados por comuna
		Given existen 5 clientes registrados en el sistema
		And 3 de ellos tienen comuna "Providencia"
		When se envía GET /api/clientes?comuna=Providencia
		Then la respuesta tiene código 200
		And el body es una lista con 3 elementos
		And todos los elementos tienen comuna "Providencia"

	Scenario: Listar clientes por comuna sin resultados
		Given no existen clientes con comuna "Antártica"
		When se envía GET /api/clientes?comuna=Antártica
		Then la respuesta tiene código 200
		And el body es una lista vacía

	Scenario: Listar clientes filtrados por región
		Given existen 8 clientes registrados en el sistema
		And 5 de ellos pertenecen a comunas de la región "Metropolitana"
		When se envía GET /api/clientes?region=Metropolitana
		Then la respuesta tiene código 200
		And el body es una lista con 5 elementos
		And todos los elementos pertenecen a la región "Metropolitana"

	Scenario: Listar clientes por región sin resultados
		Given no existen clientes en comunas de la región "Aysén"
		When se envía GET /api/clientes?region=Aysén
		Then la respuesta tiene código 200
		And el body es una lista vacía

	Scenario: Obtener cliente por ID existente
		Given existe un cliente con id 42 y comuna "Providencia"
		When se envía GET /api/clientes/42
		Then la respuesta tiene código 200
		And el body contiene el campo comuna con valor "Providencia"
		And el body contiene el campo región con valor "Metropolitana"

	Scenario: Obtener cliente por ID inexistente
		Given no existe un cliente con id 999
		When se envía GET /api/clientes/999
		Then la respuesta tiene código 404
		And el body contiene el mensaje "Cliente no encontrado"

	Scenario: Actualizar comuna de un cliente
		Given existe un cliente con id 42 y comuna "Providencia"
		When se envía PUT /api/clientes/42 con comuna "Las Condes"
		Then la respuesta tiene código 200
		And el cliente con id 42 tiene comuna "Las Condes"
		And el cliente con id 42 sigue perteneciendo a la región "Metropolitana"

	Scenario: Actualizar datos de contacto de un cliente
		Given existe un cliente con id 42 y email "ana@ejemplo.cl"
		When se envía PUT /api/clientes/42 con email "nuevo@ejemplo.cl"
		Then la respuesta tiene código 200
		And el cliente con id 42 tiene email "nuevo@ejemplo.cl"

	Scenario: Obtener historial de pedidos de un cliente
		Given existe un cliente con id 42
		And pedido-service retorna 2 pedidos para ese cliente
		When se envía GET /api/clientes/42/pedidos
		Then la respuesta tiene código 200
		And el body es una lista con 2 pedidos

	Scenario: Historial de pedidos cuando pedido-service no responde
		Given existe un cliente con id 42
		And pedido-service no está disponible
		When se envía GET /api/clientes/42/pedidos
		Then la respuesta tiene código 503
		And el body contiene el mensaje "Servicio de pedidos no disponible"
```
