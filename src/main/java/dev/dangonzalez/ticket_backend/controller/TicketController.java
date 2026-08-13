package dev.dangonzalez.ticket_backend.controller;

import dev.dangonzalez.ticket_backend.domain.dto.TicketRequestDTO;
import dev.dangonzalez.ticket_backend.domain.dto.TicketResponseDTO;
import dev.dangonzalez.ticket_backend.domain.dto.TicketStatusUpdateDTO;
import dev.dangonzalez.ticket_backend.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// @RestController = @Controller + @ResponseBody: cada método serializa su
// valor de retorno a JSON automáticamente (Jackson), igual que en Nest un
// @Controller() cuyos métodos devuelven objetos planos.
// @RequestMapping("/api/v1/tickets") = prefijo de ruta, equivalente a
// @Controller('api/v1/tickets') en Nest.
@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor // inyecta TicketService por constructor, como en Nest
public class TicketController {

    private final TicketService ticketService;

    // @PostMapping = @Post() de Nest.
    // @Valid activa las validaciones de Bean Validation del DTO (@NotBlank, etc.)
    // ANTES de que el método se ejecute -> equivalente al ValidationPipe global de Nest.
    // Si falla, ni siquiera entra al método: Spring lanza MethodArgumentNotValidException.
    @PostMapping
    public ResponseEntity<TicketResponseDTO> create(@Valid @RequestBody TicketRequestDTO request) {
        // ResponseEntity<T> te da control explícito del status code + body,
        // similar a usar @Res() en Nest pero sin perder el manejo automático
        // de excepciones (a diferencia de @Res() de Express, esto SÍ sigue
        // pasando por el GlobalExceptionHandler si algo falla antes).
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.createTicket(request));
    }

    // @GetMapping sin path = GET /api/v1/tickets (equivalente a @Get() en Nest)
    @GetMapping
    public ResponseEntity<List<TicketResponseDTO>> getAll() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    // @PathVariable extrae {id} de la URL, como @Param('id') en Nest.
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> update(@PathVariable Long id, @Valid @RequestBody TicketRequestDTO request) {
        return ResponseEntity.ok(ticketService.updateTicket(id, request));
    }

    // Ruta específica solo para cambiar el estado -> PATCH semánticamente correcto
    // (actualización parcial) en vez de reusar el PUT de arriba.
    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketResponseDTO> updateStatus(@PathVariable Long id,
                                                            @Valid @RequestBody TicketStatusUpdateDTO request) {
        return ResponseEntity.ok(ticketService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        // 204 No Content: operación exitosa sin cuerpo de respuesta,
        // igual que @HttpCode(204) en Nest en un endpoint DELETE.
        return ResponseEntity.noContent().build();
    }
}
