# HTTP Client

Este documento define el esqueleto teórico para un consumidor HTTP externo del `cliente-service`. La idea es dejar preparada una pieza reutilizable para que, más adelante, `RegistroUsuarios` o un frontend web consuman la API REST sin depender de repositorios, entidades o servicios internos del backend.

**Nota de actualización:** El proyecto ha sido actualizado al **JDK 25**, requiriendo versiones compatibles de Maven y Lombok.

## Objetivo

- Consumir la API REST del módulo de clientes por HTTP.
- Poblar los combos de región y comuna desde la base de datos a través de endpoints.
- Enviar solicitudes `POST`, `PUT`, `GET` y `DELETE` contra el `cliente-service`.
- Traducir respuestas y errores HTTP a mensajes utilizables por la interfaz.
- Mantener desacoplada la UI del backend para facilitar el reemplazo futuro por un frontend web.

## Alcance

Incluye:
- Cliente HTTP para clientes.
- Cliente HTTP para ubicación.
- DTOs de transporte para requests y responses.
- Mapeadores entre UI y DTOs.
- Manejo de errores HTTP.
- Esqueleto de arranque para abrir la UI cuando Spring Boot ya esté listo.

No incluye:
- Acceso directo a repositorios desde la UI.
- Lógica de negocio duplicada en la interfaz.
- Hardcodear regiones o comunas en los combos.
- Persistencia local alternativa.

## Orden de clases

Este es el orden recomendado para construir e integrar el esqueleto:

1. `ApiPedidosApplication`
- Inicia Spring Boot.
- Dispara la inicialización del contexto.
- Sirve como punto de arranque del backend y, si se desea, de la UI externa.

2. `UbicacionApiClient`
- Consume endpoints de regiones y comunas.
- Devuelve listas para poblar combos.
- Debe ser la primera dependencia de la UI si se cargan catálogos dinámicos.

3. `ClienteApiClient`
- Consume la API de clientes.
- Encapsula las llamadas `POST`, `PUT`, `GET` y `DELETE`.
- Reutiliza la URL base del backend.

4. `ClienteRequestDTO`
- Representa el payload de creación y actualización.
- Puede dividirse en `ClienteCreateRequestDTO` y `ClienteUpdateRequestDTO` o unificarse si la UI usa un formulario único.

5. `ClienteResponseDTO`
- Representa la respuesta del backend.
- Sirve para mostrar datos en pantalla y refrescar tablas o listados.

6. `ApiErrorResponse`
- Estandariza errores devueltos por la API.
- Permite mostrar mensajes claros para `400`, `404`, `409` y `503`.

7. `ClienteMapper`
- Convierte datos de la UI a DTOs.
- Convierte respuestas del backend a modelos de presentación si hace falta.

8. `RegistroUsuariosStartupListener`
- Escucha `ApplicationReadyEvent`.
- Abre la ventana Swing cuando Spring Boot termina de iniciar.
- Separa el ciclo de arranque de la lógica visual.

9. `RegistroUsuarios` o frontend consumidor
- Usa los clientes HTTP.
- No conoce repositorios ni entidades.
- Solo maneja formularios, botones y resultados.

## Estructura teórica sugerida

- `cl.apipedidos.http.client.ClienteApiClient`
- `cl.apipedidos.http.client.UbicacionApiClient`
- `cl.apipedidos.http.dto.ClienteRequestDTO`
- `cl.apipedidos.http.dto.ClienteResponseDTO`
- `cl.apipedidos.http.dto.ApiErrorResponse`
- `cl.apipedidos.http.mapper.ClienteMapper`
- `cl.apipedidos.cliente.gui.RegistroUsuariosStartupListener`
- `cl.apipedidos.cliente.gui.RegistroUsuarios`

Si el proyecto crece, conviene separar además:
- `cl.apipedidos.http.config.HttpClientConfig`
- `cl.apipedidos.http.exception.HttpClientException`

## Contrato HTTP disponible

### Clientes

- `POST /api/clientes`
  - Crea un cliente.
  - Body con nombre, RUT, DV, dirección y comuna.
  - Devuelve `201 Created`.

- `GET /api/clientes`
  - Lista clientes.
  - Soporta filtro por comuna.
  - Devuelve lista de `ClienteResponseDTO`.

- `GET /api/clientes/{identificador}`
  - Busca por ID numérico o por nombre.
  - Devuelve `200 OK` o `404 Not Found`.

- `PUT /api/clientes/{id}`
  - Actualiza un cliente existente.
  - Devuelve el cliente actualizado.

- `DELETE /api/clientes/{id}`
  - Elimina un cliente.
  - Devuelve `204 No Content`.

### Ubicación

- `GET /api/regiones`
  - Lista todas las regiones.

- `GET /api/regiones/{id}/comunas`
  - Lista comunas de una región.

## Responsabilidades por clase

### `ClienteApiClient`

Responsabilidades:
- Enviar solicitudes al backend de clientes.
- Serializar request DTOs.
- Deserializar respuestas DTO.
- Traducir respuestas fallidas en excepciones controladas.

Métodos esperados:
- `crearCliente(...)`
- `listarClientes(...)`
- `obtenerClientePorIdentificador(...)`
- `actualizarCliente(...)`
- `eliminarCliente(...)`

### `UbicacionApiClient`

Responsabilidades:
- Obtener regiones.
- Obtener comunas filtradas por región.
- Soportar el llenado dinámico de combos.

