package dev.dangonzalez.ticket_backend.exception;

import java.time.LocalDateTime;

// Forma estándar del JSON de error que devuelve la API cuando algo falla
// (404, 409, 401, 403, 500...). Tenerla centralizada evita que cada
// excepción devuelva un shape distinto -> el frontend Angular siempre
// puede confiar en esta misma estructura.
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,   // texto del status HTTP, ej. "Not Found"
        String message, // mensaje específico y legible del error
        String path     // URI donde ocurrió, útil para debuggear
) {
}
