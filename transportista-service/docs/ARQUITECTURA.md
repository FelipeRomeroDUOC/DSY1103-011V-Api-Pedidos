# Arquitectura: Transportista Service

Este microservicio sigue un patrón de arquitectura en capas (Layered Architecture) tradicional de Spring Boot, enfocándose en la separación de responsabilidades y la validación robusta.

## Capas del Servicio

1. **Controller (`TransportistaController`)**: Expone la API REST. Responsable de recibir las peticiones, interceptar las validaciones básicas de `jakarta.validation` y empaquetar las respuestas utilizando la clase genérica `ApiResponse`.
2. **Service (`TransportistaService / TransportistaServiceImpl`)**: Contiene toda la lógica de negocio.
   - Verifica unicidad de códigos (`codigoInterno`) antes de inserciones.
   - Maneja los *soft-updates* (cambio de atributos y toggle del campo `activo`).
   - Lanza excepciones (`ResponseStatusException` o `IllegalArgumentException`) que posteriormente son atrapadas.
3. **Repository (`TransportistaRepository`)**: Capa de persistencia (Spring Data JPA). Extiende `JpaRepository` para operaciones CRUD por defecto e incorpora métodos de consulta personalizados como `findByActivoTrue()` y `existsByCodigoInternoIgnoreCase()`.

## Manejo de Errores Global

Se implementa una clase `@RestControllerAdvice` (`ApiExceptionHandler`) que captura transversalmente:
- `MethodArgumentNotValidException`: Captura errores en los DTOs (ej. strings demasiado largos o campos nulos) devolviendo un código `400 Bad Request`.
- `ResponseStatusException`: Captura excepciones de negocio lanzadas desde el Service, estandarizando la respuesta de error a formato JSON.
- `Exception`: Respaldo genérico para errores `500 Internal Server Error`.

## Integración
Este servicio actúa como **productor de información** (Upstream). Es consumido sincrónicamente por `despacho-service` mediante Spring Cloud OpenFeign al momento de autorizar un despacho regional.
