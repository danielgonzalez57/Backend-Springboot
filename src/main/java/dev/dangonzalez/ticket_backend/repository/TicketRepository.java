package dev.dangonzalez.ticket_backend.repository;

import dev.dangonzalez.ticket_backend.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

// Esto es TODO lo que hace falta escribir para tener CRUD completo sobre "tickets".
// JpaRepository<Ticket, Long> le dice a Spring Data JPA: "genera en tiempo de
// ejecución una implementación de esta interfaz" con métodos como save(), findById(),
// findAll(), deleteById(), existsById(), count()... (Long = tipo de la PK).
//
// Es el equivalente a un Repository<Ticket> de TypeORM, pero acá NO escribes
// ninguna clase: solo declaras la interfaz y Spring te inyecta un proxy que
// implementa todo por ti. Se inyecta en TicketService vía constructor, tal cual
// inyectarías @InjectRepository(Ticket) en un service de Nest+TypeORM.
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Se usan en UserService.deleteUser(): un usuario no se puede borrar si
    // todavía tiene tickets creados o asignados (violaría la FK created_by/
    // assigned_to). "CreatedById"/"AssignedToId" -> Spring Data navega la
    // relación (ticket.createdBy.id / ticket.assignedTo.id) sin escribir JPQL.
    boolean existsByCreatedById(Long userId);

    boolean existsByAssignedToId(Long userId);
}
