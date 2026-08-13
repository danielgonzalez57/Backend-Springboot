package dev.dangonzalez.ticket_backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

// @RestControllerAdvice = intercepta las excepciones lanzadas por CUALQUIER
// @RestController de la app y las centraliza acá. Es el equivalente exacto
// a un Exception Filter GLOBAL de Nest (@Catch() + app.useGlobalFilters()),
// pero declarativo: un método por tipo de excepción, en vez de un switch manual.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Logger estilo SLF4J, el "console.log/error" estándar del ecosistema Java
    // (aquí solo se usa para loguear errores 500 inesperados, ver handleGeneric).
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // @ExceptionHandler(X.class): "si en cualquier controller se lanza X,
    // ejecuta este método en su lugar". Spring recorre estos handlers
    // buscando el más específico para la excepción lanzada.
    //
    // Este captura los fallos de @Valid (ver los DTOs con @NotBlank/@Email/...)
    // y arma el mapa { campo -> mensaje } que exponen los inputs inválidos.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                                      HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>(); // LinkedHashMap = conserva el orden de inserción
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ValidationErrorResponse body = new ValidationErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                errors,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // Nuestra excepción custom (lanzada en TicketService/AuthService/etc.) -> 404.
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex,
                                                                  HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // Lanzada en AuthService.register() -> 409 Conflict.
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException ex,
                                                                    HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // Excepción que lanza Spring Security cuando authenticationManager.authenticate()
    // falla (credenciales incorrectas en el login) -> 401.
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex,
                                                                HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Credenciales inválidas", request);
    }

    // Lanzada por Spring Security cuando el usuario SÍ está autenticado pero
    // no tiene el rol/permiso necesario (p. ej. con @PreAuthorize) -> 403.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex,
                                                              HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "No tienes permisos para acceder a este recurso", request);
    }

    // Handler "catch-all": cualquier excepción no manejada arriba cae aquí.
    // Es la red de seguridad para no filtrar stack traces al cliente.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        // Excepciones propias de Spring MVC (404 de recurso estático, 405 método no soportado, etc.)
        // implementan ErrorResponse y ya traen su propio status code; no deben caer como 500.
        if (ex instanceof org.springframework.web.ErrorResponse errorResponse) {
            HttpStatus status = HttpStatus.valueOf(errorResponse.getStatusCode().value());
            String message = errorResponse.getBody().getDetail() != null
                    ? errorResponse.getBody().getDetail()
                    : ex.getMessage();
            return buildResponse(status, message, request);
        }

        // Cualquier otra cosa es realmente inesperada (bug, NPE, fallo de BD...):
        // se loguea completo en el servidor pero al cliente solo se le informa
        // un mensaje genérico -> nunca exponer detalles internos por seguridad.
        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Ha ocurrido un error inesperado", request);
    }

    // Helper privado para no repetir la construcción del ErrorResponse en cada handler.
    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }
}
