# Modelo de Datos — log-service

## Diagrama Entidad-Relacion

```text
┌──────────────────────────────────────────┐
│                logs                     │
├──────────────────────────────────────────┤
│ PK id                BIGINT             │
│    servicio          VARCHAR            │
│    operacion         VARCHAR            │
│    usuario_id        VARCHAR            │
│    timestamp         TIMESTAMP          │
│    resultado         VARCHAR            │
│    detalle           VARCHAR(1000)      │
└──────────────────────────────────────────┘
```

## Entidad `LogEntrada`

| Campo | Tipo | Columna | Nullable | Notas |
|---|---|---|---|---|
| `id` | `Long` | `id` | No | PK, IDENTITY |
| `servicio` | `String` | `servicio` | No | `@NotBlank` |
| `operacion` | `String` | `operacion` | No | `@NotBlank` |
| `usuarioId` | `String` | `usuario_id` | Si | Opcional |
| `timestamp` | `LocalDateTime` | `timestamp` | No | Asignado en servicio |
| `resultado` | `String` | `resultado` | No | `@NotBlank` |
| `detalle` | `String` | `detalle` | Si | Max 1000 caracteres |

## Ciclo de Vida

- `@PrePersist`: normaliza textos y asegura `timestamp` si aun no fue seteado.
- `@PreUpdate`: normaliza textos antes de actualizar.

## Repositorio

| Metodo | Descripcion |
|---|---|
| `findByServicio(String)` | Lista logs por servicio |
| `findByTimestampAfter(LocalDateTime)` | Lista logs desde una fecha |
| `findByServicioAndTimestampAfter(String, LocalDateTime)` | Lista logs filtrados por ambos criterios |
