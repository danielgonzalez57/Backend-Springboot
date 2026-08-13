package dev.dangonzalez.ticket_backend.exception;

// Misma idea que ResourceNotFoundException, pero mapeada a 409 Conflict
// en GlobalExceptionHandler (se lanza en AuthService.register()).
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
