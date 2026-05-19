# Configuración — metrica-service

## application.properties

| Propiedad | Valor Base | Rol |
|---|---|---|
| `spring.application.name` | `metrica-service` | Identificador del microservicio. |
| `server.port` | `8087` | **Corrección**: Desplazado al `8087` en lugar del `8086` para eludir la superposición con `fabricacion-service`. |
| `pedido.service.url` | `${PEDIDO_SERVICE_URL:http://localhost:8081}` | Puntero dinámico a Pedidos. |
| `cliente.service.url` | `${CLIENTE_SERVICE_URL:http://localhost:8082}` | Puntero dinámico a Clientes. |
| `spring.cloud.openfeign.okhttp.enabled` | `true` | Motor HTTP subyacente para conexiones más sólidas y rápidas. |

## application-h2.properties

| Propiedad | Valor | Rol |
|---|---|---|
| `spring.datasource.url` | `jdbc:h2:file:./data/metrica_service;AUTO_SERVER=TRUE` | Base de datos empaquetada localmente para los Snapshots de BI. |
| `spring.jpa.hibernate.ddl-auto` | `update` | Creación y actualización automática del esquema. |
| `spring.h2.console.enabled` | `true` | Interfaz gráfica activa en `/h2-console`. |
