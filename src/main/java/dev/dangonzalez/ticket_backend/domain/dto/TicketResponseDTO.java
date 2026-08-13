package dev.dangonzalez.ticket_backend.domain.dto;

import dev.dangonzalez.ticket_backend.domain.TicketPriority;
import dev.dangonzalez.ticket_backend.domain.TicketStatus;

// DTO de salida: lo que realmente viaja como JSON al cliente (Angular).
// Nota que assignedTo/createdBy NO son la entidad User completa (con password
// incluido), sino UserResponseDTO -> así nunca se filtra el hash de la
// contraseña ni datos internos. Este mapeo lo hace TicketMapper.
public record TicketResponseDTO(
        Long id,
        String title,
        String description,
        TicketStatus status,
        TicketPriority priority,
        UserResponseDTO assignedTo,
        UserResponseDTO createdBy
) {
}
