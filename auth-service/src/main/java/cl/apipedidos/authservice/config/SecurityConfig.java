package cl.apipedidos.authservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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

                // H2 Console (solo desarrollo)
                .requestMatchers("/h2-console/**").permitAll()

                // Gestión de usuarios: solo ADMIN
                .requestMatchers(HttpMethod.GET,  "/api/auth/usuarios").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/auth/usuarios").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,  "/api/auth/usuarios/**").hasRole("ADMIN")

                // Logout: cualquier usuario autenticado
                .requestMatchers(HttpMethod.POST, "/api/auth/logout").authenticated()

                // Todo lo demás requiere autenticación
                .anyRequest().authenticated()
            )
            // Permitir H2 Console en iframe
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            // Registrar JwtAuthFilter ANTES del filtro estándar de usuario/contraseña
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();   // cost factor 10 por defecto
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
