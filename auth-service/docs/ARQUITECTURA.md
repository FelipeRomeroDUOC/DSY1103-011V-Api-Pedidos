# Arquitectura - Auth Service

## Patrón Arquitectónico

`auth-service` implementa un modelo **Stateless basado en JWT**, separando por completo el estado de sesión de los servidores. Ningún microservicio, incluyendo el `auth-service`, guarda la sesión HTTP tradicional en memoria.

### Diagrama de Flujo

1. **Autenticación (Login):**
   El usuario envía sus credenciales a `/api/auth/login`. El `auth-service` compara el password encriptado con BCrypt contra la base de datos. Si es válido, emite un token JWT que contiene el Email y Rol en su payload y lo firma con el secreto simétrico (`HS256`).

2. **Validación (Otros servicios):**
   El cliente (Postman o el Frontend) envía ese Token al `cliente-service` en la cabecera `Authorization`. El `cliente-service` no necesita contactar al `auth-service` para validarlo; simplemente verifica la **Firma Matemática** del token utilizando el mismo secreto (`jwt.secret`) inyectado por propiedades.

## Componentes Internos Clave

- `SecurityConfig`: Configura la ruta pública para Login y Ping, desactivando el control CSRF y aplicando la política de sesión `STATELESS`.
- `JwtUtil`: Centraliza la generación (`Jwts.builder()`) y validación (`Jwts.parser()`) del token.
- `JwtAuthFilter`: Extrae el token por cada Request, valida la firma e inyecta el `SecurityContext` al hilo local, permitiendo el uso de anotaciones como `@PreAuthorize`.
- `CustomUserDetailsService`: Capa intermedia requerida por el `AuthenticationManager` de Spring Security para buscar el usuario en la tabla de la base de datos a partir del email durante la fase de Login.
