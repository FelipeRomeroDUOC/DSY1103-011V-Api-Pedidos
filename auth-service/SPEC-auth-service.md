# SPEC — `auth-service`

> DSY1103 · Desarrollo FullStack 1 · Proyecto Semestral — Arquitectura de Microservicios  
> Servicio: **auth-service** · Puerto: `8090` · BD: `db_auth`  
> Seguridad: **Spring Security + JWT (Bearer Token) + BCrypt**

---

## 1. Propósito

`auth-service` gestiona la autenticación y autorización de todos los usuarios del sistema. El usuario se autentica una sola vez en `POST /api/auth/login` enviando su email y contraseña, y recibe un **token JWT firmado** que usa en todos los requests siguientes. Las contraseñas se almacenan como hashes **BCrypt**.

El resto de microservicios del sistema reciben el token JWT en el header `Authorization: Bearer ...` y lo validan localmente (verificando firma y expiración) sin consultar la base de datos ni llamar a `auth-service` en cada request.

---

## 2. Posición en la arquitectura

```
Cliente / Frontend
        │
        │  POST /api/auth/login  →  { email, password }
        ▼
auth-service (8090) ──► db_auth (H2)
        │
        │  ← devuelve { token: "eyJhbGci..." }
        │
        │  Los demás servicios reciben:
        │  Authorization: Bearer eyJhbGci...
        │  y validan el token localmente con la misma jwt.secret
        ▼
pedido-service / cliente-service / fabricacion-service / ...
```

- `auth-service` es el **único emisor** de tokens JWT del sistema.
- Los demás servicios **no llaman** a `auth-service` en cada request: validan el token por sí mismos usando la clave secreta compartida (`jwt.secret` en `application.properties`).
- `auth-service` **no llama a ningún otro servicio** (sin clientes Feign salientes).

---

## 3. Stack tecnológico

| Elemento | Valor |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.4.x |
| Seguridad | `spring-boot-starter-security` |
| Autenticación | JWT Bearer Token (`JwtAuthFilter` + `OncePerRequestFilter`) |
| Librería JWT | `io.jsonwebtoken:jjwt` (jjwt-api + jjwt-impl + jjwt-jackson) |
| Cifrado | `BCryptPasswordEncoder` (cost factor 10) |
| Sesión | `SessionCreationPolicy.STATELESS` |
| Autorización | `SecurityConfig` + `@PreAuthorize` + `@EnableMethodSecurity` |
| Persistencia | Spring Data JPA + Hibernate |
| Base de datos (dev) | H2 en archivo (`auth-service/data/auth_service.mv.db`) |
| Lombok | `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@RequiredArgsConstructor` |
| Validación | Bean Validation (`jakarta.validation`) |
| Módulo Maven | `auth-service` (hijo del POM padre raíz) |

---

## 4. Estructura de carpetas

```
auth-service/
├── pom.xml
└── src/
    └── main/
        ├── java/com/duoc/auth_service/
        │   ├── AuthServiceApplication.java
        │   ├── config/
        │   │   ├── SecurityConfig.java              ← SecurityFilterChain + PasswordEncoder
        │   │   ├── CustomUserDetailsService.java    ← puente Spring Security ↔ UserRepository
        │   │   ├── JwtUtil.java                     ← genera y valida tokens JWT
        │   │   └── JwtAuthFilter.java               ← filtro que intercepta Authorization: Bearer
        │   ├── controller/
        │   │   ├── AuthController.java              ← login y logout
        │   │   └── UsuarioController.java           ← CRUD de usuarios
        │   ├── dto/
        │   │   ├── LoginRequestDTO.java
        │   │   ├── LoginResponseDTO.java            ← contiene el token JWT
        │   │   ├── UsuarioRequestDTO.java
        │   │   ├── UsuarioResponseDTO.java
        │   │   └── ApiResponse.java                 ← wrapper genérico del proyecto
        │   ├── entity/
        │   │   └── Usuario.java
        │   ├── repository/
        │   │   └── UsuarioRepository.java
        │   └── service/
        │       ├── AuthService.java                 ← interfaz
        │       ├── AuthServiceImpl.java             ← lógica de login/logout
        │       ├── UsuarioService.java              ← interfaz
        │       └── UsuarioServiceImpl.java
        └── resources/
            ├── application.properties
            └── application-h2.properties
```

