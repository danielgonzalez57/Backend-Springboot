package dev.dangonzalez.ticket_backend.exception;

import java.time.LocalDateTime;
import java.util.Map;

// Variante de ErrorResponse específica para errores de validación (@Valid):
// en vez de un único "message", trae un mapa { campo -> mensaje } para que
// el frontend pueda marcar cada input inválido individualmente
// (similar a la respuesta que arma un ValidationPipe de Nest con class-validator).
public record ValidationErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        Map<String, String> errors, // ej: { "email": "El email debe tener un formato válido" }
        String path
) {
}
