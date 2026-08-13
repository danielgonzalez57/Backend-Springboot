package dev.dangonzalez.ticket_backend.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Este es EL filtro que hace la magia de "leer el JWT del header y autenticar
// al usuario para este request". Se registra en SecurityConfig con
// .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class),
// así que corre en CADA petición, antes de llegar al controller.
//
// Es el equivalente conceptual a un Guard/Middleware de Nest que decodifica el
// JWT y hace `request.user = payload`, pero en Spring el resultado no se guarda
// en el `request`, sino en el SecurityContextHolder (un almacén thread-local
// que Spring Security consulta en toda la app para saber "quién soy ahora mismo").
//
// OncePerRequestFilter garantiza que este filtro se ejecute una sola vez por
// request (evita duplicados en forwards/includes internos de Servlet).
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService; // aquí se inyecta CustomUserDetailsService

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Sin header o sin el prefijo "Bearer ": no hay nada que autenticar acá,
        // se deja pasar la petición sin usuario. Si la ruta requiere auth,
        // más adelante en la cadena la rechazará el propio SecurityFilterChain (401).
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response); // sigue al siguiente filtro/controller, como next() en Express
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            String email = jwtService.extractEmail(token);
            // Solo autentica si aún no hay una autenticación ya establecida en
            // este contexto (evita pisar una autenticación previa en el mismo request).
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                if (jwtService.isTokenValid(token, userDetails)) {
                    // Construye el objeto de autenticación "ya autenticado" y lo guarda
                    // en el SecurityContextHolder. A partir de acá, en el controller
                    // podrías inyectar el usuario actual, y las reglas de
                    // .authorizeHttpRequests(...) de SecurityConfig ya lo ven como logueado.
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException | IllegalArgumentException ignored) {
            // Token ausente/expirado/mal formado: se deja la petición sin autenticar y el filtro de autorización decide si la rechaza.
        }

        filterChain.doFilter(request, response);
    }
}