---

## 5. Entidad JPA — `Usuario`

```java
@Entity
@Table(name = "usuarios")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String password;        // hash BCrypt — nunca texto plano

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Rol rol = Rol.ENCARGADO_PEDIDOS;

    @Column(nullable = false)
    private boolean activo = true;  // false → 401 aunque el token sea válido

    public enum Rol {
        ADMIN,
        ENCARGADO_PEDIDOS,
        ENCARGADO_DESPACHO,
        COMERCIAL
    }
}
```

**Puntos clave:**
- `@Column(name = "password_hash")` respeta la nomenclatura del levantamiento en BD; el campo Java se llama `password` para compatibilidad con Spring Security.
- `activo = false` desactiva al usuario sin eliminarlo. El `JwtAuthFilter` debe verificar este campo al validar el token.
- `@Enumerated(EnumType.STRING)` guarda el nombre del enum como texto.

---

## 6. DTOs

### `LoginRequestDTO`
```java
public class LoginRequestDTO {
    @NotBlank @Email String email;
    @NotBlank         String password;   // contraseña en texto plano
}
```

### `LoginResponseDTO`
```java
public class LoginResponseDTO {
    String token;        // JWT firmado — el cliente lo guarda y lo envía en cada request
    String tipo;         // siempre "Bearer"
    String email;
    String rol;
    long   expiracion;   // timestamp Unix de expiración
}
```

### `UsuarioRequestDTO`
```java
public class UsuarioRequestDTO {
    @NotBlank           String nombre;
    @NotBlank @Email    String email;
    @NotBlank           String password;  // texto plano → se hashea en el servicio
    Usuario.Rol         rol;              // opcional; default = ENCARGADO_PEDIDOS
    Boolean             activo;           // opcional; default = true
}
```

### `UsuarioResponseDTO`
```java
public class UsuarioResponseDTO {
    Long    id;
    String  nombre;
    String  email;
    String  rol;
    boolean activo;
    // NUNCA incluir password ni hash
}
```

---

## 7. Wrapper de respuesta — `ApiResponse<T>`

Mismo patrón del proyecto:

```json
{
  "mensaje": "...",
  "data": { ... },
  "exitoso": true,
  "timestamp": "2025-05-19T12:00:00"
}
```

---

## 8. `JwtUtil`

Clase responsable de **generar** y **validar** tokens JWT. Es un `@Component` que se inyecta en `AuthServiceImpl` y `JwtAuthFilter`.

```java
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;    // ej: 86400000 = 24 horas

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    // Genera un token JWT firmado con HS256
    public String generarToken(String email, String rol) {
        return Jwts.builder()
            .subject(email)
            .claim("rol", rol)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expirationMs))
            .signWith(getKey())
            .compact();
    }

    // Extrae el email (subject) del token
    public String extraerEmail(String token) {
        return getClaims(token).getSubject();
    }

    // Extrae el rol del token
    public String extraerRol(String token) {
        return getClaims(token).get("rol", String.class);
    }

    // Verifica firma y expiración — lanza excepción si es inválido
    public boolean esValido(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
            .verifyWith(getKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
```

**Qué hace cada parte:**

| Método | Propósito |
|---|---|
| `generarToken(email, rol)` | Crea el JWT con subject=email, claim rol, fechas de emisión y expiración, firmado con HS256 |
| `extraerEmail(token)` | Lee el subject del payload para saber quién es el usuario |
| `extraerRol(token)` | Lee el claim `rol` para la autorización |
| `esValido(token)` | Verifica firma y expiración sin lanzar excepción al llamador |

---

## 9. `JwtAuthFilter`

