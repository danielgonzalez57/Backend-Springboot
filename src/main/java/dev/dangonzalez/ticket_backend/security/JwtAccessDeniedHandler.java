package dev.dangonzalez.ticket_backend.security;

import tools.jackson.databind.ObjectMapper;
import dev.dangonzalez.ticket_backend.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

// Hermano de JwtAuthenticationEntryPoint: este se dispara cuando el usuario SÍ
// está autenticado (JWT válido) pero no tiene el rol/permiso requerido -> 403.
// Misma razón para serializar "a mano": ocurre en la capa de filtros, fuera
// del alcance de @RestControllerAdvice.
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                        AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "No tienes permisos para acceder a este recurso",
                request.getRequestURI()
        );

        objectMapper.writeValue(response.getWriter(), body);
    }
}