Métodos esperados:
- `listarRegiones()`
- `listarComunasPorRegion(...)`
- `obtenerComunaPorNombre(...)`

### `ClienteMapper`

Responsabilidades:
- Convertir datos del formulario a DTOs.
- Convertir DTOs a estructuras de presentación si se requiere.
- Evitar que la UI trabaje con detalles de transporte.

### `ApiErrorResponse`

Responsabilidades:
- Representar un error estándar.
- Incluir código, mensaje y, opcionalmente, detalles de validación.

Campos sugeridos:
- `status`
- `message`
- `path`
- `timestamp`
- `errors`

## Manejo de errores en la API

La API REST ya devuelve respuestas estructuradas para errores de negocio y validación.

- Los conflictos `409` como nombre o RUT duplicado incluyen un mensaje claro en `message`.
- Los errores `400` de validación pueden incluir mensajes por campo en `errors`.
- El cliente HTTP puede leer `ApiErrorResponse` directamente y mostrar el texto devuelto por el backend sin recurrir a mensajes genéricos.

## Flujo esperado de la UI

1. La aplicación Spring Boot arranca.
2. Se inicializan regiones, comunas y clientes de ejemplo.
3. El listener `RegistroUsuariosStartupListener` abre la UI automáticamente al completar `ApplicationReadyEvent`.
4. La UI llama a `UbicacionApiClient` para poblar los combos.
5. El usuario completa el formulario de cliente.
6. La UI llama a `ClienteApiClient.crearCliente(...)` o `actualizarCliente(...)`.
7. El backend valida y responde.
8. La UI muestra confirmación o error amigable.

## Plan de implementación

### Fase 1: Contrato base
- Definir la base URL del backend.
- Crear DTOs de transporte para request y response.
- Definir el DTO estándar de error.
- Definir el package del cliente HTTP.

### Fase 2: Cliente de ubicación
- Implementar `UbicacionApiClient`.
- Consumir regiones y comunas desde la API.
- Conectar la carga de combos a ese cliente.

### Fase 3: Cliente de clientes
- Implementar `ClienteApiClient`.
- Crear métodos para alta, edición, listado, búsqueda y borrado.
- Manejar respuestas exitosas y errores HTTP.

### Fase 4: Integración con UI
- Ajustar `RegistroUsuarios` para usar el cliente HTTP.
- Abrir `RegistroUsuarios` desde un listener de `ApplicationReadyEvent`.
- Poblar combos al abrir la ventana.
- Convertir el submit del formulario en una llamada `POST` o `PUT`.
- Mostrar errores claros en la interfaz.

### Fase 5: Validación
- Probar contra el `cliente-service` corriendo localmente.
- Verificar casos felices y errores `400`, `404`, `409` y `503`.
- Revisar que regiones y comunas se carguen desde la base.

## Consideraciones de diseño

- La UI no debe acceder a repositorios JPA.
- La UI no debe conocer entidades de persistencia.
- La API de clientes es la fuente de verdad.
- La API de ubicación debe alimentar los combos desde la base de datos.
- El frontend futuro podrá reemplazar a `RegistroUsuarios` sin cambiar el backend.

## Especificación Gherkin

```gherkin
Feature: Cliente HTTP externo

  Background:
    Given el cliente-service está disponible en "http://localhost:8080"
    And existen regiones y comunas reales cargadas en la base de datos
    And la UI consumidora está iniciada como cliente externo

  Scenario: Cargar regiones para el combo
    When la UI solicita GET /api/regiones
    Then la respuesta tiene código 200
    And el body contiene la lista de regiones de Chile
    And el combo de regiones se llena con los nombres recibidos

  Scenario: Cargar comunas de una región seleccionada
    Given la UI tiene seleccionada la región "Metropolitana"
    When la UI solicita GET /api/regiones/{id}/comunas
    Then la respuesta tiene código 200
    And el body contiene las comunas de esa región
    And el combo de comunas se llena con los nombres recibidos

  Scenario: Crear cliente desde la UI
    Given el formulario contiene nombre "Ana Pérez" y comuna "Providencia"
    When la UI envía POST /api/clientes
    Then la respuesta tiene código 201
    And el cliente queda registrado en el backend
    And la UI muestra un mensaje de éxito

  Scenario: Crear cliente con RUT duplicado
    Given ya existe un cliente con RUT "12345678"
    When la UI envía POST /api/clientes con ese RUT
    Then la respuesta tiene código 409
    And la UI muestra el mensaje "RUT ya registrado"

  Scenario: Obtener cliente inexistente
    When la UI solicita GET /api/clientes/999
    Then la respuesta tiene código 404
    And la UI muestra el mensaje "Cliente no encontrado"

  Scenario: Actualizar cliente existente
    Given existe un cliente con id 42
    When la UI envía PUT /api/clientes/42
    Then la respuesta tiene código 200
    And la UI refresca la información mostrada

  Scenario: Eliminar cliente existente
    Given existe un cliente con id 42
    When la UI envía DELETE /api/clientes/42
    Then la respuesta tiene código 204
    And la UI elimina el registro de la vista
```

## Resultado esperado

Al terminar esta implementación, la interfaz consumirá el backend como un cliente HTTP real. Eso deja la base lista para reemplazar `RegistroUsuarios` por un frontend web sin modificar la API del `cliente-service`.
