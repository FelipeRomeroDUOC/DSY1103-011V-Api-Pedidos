# RegistroUsuarios

Este documento describe el JFrame `RegistroUsuarios` como una pantalla de escritorio orientada a simplificar el registro manual de nuevos usuarios o clientes mediante solicitudes `POST`.

## Propósito

`RegistroUsuarios` existe para capturar la información de un nuevo usuario desde una interfaz campo a campo, sin obligar a la persona usuaria a conocer el contrato HTTP del backend.

La pantalla actúa como un formulario de alta guiado y busca reducir errores de carga, especialmente en los campos de ubicación, al trabajar con catálogos dinámicos de región y comuna.

## Relación con Spring Boot

Esta pantalla no debe conectarse a repositorios, entidades ni servicios internos del backend.

Su interacción correcta con la aplicación Spring Boot es como consumidor HTTP externo:

- consulta los catálogos de ubicación desde la API;
- construye el payload de alta desde los valores del formulario;
- envía una solicitud `POST` para registrar el usuario;
- muestra éxito o error según la respuesta recibida.

El flujo esperado es que `RegistroUsuarios` consuma el backend a través del cliente HTTP preparado en `docs/http_client.md`, en lugar de llamar código de negocio directamente.

La ventana se abre automáticamente al terminar el arranque de Spring Boot mediante `ApplicationReadyEvent`, a través del listener `RegistroUsuariosStartupListener`.

## Campos del formulario

La pantalla contiene los siguientes campos visibles:

- `nombreClTxt`: nombre del usuario o cliente.
- `rutClTxt`: número de RUT.
- `rutDVClTxt`: dígito verificador del RUT.
- `emailClTxt`: correo electrónico.
- `telefonoClTxt`: teléfono de contacto.
- `direccionClTxt`: dirección de residencia o facturación.
- `regionClComboBox`: selector de región.
- `comunaClComboBox`: selector de comuna.
- `registroClBtn`: botón para enviar el alta.

## Campos y uso esperado

| Campo | Tipo | Uso esperado |
| --- | --- | --- |
| `nombreClTxt` | Texto | Nombre visible del nuevo usuario. |
| `rutClTxt` | Texto numérico | RUT principal del usuario. |
| `rutDVClTxt` | Texto corto | Dígito verificador del RUT. |
| `emailClTxt` | Texto | Correo de contacto. |
| `telefonoClTxt` | Texto | Teléfono de contacto. |
| `direccionClTxt` | Texto | Dirección principal. |
| `regionClComboBox` | Combo | Región seleccionada para limitar las comunas disponibles. |
| `comunaClComboBox` | Combo | Comuna asociada al usuario. |
| `registroClBtn` | Botón | Ejecuta el envío del formulario. |

## Cómo se llenarán los combos

Los combos no deben quedar hardcodeados en el JFrame.

### Región

El combo de regiones se llenará consultando el endpoint:

- `GET /api/regiones`

La UI debe convertir la respuesta a una lista de opciones visibles, normalmente mostrando el nombre de la región y conservando su identificador para posteriores consultas.

### Comuna

El combo de comunas se llenará de forma dependiente de la región seleccionada:

- cuando el usuario elija una región, la UI consulta `GET /api/regiones/{idRegion}/comunas`;
- la respuesta se usa para poblar `comunaClComboBox`;
- el valor seleccionado se envía en el payload final como la comuna elegida.

### Regla de carga

- Las regiones se cargan al abrir la ventana.
- Las comunas se cargan o recargan cada vez que cambia la región.
- El formulario no debe asumir valores fijos.
- Si no hay regiones o comunas disponibles, la UI debe mostrar un mensaje claro y evitar un envío inválido.
- Cuando no se haya elegido ninguna región, el combo de regiones debe mostrar `--Seleccione una región--` como opción inicial.
- Cuando no se haya elegido ninguna región, el combo de comunas debe mostrar `--Seleccione una comuna--` como opción inicial y debe ser su única opción disponible hasta que exista una región seleccionada.
- Al seleccionar una región válida, el combo de comunas debe mantener `--Seleccione una comuna--` como primera opción y agregar debajo las comunas asociadas a esa región.

## Interacción con el registro

Cuando el usuario presione `registroClBtn`, la pantalla deberá construir un payload con los datos del formulario y enviarlo al backend mediante `POST /api/clientes`.

El comportamiento esperado es:

1. Validar que los campos obligatorios estén completos.
2. Tomar la región y la comuna seleccionadas desde los combos.
3. Construir el DTO de creación.
4. Enviar la solicitud al backend.
5. Mostrar una confirmación si el alta fue exitosa.
6. Mostrar el mensaje del backend si ocurre un error de validación o de negocio.

