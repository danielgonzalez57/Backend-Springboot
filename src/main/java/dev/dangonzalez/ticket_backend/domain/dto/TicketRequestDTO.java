package dev.dangonzalez.ticket_backend.domain.dto;

import dev.dangonzalez.ticket_backend.domain.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// DTO de entrada para crear/actualizar un ticket (equivalente a un CreateTicketDto
// de Nest). Nota que NO incluye "status": el status inicial lo fija el propio
// TicketService (siempre nace en OPEN), no lo decide el cliente.
public record TicketRequestDTO(
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 150, message = "El título no puede superar los 150 caracteres")
        String title,

        @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
        String description, // opcional: sin @NotBlank/@NotNull

        @NotNull(message = "La prioridad es obligatoria")
        TicketPriority priority,

        Long assignedTo, // opcional: un ticket puede crearse sin asignar a nadie

        @NotNull(message = "El creador es obligatorio")
        Long createdBy
) {
}
