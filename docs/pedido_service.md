# Pedido Service

Resumen

- Nombre del módulo: `pedido` (pedido-service).
- Responsabilidad: gestionar el ciclo de vida de pedidos (crear, consultar, actualizar estado, eliminar, agregar ítems).
- Integración: valida cliente y consulta productos mediante llamadas HTTP a otros servicios del repositorio usando `AbstractHttpClient`.

Componentes principales

- Entidades: `Pedido`, `ItemPedido`, `EstadoPedido`, `TipoDespacho`.
- DTOs: `CreatePedidoRequest`, `CreateItemRequest`, `PedidoDTO`, `ItemPedidoDTO`, `UpdateEstadoRequest`.
- Repositorios: `PedidoRepository`, `ItemPedidoRepository` (Spring Data JPA).
- Servicio: `PedidoService` (lógica de negocio, validaciones, cálculos de subtotal/total).
- Controlador: `PedidoController` (endpoints REST).
- Cliente HTTP: `ClienteServiceClient` (extiende `AbstractHttpClient`) para validar existencia/estado del cliente.

Endpoints relevantes

- `POST /api/pedidos` — Crear pedido
  - Body (ejemplo):

```json
{
  "clienteId": 10,
  "tipoDespacho": "DOMICILIO",
  "items": [
    { "productoId": 100, "cantidad": 2, "precioUnitario": 12990 },
    { "productoId": 101, "cantidad": 1, "precioUnitario": 5990 }
  ]
}
```

- `GET /api/pedidos` — Listar pedidos (soporta filtros por estado, tipo, clienteId)
- `GET /api/pedidos/{id}` — Obtener pedido por ID
- `GET /api/pedidos/numero/{numeroPedido}` — Obtener por número único
- `PATCH /api/pedidos/{id}/estado` — Actualizar estado (ej: EN_PREPARACION → DESPACHADO)
- `DELETE /api/pedidos/{id}` — Eliminar pedido (aplica reglas de negocio)
- `POST /api/pedidos/{id}/items` — Agregar item a pedido existente

Configuración

- Claves de configuración relevantes (archivo `application.properties` / perfiles):
  - `services.cliente-service.url` — URL base del cliente-service (por defecto `http://localhost:8080`).
  - `services.producto-service.url` — URL base del producto-service (por defecto `http://localhost:8081`).
  - `services.despacho-service.url` — URL base del despacho-service (por defecto `http://localhost:8082`).

Ejecución local

- Perfil H2 (por defecto):
  - Ejecutar: `./mvnw spring-boot:run` (arranca con `h2` si no se especifica otro perfil).
  - Alternativa: `./mvnw -q -DskipTests compile && ./mvnw -Dtest=PedidoServiceTest test` para ejecutar pruebas unitarias específicas.
- Para pruebas de integración que requieran `cliente-service`, iniciar el servicio cliente o usar simulación (ver Testing).

Testing

- Unit tests: `src/test/java/.../pedido/service/PedidoServiceTest.java` (Mockito + JUnit).
- Recomendado: agregar tests de integración con WireMock o MockWebServer para simular `cliente-service` y `producto-service` y validar flujos de error (404/502) y éxito.

Notas técnicas

- El proyecto sigue la convención del repositorio: uso de `AbstractHttpClient` para llamadas HTTP sin reactividad (sin WebFlux).
- `spring.jpa.open-in-view` está deshabilitado (`false`) en la configuración para evitar LazyInitializationException en capas de presentación.
- Identificadores de entidades usan `@GeneratedValue(strategy = GenerationType.IDENTITY)` para autoincrement cuando la base lo soporte (H2/MySQL).
- En local, asegúrate de que `services.cliente-service.url` apunte al puerto correcto (por ejemplo `http://localhost:8080`) para evitar errores de conexión (502).

Siguientes pasos recomendados

- Añadir tests de integración que mocdeen `cliente-service` y `producto-service`.
- Crear un `Dockerfile` y pipeline CI que ejecute pruebas unitarias e integración.
- Añadir resiliencia (Resilience4j / retries / timeouts) en `ClienteServiceClient` para tolerar fallos temporales.

---

Documento generado para complementar la documentación principal del repositorio y facilitar pruebas del nuevo microservicio `pedido`.
