# AI Agent Instructions - Api Pedidos

This document provides guidance for AI agents working on the `Api Pedidos` project.

## 📌 Project Overview
`Api Pedidos` is a Java 21 Spring Boot REST API focused on managing clients and Chile's territorial divisions (Regions and Communes). It includes a Swing GUI for user registration.

## 🛠 Tech Stack
- **Language:** Java 21
- **Framework:** Spring Boot
- **Persistence:** Spring Data JPA, Hibernate
- **Database:** H2 (local development), MySQL, PostgreSQL (alternatives)
- **Utilities:** Lombok, Bean Validation, Jackson
- **GUI:** Java Swing

## 🏗 Architecture & Patterns
- **Package Structure:** Root package is `cl.apipedidos`.
- **Layering:** Follows a standard Controller -> Service -> Repository pattern.
- **DTOs vs Entities:** 
    - Entities (in `.entity` packages) are used for database mapping.
    - DTOs (in `.dto` packages) are used for API requests and responses to decouple the internal model from the external API.
- **Error Handling:** Centralized via `cl.apipedidos.config.ApiExceptionHandler` using `@RestControllerAdvice`. It returns a standardized `ApiErrorResponse`.
- **GUI Interaction:** The Swing GUI (`RegistroUsuarios`) interacts with the backend via HTTP clients (`ClienteApiClient`, `UbicacionApiClient`) rather than direct service calls, simulating a decoupled frontend/backend architecture.
- **Data Initialization:** Uses `DataLoader` classes (e.g., `ClienteDataLoader`, `UbicacionDataLoader`) to populate the database on startup.

## 📋 Conventions & Guidelines
- **Naming:** Follow standard Java camelCase and Spring Boot naming conventions.
- **Validation:** Use Bean Validation annotations in DTOs for input validation.
- **API Responses:** Use `ResponseEntity` in controllers.
- **Chilean Location Model:** The project uses a specific model for Chile's regions and communes. Ensure any changes to location logic maintain consistency with the data in `src/main/resources/data/chile-divisiones-territoriales.json`.

## 🚀 Development Workflow
- **Build/Run:** 
    - Run application: `./mvnw spring-boot:run`
    - Compile only: `./mvnw -q -DskipTests compile`
- **Database Profiles:** Use `application-h2.properties`, `application-mysql.properties`, or `application-supabase.properties` depending on the environment.
- **Documentation:** Refer to the `docs/` folder for entity and API specifications.

## 🔗 Key References
- [README.md](./README.md) - General project overview and setup.
- [Client Entity Documentation](docs/entidad_cliente.md) - Details on the Client entity.
- [HTTP Client Documentation](docs/http_client.md) - Details on the HTTP client implementation.
- [User Registration Documentation](docs/registro_usuarios.md) - Details on the registration process.
