package dev.dangonzalez.ticket_backend.security;

import tools.jackson.databind.ObjectMapper;
import dev.dangonzalez.ticket_backend.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

// Se dispara cuando una petición a una ruta protegida NO trae credenciales
// válidas (no autenticado) -> responde 401. Es un componente aparte de
// GlobalExceptionHandler porque este error ocurre en la capa de filtros de
// Servlet, ANTES de llegar al DispatcherServlet/controllers, así que el
// @RestControllerAdvice normal no llega a interceptarlo; por eso hay que
// serializar el JSON "a mano" con ObjectMapper (el mismo Jackson que usa
// Spring internamente para (de)serializar tus DTOs).
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "No autenticado: token JWT ausente o inválido",
                request.getRequestURI()
        );

        objectMapper.writeValue(response.getWriter(), body); // escribe el JSON directo al response
    }
}
