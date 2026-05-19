# Arquitectura — metrica-service

## Diagrama de Interconexión

```
┌─────────────────────────────────┐
│         metrica-service         │
│          (Agregador)            │
│                                 │
│  ┌─> ClienteFeignClient ────────┼── GET ──► cliente-service (8082)
│  │                              │           (Obtiene nombre para mostrar)
│  │                              │
│  └─> PedidoFeignClient  ────────┼── GET ──► pedido-service (8081)
│                                 │           (Descarga lista de pedidos para reducir)
└─────────────────────────────────┘
```

## Patrones Aplicados

### 1. API Composition / Aggregator
`metrica-service` expone APIs complejas (como rankings) que requieren leer datos esparcidos en múltiples bases de datos. Delega la extracción a Feign, los junta en RAM, ejecuta operaciones matemáticas complejas (como streams con `Collectors.groupingBy`) y devuelve el cruce.

### 2. Tolerancia a fallos parcial (Graceful Degradation)
Al renderizar el Ranking de Clientes, si `cliente-service` colapsa, el ranking no falla con un error HTTP `500`. En cambio, el servicio silencia (suprime) el error Feign y asigna un "Cliente Desconocido" como nombre, pero manteniendo los cálculos de montos intactos (que provienen del otro microservicio).

### 3. Caching Opcional con Snapshotting
Aunque el proceso opera *on-the-fly* (en memoria), los resultados finales del ranking o del análisis se guardan pasivamente en JPA (`MetricaClienteRepository`, `MetricaProductoRepository`) para que en un futuro se puedan cronometrar tareas pesadas y servir el caché.