Filtro que se ejecuta una vez por request (`OncePerRequestFilter`). Intercepta el header `Authorization: Bearer ...`, valida el token y pone el usuario autenticado en el `SecurityContext`.

```java
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil       jwtUtil;
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Si no hay header Bearer, continuar sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);   // quitar "Bearer "

        if (!jwtUtil.esValido(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtUtil.extraerEmail(token);
        String rol   = jwtUtil.extraerRol(token);

        // Verificar que el usuario sigue activo en la BD
        boolean activo = usuarioRepository.findByEmail(email)
            .map(Usuario::isActivo)
            .orElse(false);

        if (!activo) {
            filterChain.doFilter(request, response);
            return;
        }

        // Construir authorities a partir del rol del token
        List<SimpleGrantedAuthority> authorities =
            List.of(new SimpleGrantedAuthority("ROLE_" + rol));

        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(email, null, authorities);

        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }
}
```

**Flujo interno:**

```
Request llega con Authorization: Bearer eyJhbGci...
        │
        ▼
¿Header existe y empieza con "Bearer "?
  No → continuar sin autenticar (el AuthorizationFilter decidirá si es 401)
        │
        ▼
jwtUtil.esValido(token)
  No → continuar sin autenticar
        │
        ▼
Extraer email y rol del payload
        │
        ▼
usuarioRepository.findByEmail(email) → ¿activo?
  No → continuar sin autenticar
        │
        ▼
Crear UsernamePasswordAuthenticationToken con ROLE_<rol>
Guardar en SecurityContextHolder
        │
        ▼
filterChain.doFilter() → sigue al AuthorizationFilter → Controlador
```

---

## 10. `CustomUserDetailsService`

Sigue siendo necesario para que `DaoAuthenticationProvider` (usado en el endpoint de login) pueda validar las credenciales con BCrypt.

```java
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
            .filter(u -> u.getPassword() != null && !u.getPassword().isBlank())
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        return org.springframework.security.core.userdetails.User
            .withUsername(usuario.getEmail())
            .password(usuario.getPassword())
            .roles(usuario.getRol().name())   // Spring agrega ROLE_ automáticamente
            .disabled(!usuario.isActivo())
            .build();
    }
}
```

> **Importante:** `CustomUserDetailsService` solo se usa en el login (validación de credenciales). En los demás requests, la identidad viene del token JWT y la extrae `JwtAuthFilter` sin consultar la BD de nuevo.

---

## 11. `SecurityConfig`

