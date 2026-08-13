package dev.dangonzalez.ticket_backend.service;

import dev.dangonzalez.ticket_backend.domain.Ticket;
import dev.dangonzalez.ticket_backend.domain.TicketStatus;
import dev.dangonzalez.ticket_backend.domain.User;
import dev.dangonzalez.ticket_backend.domain.dto.TicketRequestDTO;
import dev.dangonzalez.ticket_backend.domain.dto.TicketResponseDTO;
import dev.dangonzalez.ticket_backend.domain.dto.TicketStatusUpdateDTO;
import dev.dangonzalez.ticket_backend.exception.ResourceNotFoundException;
import dev.dangonzalez.ticket_backend.mapper.TicketMapper;
import dev.dangonzalez.ticket_backend.repository.TicketRepository;
import dev.dangonzalez.ticket_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// @Service marca esta clase como un bean de la capa de negocio, exactamente
// igual que @Injectable() en Nest sobre una clase *Service. Spring la detecta
// por @ComponentScan y la registra en el contenedor de DI.
// Aquí vive la LÓGICA: los controllers solo delegan, no deciden nada.
@Service
@RequiredArgsConstructor // Lombok genera el constructor con los 2 campos de abajo
                          // -> Spring hace inyección por constructor, como en Nest.
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    // Nota: sin @Transactional explícito, cada llamada a save()/findById() del
    // repository abre y cierra su propia transacción corta (comportamiento por
    // defecto de Spring Data JPA). Se usa @Transactional explícito cuando se
    // necesita agrupar varias operaciones en una sola transacción atómica
    // (ver @Transactional(readOnly = true) más abajo, que es una optimización
    // de lectura, no una necesidad de atomicidad).
    public TicketResponseDTO createTicket(TicketRequestDTO request) {
        // orElseThrow: si el Optional viene vacío, lanza la excepción del lambda.
        // Es el equivalente a: `if (!user) throw new NotFoundException(...)` en Nest,
        // pero expresado de forma funcional/fluida.
        User createdBy = userRepository.findById(request.createdBy())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario creador no encontrado con id " + request.createdBy()));

        // Patrón builder de Lombok (ver @Builder en Ticket.java): construye el
        // objeto paso a paso, más legible que un constructor con 6 posiciones.
        Ticket ticket = Ticket.builder()
                .title(request.title())
                .description(request.description())
                .status(TicketStatus.OPEN) // regla de negocio: todo ticket nace OPEN, el cliente no lo decide
                .priority(request.priority())
                .assignedTo(resolveAssignedTo(request.assignedTo()))
                .createdBy(createdBy)
                .build();

        // save() hace INSERT (si el id es null) o UPDATE (si ya existe), y
        // devuelve la entidad ya persistida (con el id generado por la BD).
        return TicketMapper.toResponseDTO(ticketRepository.save(ticket));
    }

    // readOnly = true: le dice a Hibernate que no espere cambios, puede optimizar
    // (evita el "dirty checking" de autocommit de escritura). Buena práctica en
    // todo método que solo lee, no tiene equivalente directo en TypeORM (ahí
    // simplemente no envuelves en transacción si no escribes).
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getAllTickets() {
        // findAll() -> List<Ticket>; .stream().map(...).toList() es el equivalente
        // Java a un `.map()` de un array en JS, pero con Streams (API funcional
        // de Java para transformar colecciones sin mutar la original).
        return ticketRepository.findAll().stream()
                .map(TicketMapper::toResponseDTO) // method reference, azúcar sintáctico de (t) -> TicketMapper.toResponseDTO(t)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketResponseDTO getTicketById(Long id) {
        return TicketMapper.toResponseDTO(findTicketOrThrow(id));
    }

    public TicketResponseDTO updateTicket(Long id, TicketRequestDTO request) {
        Ticket ticket = findTicketOrThrow(id);

        // Como `ticket` es una entidad manejada por JPA dentro de una transacción,
        // en teoría bastaría con hacer los setters para que Hibernate detecte el
        // cambio y genere el UPDATE solo (dirty checking). Aquí igual se llama a
        // save() explícitamente para que quede claro y sea consistente con el resto.
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setPriority(request.priority());
        ticket.setAssignedTo(resolveAssignedTo(request.assignedTo()));

        return TicketMapper.toResponseDTO(ticketRepository.save(ticket));
    }

    // Endpoint dedicado solo a cambiar el status (PATCH), separado de updateTicket (PUT)
    // para no permitir tocar otros campos por accidente en esta operación.
    public TicketResponseDTO updateStatus(Long id, TicketStatusUpdateDTO request) {
        Ticket ticket = findTicketOrThrow(id);
        ticket.setStatus(request.status());
        return TicketMapper.toResponseDTO(ticketRepository.save(ticket));
    }

    public void deleteTicket(Long id) {
        // existsById es más barato que findById: solo hace un SELECT COUNT/EXISTS,
        // no trae la entidad completa, solo para validar que exista antes de borrar.
        if (!ticketRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ticket no encontrado con id " + id);
        }
        ticketRepository.deleteById(id);
    }

    // Método privado de ayuda (helper), no expuesto fuera de la clase.
    // Evita repetir el mismo findById().orElseThrow(...) en 3 métodos distintos.
    private Ticket findTicketOrThrow(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado con id " + id));
    }

    // assignedTo es opcional: si el id viene null, el ticket queda sin asignar.
    // Si viene un id, hay que validar que ese usuario exista de verdad.
    private User resolveAssignedTo(Long assignedToId) {
        if (assignedToId == null) {
            return null;
        }
        return userRepository.findById(assignedToId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario asignado no encontrado con id " + assignedToId));
    }
}
