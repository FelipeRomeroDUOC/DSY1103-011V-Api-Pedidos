# Auth Service

> Microservicio de Autenticación y Autorización — parte del ecosistema **Api Pedidos**.

## Descripción General

`auth-service` es el microservicio responsable de emitir tokens JWT (JSON Web Tokens) y administrar los usuarios del sistema. Todos los demás microservicios confían en los tokens firmados por este servicio para conceder acceso a los endpoints protegidos, actuando como el pilar de la seguridad *stateless* del ecosistema.

### Responsabilidades Principales

| Responsabilidad | Descripción |
|---|---|
| **Login** | Valida credenciales de usuario y retorna un token JWT firmado. |
| **Gestión de Usuarios** | Permite a un administrador crear y consultar usuarios del sistema. |
| **Generación de JWT** | Crea tokens con Claims específicos (ej. Email y Rol del usuario). |
| **Encriptación de Passwords** | Almacena contraseñas de forma segura utilizando BCrypt. |

## Stack Tecnológico

| Tecnología | Versión / Detalle |
|---|---|
| Java | 21+ |
| Spring Boot | 3.4.4 |
| Spring Security | Autenticación y cifrado (BCrypt) |
| JJWT (io.jsonwebtoken) | 0.12.6 - Para generación y firma de tokens JWT |
| Spring Data JPA | Hibernate como ORM |
| H2 Database | Base de datos embebida (desarrollo) |

## Puerto por Defecto

```text
http://localhost:8090
```

## Inicio Rápido

```bash
./mvnw spring-boot:run -pl auth-service
```

## Estructura del Módulo

```text
auth-service/
├── pom.xml
├── data/                            ← Base H2 en archivo
└── src/main/
    ├── java/cl/apipedidos/authservice/
    │   ├── AuthServiceApplication.java
    │   ├── config/
    │   │   ├── CustomUserDetailsService.java ← Carga usuarios para Spring Security
    │   │   ├── DataInitializer.java          ← Carga 4 usuarios de prueba por defecto
    │   │   ├── JwtAuthFilter.java            ← Filtro JWT (usado internamente y en otros ms)
    │   │   ├── JwtUtil.java                  ← Utilidad de generación y validación
    │   │   └── SecurityConfig.java           ← Configura endpoints públicos/privados
    │   ├── controller/
    │   │   ├── AuthController.java           ← Login y validaciones públicas
    │   │   └── UsuarioController.java        ← CRUD de Usuarios (solo ADMIN)
    │   ├── dto/
    │   │   ├── LoginRequestDTO.java
    │   │   ├── LoginResponseDTO.java
    │   │   ├── UsuarioRequestDTO.java
    │   │   └── UsuarioResponseDTO.java
    │   ├── entity/
    │   │   └── Usuario.java
    │   ├── repository/
    │   │   └── UsuarioRepository.java
    │   └── service/
    │       ├── AuthService.java              ← Lógica de negocio del Login
    │       └── UsuarioService.java           ← Lógica de negocio de Usuarios
    └── resources/
        └── application.properties
```

## Documentación Detallada

| Documento | Descripción |
|---|---|
| [API.md](API.md) | Referencia completa de endpoints REST |
| [ARQUITECTURA.md](ARQUITECTURA.md) | Arquitectura interna de seguridad |
| [MODELO_DATOS.md](MODELO_DATOS.md) | Entidad Usuario y base de datos |
| [CONFIGURACION.md](CONFIGURACION.md) | Propiedades y la clave secreta JWT |

---

*Proyecto académico — Arquitectura Fullstack, DuocUC.*