Registra el `JwtAuthFilter` **antes** del `UsernamePasswordAuthenticationFilter`, deshabilita `httpBasic`, y define las reglas de autorización por endpoint.

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter              jwtAuthFilter;
    private final CustomUserDetailsService   userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos (no requieren token)
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/auth/ping").permitAll()

                // Gestión de usuarios: solo ADMIN
                .requestMatchers(HttpMethod.GET,  "/api/auth/usuarios").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/auth/usuarios").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,  "/api/auth/usuarios/**").hasRole("ADMIN")

                // Logout: cualquier usuario autenticado
                .requestMatchers(HttpMethod.POST, "/api/auth/logout").authenticated()

                // Todo lo demás requiere autenticación
                .anyRequest().authenticated()
            )
            // Registrar JwtAuthFilter ANTES del filtro estándar de usuario/contraseña
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();   // cost factor 10, nunca en CustomUserDetailsService
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}
```

**Diferencia clave respecto a HTTP Basic:**

| HTTP Basic (Lección 16) | JWT |
|---|---|
| `.httpBasic(Customizer.withDefaults())` | `.addFilterBefore(jwtAuthFilter, ...)` |
| Spring intercepta automáticamente `Authorization: Basic ...` | Tú escribes el filtro que lee `Authorization: Bearer ...` |
| Contraseña viaja en cada request | Token firmado viaja en cada request |
| Sin endpoint de login | `POST /api/auth/login` emite el token |

---

## 12. `AuthController` — endpoints de autenticación

Implementa los dos endpoints del levantamiento: `login` y `logout`.

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/login — público, no requiere token
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(
            new ApiResponse<>("Login exitoso", response, true));
    }

    // POST /api/auth/logout — requiere token válido
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // Con JWT stateless el logout es del lado del cliente (descartar el token).
        // El servidor confirma la recepción.
        return ResponseEntity.ok(
            new ApiResponse<>("Sesión cerrada. Descarta el token en el cliente.", null, true));
    }

    // GET /api/auth/ping — healthcheck público
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("auth-service OK");
    }
}
```

> **Nota sobre logout:** Con JWT stateless puro, el servidor no puede invalidar un token ya emitido (no hay sesión). El logout real ocurre en el cliente descartando el token. Si se necesita invalidación server-side (por seguridad), se puede implementar una blacklist en memoria o en BD, pero eso es una extensión futura fuera del alcance de este proyecto.

---

## 13. `AuthServiceImpl` — lógica de login

```java
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager     authenticationManager;
    private final JwtUtil                   jwtUtil;
    private final UsuarioRepository         usuarioRepository;

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        // 1. Delegar la validación de credenciales a Spring Security (BCrypt interno)
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );

        // Si llega aquí, las credenciales son correctas
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // 2. Obtener el rol real desde la BD
        String rol = usuarioRepository.findByEmail(userDetails.getUsername())
            .map(u -> u.getRol().name())
            .orElseThrow();

        // 3. Generar el token JWT
        String token = jwtUtil.generarToken(userDetails.getUsername(), rol);

        // 4. Construir y retornar la respuesta
        return LoginResponseDTO.builder()
            .token(token)
            .tipo("Bearer")
            .email(userDetails.getUsername())
            .rol(rol)
            .expiracion(System.currentTimeMillis() + /* inyectar expirationMs */ 86400000L)
            .build();
    }
}
```

**¿Qué hace `authenticationManager.authenticate(...)`?**

Internamente llama a `DaoAuthenticationProvider` → `CustomUserDetailsService.loadUserByUsername(email)` → `BCryptPasswordEncoder.matches(passwordPlano, hashBD)`. Si la contraseña no coincide o el usuario está inactivo, lanza `BadCredentialsException` y Spring Security devuelve `401` automáticamente.

---

## 14. `UsuarioController` — CRUD de usuarios

```java
@RestController
@RequestMapping("/api/auth/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // GET /api/auth/usuarios — solo ADMIN
    @GetMapping
    public ResponseEntity<ApiResponse<List<UsuarioResponseDTO>>> listar() {
        return ResponseEntity.ok(
            new ApiResponse<>("Usuarios obtenidos", usuarioService.listar(), true));
    }

    // POST /api/auth/usuarios — solo ADMIN
    @PostMapping
    public ResponseEntity<ApiResponse<UsuarioResponseDTO>> crear(
            @Valid @RequestBody UsuarioRequestDTO request) {
        UsuarioResponseDTO creado = usuarioService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>("Usuario creado", creado, true));
    }

    // PUT /api/auth/usuarios/{id} — solo ADMIN
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponseDTO>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequestDTO request) {
        UsuarioResponseDTO actualizado = usuarioService.actualizar(id, request);
        return ResponseEntity.ok(
            new ApiResponse<>("Usuario actualizado", actualizado, true));
    }
}
```

---

## 15. Lógica de `UsuarioServiceImpl`

```
crear(UsuarioRequestDTO dto):
  1. existsByEmail(dto.email) → si true: lanzar excepción 409 Conflict
  2. passwordEncoder.encode(dto.password) → guardar hash, nunca texto plano
  3. Mapear DTO → entidad → usuarioRepository.save()
  4. Retornar UsuarioResponseDTO (sin password)

listar():
  1. usuarioRepository.findAll()
  2. Mapear lista → List<UsuarioResponseDTO>

actualizar(Long id, UsuarioRequestDTO dto):
  1. findById(id) → si vacío: lanzar excepción 404
  2. Si dto.password != null → hashear y actualizar
  3. Actualizar nombre, rol, activo si vienen en el DTO
  4. usuarioRepository.save() → retornar UsuarioResponseDTO
