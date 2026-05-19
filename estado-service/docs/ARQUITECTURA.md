# Arquitectura — estado-service

## Diagrama de Interconexión y Recepción

A diferencia de otros módulos, `estado-service` es el final de la cadena de trazabilidad.

```
┌──────────────────────────────┐              ┌─────────────────────────────────────┐
│       pedido-service         │              │           estado-service            │
│                              │              │                                     │
│ 1. Actualiza Estado en H2    │              │ 3. Recibe evento en POST /api/estados
│ 2. Ejecuta EstadoFeignClient ┼── POST ─────►│ 4. Genera Timestamp                 │
│    en bloque Try/Catch       │              │ 5. Inserta inmutable en H2          │
└──────────────────────────────┘              └─────────────────────────────────────┘
```

## Resiliencia Implementada

El diseño dicta que **la auditoría nunca debe romper el proceso de negocio original**.
Si `estado-service` está caído, `pedido-service` captura la `FeignException` en un bloque `catch`, escribe una alerta `WARN/ERROR` en consola informando que la auditoría falló, pero prosigue con el retorno de éxito de la orden principal. 

## Patrones Aplicados

| Patrón | Descripción en el Módulo |
|---|---|
| **Audit Log / Append-Only Pattern** | La estructura carece deliberadamente de endpoints transaccionales como `PUT` o `DELETE`. Una vez ingresada una fila de histórico, se considera evidencia inmutable. |
| **Loose Coupling Data** | `EstadoAnterior` y `EstadoNuevo` se guardan en la base de datos como campos de texto (varchar) en lugar de requerir una dependencia fuerte sobre las nomenclaturas de `pedido-service` o enums rígidos. |
| **Controller → Service → Repository** | División estructural estándar en Spring Boot. |
| **Global Exception Handling** | Captura automática de `MethodArgumentNotValidException` (por un payload incompleto desde Feign) y lo encapsula en un JSON amable tipo `ApiResponse`. |
