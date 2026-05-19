# API Reference — metrica-service

> Base URL: `http://localhost:8087`

Todos los endpoints exponen respuestas protegidas por el envoltorio genérico `ApiResponse`.

---

## 1. Métricas Individuales de Cliente — `GET /api/metricas/clientes/{id}`

Calcula la frecuencia de compra anual, la suma total pagada históricamente y la cantidad neta de pedidos de un cliente. Si el cliente existe pero nunca ha comprado, los campos retornan valor `0`.

**Ejemplo:**
```bash
curl http://localhost:8087/api/metricas/clientes/1
```

**Respuesta (`200 OK`):**
```json
{
  "mensaje": "Métricas del cliente obtenidas correctamente",
  "data": {
    "clienteId": 1,
    "nombreCliente": "Juan Pérez",
    "montoTotal": 299980.0,
    "cantidadPedidos": 2,
    "frecuenciaAnual": 2.0
  },
  "exitoso": true,
  "timestamp": "2026-05-19T01:00:00"
}
```

---

## 2. Ranking de Mejores Clientes — `GET /api/metricas/clientes/ranking`

Cruza el historial total de `pedido-service`, lo agrupa por cliente, calcula la sumatoria monetaria y lo ordena de mayor a menor.

**Query Params:**
- `limite` (Opcional, default `10`): Número máximo de clientes en el top.

**Ejemplo:**
```bash
curl http://localhost:8087/api/metricas/clientes/ranking?limite=3
```

**Respuesta (`200 OK`):**
```json
{
  "mensaje": "Ranking obtenido correctamente",
  "data": [
    {
      "clienteId": 4,
      "nombreCliente": "Empresa Alfa",
      "montoTotal": 1500000.0,
      "cantidadPedidos": 10,
      "frecuenciaAnual": 0.0
    },
    ...
  ],
  "exitoso": true,
  "timestamp": "2026-05-19T01:00:00"
}
```

---

## 3. Productos Más Vendidos — `GET /api/metricas/productos/top`

Calcula la cantidad total vendida por cada ID de producto dentro de un rango de tiempo y entrega los N artículos líderes.

**Query Params:**
- `desde` (Opcional, `LocalDate`): Por defecto es el día 1 del mes actual.
- `hasta` (Opcional, `LocalDate`): Por defecto es el día de hoy.
- `limite` (Opcional, default `10`).

**Ejemplo:**
```bash
curl "http://localhost:8087/api/metricas/productos/top?desde=2026-05-01&hasta=2026-05-31"
```

---

## 4. Resumen Global de Ventas — `GET /api/metricas/ventas`

Genera un informe rápido del balance de un lapso determinado (Ingresos brutos y caudal de pedidos).

**Query Params:**
- `desde` (Opcional, `LocalDate`)
- `hasta` (Opcional, `LocalDate`)

**Ejemplo:**
```bash
curl http://localhost:8087/api/metricas/ventas
```

**Respuesta (`200 OK`):**
```json
{
  "mensaje": "Resumen de ventas obtenido",
  "data": {
    "desde": "2026-05-01",
    "hasta": "2026-05-19",
    "montoTotal": 3500000.0,
    "cantidadPedidos": 45
  },
  "exitoso": true,
  "timestamp": "2026-05-19T01:00:00"
}
```

---

## 5. Healthcheck — `GET /api/metricas/ping`

```json
{
  "mensaje": "metrica-service activo",
  "data": null,
  "exitoso": true,
  "timestamp": "2026-05-19T01:00:00"
}
```