```

---

## 16. Repository — `UsuarioRepository`

```java
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

---

## 17. Inicialización de datos — `DataInitializer`

```java
@Component
@Profile("h2")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder   passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) return;

        List.of(
            crearUsuario("Administrador",    "admin@empresa.com",           "pass123", Usuario.Rol.ADMIN),
            crearUsuario("Ana García",       "ana.garcia@empresa.com",      "user123", Usuario.Rol.ENCARGADO_PEDIDOS),
            crearUsuario("Carlos López",     "carlos.lopez@empresa.com",    "user123", Usuario.Rol.ENCARGADO_DESPACHO),
            crearUsuario("María Fernández",  "maria.fernandez@empresa.com", "user123", Usuario.Rol.COMERCIAL)
        ).forEach(usuarioRepository::save);
    }

    private Usuario crearUsuario(String nombre, String email, String pass, Usuario.Rol rol) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(pass));   // BCrypt — nunca texto plano
        u.setRol(rol);
        u.setActivo(true);
        return u;
    }
}
```

**Credenciales de prueba:**

| Email | Contraseña | Rol |
|---|---|---|
| `admin@empresa.com` | `pass123` | ADMIN |
| `ana.garcia@empresa.com` | `user123` | ENCARGADO_PEDIDOS |
| `carlos.lopez@empresa.com` | `user123` | ENCARGADO_DESPACHO |
| `maria.fernandez@empresa.com` | `user123` | COMERCIAL |

---

## 18. Configuración

### `application.properties`
```properties
spring.application.name=auth-service
server.port=8090
spring.profiles.active=h2

# Clave secreta JWT — en producción usar variable de entorno, nunca commitear
jwt.secret=dHVjbGF2ZXNlY3JldGFkZWJlc2VybXV5bGFyZ2FwYXJhcXVlc2VhcnNlZ3VyYQ==
jwt.expiration-ms=86400000
```

> `jwt.secret` debe ser un string Base64 de al menos 32 bytes. El valor de ejemplo es solo para desarrollo local. En producción usar `${JWT_SECRET}` como variable de entorno.

### `application-h2.properties`
```properties
spring.datasource.url=jdbc:h2:file:./data/auth_service;AUTO_SERVER=TRUE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

---

## 19. `pom.xml` — dependencias

Módulo hijo del POM padre. Añadir en `<dependencies>`:

```xml
<!-- Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT (jjwt 0.12.x — compatible con Java 21 y Spring Boot 3.4.x) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>

<!-- JPA + H2 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>

<!-- Validación -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

Registrar módulo en `pom.xml` raíz:
```xml
<modules>
    <module>cliente-service</module>
    <module>pedido-service</module>
    <module>fabricacion-service</module>
    <module>log-service</module>
    <module>auth-service</module>   <!-- ← añadir -->
</modules>
```

---

## 20. Tabla de autorización de endpoints

| Endpoint | Método | Sin token | ENCARGADO_* / COMERCIAL | ADMIN |
|---|---|:---:|:---:|:---:|
| `/api/auth/login` | POST | ✅ 200 | ✅ 200 | ✅ 200 |
| `/api/auth/logout` | POST | ❌ 401 | ✅ 200 | ✅ 200 |
| `/api/auth/usuarios` | GET | ❌ 401 | ❌ 403 | ✅ 200 |
| `/api/auth/usuarios` | POST | ❌ 401 | ❌ 403 | ✅ 201 |
| `/api/auth/usuarios/{id}` | PUT | ❌ 401 | ❌ 403 | ✅ 200 |
| `/api/auth/ping` | GET | ✅ 200 | ✅ 200 | ✅ 200 |

---

## 21. Probar con curl

### Paso 1 — Login (obtener token)
```bash
curl -i -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@empresa.com","password":"pass123"}'
```
Respuesta esperada `200 OK`:
```json
{
  "mensaje": "Login exitoso",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tipo": "Bearer",
    "email": "admin@empresa.com",
    "rol": "ADMIN",
    "expiracion": 1716086400000
  },
  "exitoso": true
}
```

