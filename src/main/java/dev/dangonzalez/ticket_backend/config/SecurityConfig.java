package dev.dangonzalez.ticket_backend.config;

import dev.dangonzalez.ticket_backend.security.JwtAccessDeniedHandler;
import dev.dangonzalez.ticket_backend.security.JwtAuthenticationEntryPoint;
import dev.dangonzalez.ticket_backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

// @Configuration: esta clase es una "fábrica de beans", como un módulo de Nest
// que en vez de @Module({ providers: [...] }) usa métodos @Bean para registrar cosas
// en el contenedor de Spring (equivalente al contenedor de DI de Nest).
// @EnableWebSecurity activa la cadena de filtros de Spring Security, que es
// conceptualmente parecida al pipeline de Guards + Middlewares de Nest, pero
// se ejecuta ANTES de llegar al DispatcherServlet (el "router" de Spring MVC).
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // Lombok genera un constructor con todos los campos "final" de abajo.
                          // Es el equivalente a que Nest inyecte por constructor automáticamente
                          // cuando declaras `constructor(private readonly x: X)`.
public class SecurityConfig {

    // Estos 3 son beans propios (@Component en sus clases), Spring los inyecta aquí
    // igual que Nest inyectaría providers en el constructor de un módulo.
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    // @Bean = "registra en el contenedor el objeto que devuelve este método,
    // identificado por el tipo de retorno (PasswordEncoder)". Cualquier otra clase
    // puede pedir un PasswordEncoder por constructor y Spring le da esta instancia.
    // BCrypt es el algoritmo de hashing de passwords (como bcryptjs en Node).
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager es el componente de Spring Security que sabe "validar
    // credenciales" (email + password contra lo guardado en BD). Se usa en AuthService
    // para el login, similar a como usarías un AuthGuard/estrategia local en Nest+Passport.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // Este bean define la cadena de filtros HTTP: qué rutas son públicas, cómo se manejan
    // sesiones, qué pasa si falla la autenticación/autorización, etc.
    // Es el equivalente más cercano a configurar Guards globales + un middleware de auth en Nest.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF protege formularios con sesión/cookies. Como esta API es stateless
                // y usa JWT en el header (no cookies de sesión), se desactiva -no aplica-.
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // STATELESS = Spring NO crea ni usa HttpSession. Cada request debe traer
                // su propio JWT. Es el mismo espíritu que un backend Nest con JWT en vez
                // de express-session: no hay estado guardado en el server entre requests.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Reglas de autorización por ruta, muy parecido a aplicar un @UseGuards(AuthGuard)
                // a nivel global y luego "whitelistear" rutas públicas.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**", "/swagger-ui/**").permitAll() // públicas, sin token
                        .anyRequest().authenticated()) // todo lo demás exige JWT válido
                // Qué responder cuando falla auth (401) o cuando el usuario no tiene permiso (403).
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint) // 401
                        .accessDeniedHandler(jwtAccessDeniedHandler)) // 403
                // Inserta nuestro filtro JWT ANTES del filtro estándar de login por formulario.
                // Es como registrar un middleware que corre antes que todo lo demás en Express/Nest.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Configuración de CORS, igual que app.enableCors({...}) en Nest/Express.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200")); // origen del front Angular
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // aplica a todas las rutas
        return source;
    }
}