## Validación y mensajes de error

Para que la experiencia sea clara e intuitiva, la pantalla debe combinar validación local y retroalimentación del backend.

### Mensajes por campo

- Cada campo obligatorio debe mostrar un mensaje breve junto al control cuando esté vacío o sea inválido.
- El mensaje debe indicar exactamente qué corregir, por ejemplo: `El nombre es obligatorio.` o `Ingresa un RUT válido.`
- Los campos con error deben resaltarse visualmente para que la persona identifique rápido dónde actuar.
- Email y Teléfono son obligatorios y deben enviarse en el payload de alta.

### Mensaje general del formulario

- Si hay varios errores al mismo tiempo, la pantalla debe mostrar un resumen general en la parte superior o en un área visible del formulario.
- Ese resumen debe invitar a corregir los campos marcados, por ejemplo: `Revisa los campos resaltados antes de continuar.`

### Mensajes del backend

- Si la API responde con un error de validación, la UI debe mostrar el mensaje devuelto por el backend o una versión equivalente en lenguaje simple.
- Si ocurre un conflicto de negocio, el mensaje debe ser específico, por ejemplo: `Ya existe un cliente con ese RUT.`
- Si la respuesta indica un problema de ubicación, el texto debe explicar si falta región, falta comuna o si la comuna no corresponde a la región seleccionada.
- La API ya devuelve errores estructurados, por lo que el formulario puede mostrar directamente el texto de `message` y, si existen, los detalles de `errors`.
- El RUT se valida solo como número sin puntos ni guión; el formulario no compara el DV con el RUT.

### Reglas recomendadas

- Validar primero en la UI para evitar envíos innecesarios.
- No enviar el formulario si faltan campos obligatorios.
- No esconder el motivo del error detrás de mensajes genéricos como `Error inesperado`.
- Mantener los textos cortos, directos y orientados a la acción.

## Estado actual

Actualmente `RegistroUsuarios` ya está enlazado al cliente HTTP para cargar regiones y comunas, y para enviar el `POST` de registro con `nombre`, `rut`, `dv`, `direccion`, `email`, `telefono` y `comuna`.

La ventana se crea y se muestra con éxito al iniciar la aplicación Spring Boot, sin errores de `headless` ni fallos de deserialización.

La interfaz sigue siendo un formulario Swing independiente, pero ahora consume los endpoints del backend en lugar de depender de datos fijos.

## Próximo paso

El siguiente trabajo será hacer correcciones menores al diseño de la interfaz y a la validación de datos del formulario, manteniendo el mismo flujo de arranque y registro HTTP.

## Dependencias conceptuales

- `docs/http_client.md`: describe el cliente HTTP que consumirá la API.
- `src/main/java/cl/apipedidos/ubicacion/controller/UbicacionController.java`: expone los endpoints de regiones y comunas.
- `src/main/java/cl/apipedidos/cliente/controller/ClienteController.java`: expone el endpoint de alta.

## Especificación Gherkin

```gherkin
Feature: Registro de usuarios desde el JFrame RegistroUsuarios

  Background:
    Given la aplicación Spring Boot está disponible
    And existen regiones y comunas cargadas en la base de datos

  Scenario: Cargar regiones al abrir la ventana
    When se abre la pantalla RegistroUsuarios
    Then la UI consulta GET /api/regiones
    And el combo de regiones se llena con los nombres recibidos

  Scenario: Cargar comunas al elegir una región
    Given la UI muestra la lista de regiones
    When el usuario selecciona una región
    Then la UI consulta GET /api/regiones/{idRegion}/comunas
    And el combo de comunas se actualiza con las opciones de esa región

  Scenario: Mantener valores por defecto cuando no hay región seleccionada
    Given la UI no tiene una región seleccionada
    Then el combo de regiones muestra "--Seleccione una región--"
    And el combo de comunas muestra "--Seleccione una comuna--"
    And el combo de comunas no ofrece otras opciones

  Scenario: Registrar un usuario desde el formulario
    Given el formulario contiene datos válidos
    And la región y la comuna están seleccionadas
    When el usuario presiona Registrar
    Then la UI envía POST /api/clientes
    And el backend responde con código 201
    And la UI muestra un mensaje de éxito

  Scenario: Registrar un usuario con datos inválidos
    Given el formulario contiene datos incompletos o inconsistentes
    When el usuario presiona Registrar
    Then la UI evita o rechaza el envío
    And muestra un mensaje de validación claro
```