### Paso 2 — Usar el token en requests protegidos
```bash
# Guardar el token
TOKEN="eyJhbGciOiJIUzI1NiJ9..."

# Listar usuarios con ADMIN → 200
curl -i http://localhost:8090/api/auth/usuarios \
  -H "Authorization: Bearer $TOKEN"

# Listar usuarios sin token → 401
curl -i http://localhost:8090/api/auth/usuarios

# Listar usuarios con rol no-ADMIN → 403
TOKEN_ENC="<token de ana.garcia@empresa.com>"
curl -i http://localhost:8090/api/auth/usuarios \
  -H "Authorization: Bearer $TOKEN_ENC"

# Crear usuario con ADMIN → 201
curl -i -X POST http://localhost:8090/api/auth/usuarios \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"nombre":"Pedro Soto","email":"pedro@empresa.com","password":"clave456","rol":"ENCARGADO_PEDIDOS"}'

# Logout → 200
curl -i -X POST http://localhost:8090/api/auth/logout \
  -H "Authorization: Bearer $TOKEN"

# Login con contraseña incorrecta → 401
curl -i -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@empresa.com","password":"mala"}'

# Healthcheck público → 200
curl -i http://localhost:8090/api/auth/ping
```

---

## 22. Cómo levantar el servicio

```bash
./mvnw spring-boot:run -pl auth-service
```

Consola H2: `http://localhost:8090/h2-console`

Agregar al `start-all.bat`:
```bat
start "auth-service" cmd /k "mvnw spring-boot:run -pl auth-service"
```

---

## 23. Checklist de implementación

- [ ] Registrar `auth-service` como módulo Maven en `pom.xml` raíz
- [ ] Agregar dependencias `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (v0.12.6) en `pom.xml`
- [ ] Crear entidad `Usuario` con `password_hash` (columna BD), `Rol` (enum) y `activo`
- [ ] Crear `UsuarioRepository` con `findByEmail` y `existsByEmail`
- [ ] Crear `JwtUtil` con `generarToken`, `extraerEmail`, `extraerRol` y `esValido`
- [ ] Crear `JwtAuthFilter` extendiendo `OncePerRequestFilter`
- [ ] Crear `CustomUserDetailsService` implementando `UserDetailsService`
- [ ] Crear `SecurityConfig` con `addFilterBefore(jwtAuthFilter, ...)`, sin `httpBasic`
- [ ] Verificar que `PasswordEncoder` y `AuthenticationManager` son `@Bean` en `SecurityConfig`
- [ ] Crear `LoginRequestDTO` y `LoginResponseDTO` (con token, tipo, email, rol, expiración)
- [ ] Crear `UsuarioRequestDTO` y `UsuarioResponseDTO` (sin `password` en el response)
- [ ] Implementar `AuthServiceImpl.login()` usando `authenticationManager.authenticate()`
- [ ] Implementar `AuthController` con `POST /api/auth/login` y `POST /api/auth/logout`
- [ ] Implementar `UsuarioController` con los 3 endpoints del levantamiento
- [ ] Crear `DataInitializer` con `@Profile("h2")` usando `passwordEncoder.encode(...)`
- [ ] Agregar `jwt.secret` y `jwt.expiration-ms` en `application.properties`
- [ ] Verificar `POST /api/auth/login` con credenciales correctas → `200` + token
- [ ] Verificar `POST /api/auth/login` con credenciales incorrectas → `401`
- [ ] Verificar `GET /api/auth/usuarios` sin token → `401`
- [ ] Verificar `GET /api/auth/usuarios` con token ENCARGADO → `403`
- [ ] Verificar `GET /api/auth/usuarios` con token ADMIN → `200`
- [ ] Verificar que usuario con `activo = false` no puede hacer login → `401`
- [ ] Verificar `GET /api/auth/ping` sin token → `200`
- [ ] Agregar el servicio a `start-all.bat`
