package dev.dangonzalez.ticket_backend.exception;

// Misma idea que EmailAlreadyExistsException, mapeada a 409 Conflict en
// GlobalExceptionHandler. Se lanza en UserService.deleteUser() cuando el
// usuario tiene tickets creados o asignados: borrarlo rompería la FK.
public class UserHasTicketsException extends RuntimeException {

    public UserHasTicketsException(String message) {
        super(message);
    }
}
