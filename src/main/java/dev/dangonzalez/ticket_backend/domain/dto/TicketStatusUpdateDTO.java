package dev.dangonzalez.ticket_backend.domain.dto;

import dev.dangonzalez.ticket_backend.domain.TicketStatus;
import jakarta.validation.constraints.NotNull;

// DTO chiquito, dedicado únicamente al endpoint PATCH /tickets/{id}/status.
// Separar este "mini-DTO" del TicketRequestDTO grande es intencional: un PATCH
// de estado no debería poder tocar título/prioridad/asignación por accidente.
public record TicketStatusUpdateDTO(
        @NotNull(message = "El estado es obligatorio")
        TicketStatus status
) {
}
