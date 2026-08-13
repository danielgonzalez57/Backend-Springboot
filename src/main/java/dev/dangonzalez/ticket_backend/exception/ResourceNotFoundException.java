package dev.dangonzalez.ticket_backend.exception;

// Excepción propia (custom), extiende RuntimeException = "unchecked" (Java no
// obliga a declararla con `throws` ni a capturarla). Se lanza desde los
// services cuando algo no existe (ver TicketService/AuthService) y la captura
// GlobalExceptionHandler para transformarla en un 404 JSON.
// Es el equivalente directo a `throw new NotFoundException(msg)` de Nest
// (que también termina en un filtro global convirtiéndolo a HTTP).
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
