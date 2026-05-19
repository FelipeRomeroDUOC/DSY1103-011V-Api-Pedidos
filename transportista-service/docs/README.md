# Transportista Service

Este microservicio es responsable de la gestión de proveedores de transporte (transportistas) habilitados para realizar despachos regionales. Provee operaciones de CRUD y mantiene un registro del estado (activo/inactivo) y la cobertura regional de cada proveedor.

## 🚀 Responsabilidades
- Mantener el catálogo de transportistas externos.
- Proveer un endpoint de consulta para la integración con `despacho-service` al registrar despachos tipo `REGION`.
- Realizar desactivación lógica de proveedores sin eliminar sus registros (Soft Delete/Toggle).

## 📂 Documentación Técnica

Para conocer más detalles sobre la arquitectura, configuración, endpoints y la base de datos de este servicio, consulta los siguientes documentos:

- 📖 [Referencia de la API](API.md): Endpoints, ejemplos de request/response y códigos HTTP.
- 🏗️ [Arquitectura](ARQUITECTURA.md): Patrones de diseño, validaciones y manejo de excepciones.
- 💾 [Modelo de Datos](MODELO_DATOS.md): Esquema de tablas y entidades JPA.
- ⚙️ [Configuración](CONFIGURACION.md): Variables de entorno, perfiles y base de datos.
