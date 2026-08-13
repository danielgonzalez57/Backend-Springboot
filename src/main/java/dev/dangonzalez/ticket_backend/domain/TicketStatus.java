package dev.dangonzalez.ticket_backend.domain;

// Estados posibles de un ticket. Al guardarse como EnumType.STRING (ver Ticket.java),
// en la BD se guarda literalmente el texto "OPEN", "IN_PROGRESS", etc.
public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED
}
