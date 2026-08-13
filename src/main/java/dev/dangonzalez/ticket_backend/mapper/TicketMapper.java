package dev.dangonzalez.ticket_backend.mapper;

import dev.dangonzalez.ticket_backend.domain.Ticket;
import dev.dangonzalez.ticket_backend.domain.dto.TicketResponseDTO;

// Mismo patrón que UserMapper: clase utilitaria estática, sin instancias.
public final class TicketMapper {

    private TicketMapper() {
    }

    public static TicketResponseDTO toResponseDTO(Ticket ticket) {
        return new TicketResponseDTO(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                // assignedTo es opcional (nullable), así que hay que comprobar null
                // antes de mapear; en TS sería el equivalente a usar optional chaining:
                // ticket.assignedTo ? UserMapper.toResponseDTO(ticket.assignedTo) : null
                ticket.getAssignedTo() != null ? UserMapper.toResponseDTO(ticket.getAssignedTo()) : null,
                // createdBy nunca es null (columna NOT NULL en la entidad), no hace falta el check
                UserMapper.toResponseDTO(ticket.getCreatedBy())
        );
    }
}
