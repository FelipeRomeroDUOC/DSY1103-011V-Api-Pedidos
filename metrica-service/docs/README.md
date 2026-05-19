# metrica-service

> Módulo del monorepo **Api Pedidos** · Puerto `8087`

## Descripción

`metrica-service` funciona como el cerebro analítico (BI) del ecosistema. En lugar de ser dueño de entidades de negocio fuertes, actúa como un **Agregador (Aggregator Pattern)**: consulta datos distribuidos a través de `pedido-service` y `cliente-service` en tiempo real vía Feign, ejecuta cálculos de negocio en memoria y entrega indicadores comerciales.

Sirve para responder preguntas del área comercial como: _"¿Quién es el mejor cliente?"_, _"¿Cuáles son los productos más vendidos este mes?"_ o _"¿Cuál es el volumen total de ventas de la semana?"_.

## Stack Tecnológico

| Tecnología | Versión / Detalle |
|---|---|
| Java | 21 |
| Spring Boot | 3.4.4 |
| Spring Web | REST Controllers |
| Spring Data JPA + Hibernate | Persistencia de Snapshots temporales |
| Lombok | `@Getter`, `@Setter`, `@RequiredArgsConstructor` |
| Spring Cloud OpenFeign + OkHttp | Consultas REST HTTP hacia otros microservicios |
| H2 Database | Base de datos local en archivo (`metrica_service.mv.db`) |

## Estructura de Paquetes

```
metrica-service/
└── src/main/java/cl/apipedidos/
    ├── MetricaServiceApplication.java
    ├── config/
    │   └── ApiExceptionHandler.java          ← Centraliza errores
    └── metrica/
        ├── controller/
        │   └── MetricaController.java        ← Endpoints analíticos
        ├── service/
        │   ├── MetricaService.java
        │   └── MetricaServiceImpl.java       ← Lógica de streams, map-reduce y matemática
        ├── repository/
        │   ├── MetricaClienteRepository.java ← Persiste snapshots cacheados
        │   └── MetricaProductoRepository.java
        ├── entity/
        │   ├── MetricaCliente.java           
        │   └── MetricaProducto.java          
        ├── client/
        │   ├── PedidoFeignClient.java        ← Conexión a puerto 8081
        │   └── ClienteFeignClient.java       ← Conexión a puerto 8082
        └── dto/
            └── *ResponseDTO.java             ← Cargas de red y DTOs de lectura
```

## Ejecución

```bash
# Desde la raíz del monorepo
./mvnw spring-boot:run -pl metrica-service

# O con el script de arranque general en Windows
start-all.bat
```

> **Nota importante de puertos:** Originalmente la especificación sugería el puerto `8086`, sin embargo, este se encontraba en uso por `fabricacion-service`. Para prevenir conflictos TCP, este servicio opera oficialmente en el puerto `8087`.
