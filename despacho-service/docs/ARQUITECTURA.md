# Arquitectura — despacho-service

## Diagrama de Capas y Red

```
┌────────────────────────────────────────────────────────┐
│                      REST Layer                        │
│                 DespachoController                     │
│  (POST, GET, PUT /api/despachos)                       │
├────────────────────────────────────────────────────────┤
│                     Service Layer                      │
│     DespachoService (interfaz)                         │
│     DespachoServiceImpl (lógica y validaciones)        │
│          │                                             │
│          │ [Feign HTTP GET /api/pedidos/{id}]          │
│          └──► PedidoFeignClient ───────────────────────┼──► pedido-service (8081)
├────────────────────────────────────────────────────────┤
│                   Repository Layer                     │
│               DespachoRepository (JPA)                 │
├────────────────────────────────────────────────────────┤
│                   Persistence Layer                    │
│                 Despacho (Entity)                      │
│               H2 Database (file-based)                 │
└────────────────────────────────────────────────────────┘
```

## Flujo de Validación Inter-Servicios (Registro de Despacho)

1. **Recepción:** `DespachoController` recibe un `POST` con un `pedidoId` de `33`.
2. **Conflicto Local:** `DespachoRepository.existsByPedidoId(33)` verifica localmente si ya se hizo un despacho para ese pedido. Si es afirmativo, lanza `409 Conflict`.
3. **Consistencia de Datos:** El servicio evalúa si `tipoDespacho` es `REGION`. Si lo es y no viene `transportista`, arroja `400 Bad Request`.
4. **Verificación Externa (Feign):**
   * El servicio llama a `PedidoFeignClient.obtenerPedido(33)`.
   * Si OpenFeign recibe un `404 Not Found` desde el `pedido-service`, propaga un `404 Not Found`.
   * Si obtiene el pedido, evalúa el campo `estado`. Si el estado es distinto a `"LISTO"` (ej. `"PENDIENTE"`), aborta y retorna un `422 Unprocessable Entity`.
5. **Persistencia:** Si todo es válido, el servicio persiste la entidad y retorna un `201 Created`.

## Patrones Aplicados

| Patrón | Implementación en el Módulo |
|---|---|
| **Controller → Service → Repository** | División estructural estándar. |
| **API Gateway / Proxy** | Uso intensivo de `OpenFeign` para llamadas sincrónicas de backend. |
| **Fail-Fast Validation** | Validaciones estáticas (`@NotNull`) previas a ejecutar llamadas HTTP que son computacionalmente más costosas. |
| **DTO Pattern** | Separación del modelo interno (`Despacho`) de la entrada/salida web (`DespachoRequestDTO`, `DespachoResponseDTO`, `ApiResponse`). |
| **Global Exception Handling** | Clase centralizada `ApiExceptionHandler` interceptando todas las variantes de error para encapsularlas en la misma respuesta unificada que el éxito. |
