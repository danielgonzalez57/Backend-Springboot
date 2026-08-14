package dev.dangonzalez.ticket_backend.service;

import dev.dangonzalez.ticket_backend.domain.User;
import dev.dangonzalez.ticket_backend.domain.dto.UserResponseDTO;
import dev.dangonzalez.ticket_backend.domain.dto.UserUpdateRequestDTO;
import dev.dangonzalez.ticket_backend.exception.EmailAlreadyExistsException;
import dev.dangonzalez.ticket_backend.exception.ResourceNotFoundException;
import dev.dangonzalez.ticket_backend.exception.UserHasTicketsException;
import dev.dangonzalez.ticket_backend.mapper.UserMapper;
import dev.dangonzalez.ticket_backend.repository.TicketRepository;
import dev.dangonzalez.ticket_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Misma forma que TicketService: los controllers delegan, la lógica vive acá.
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        return UserMapper.toResponseDTO(findUserOrThrow(id));
    }

    public UserResponseDTO updateUser(Long id, UserUpdateRequestDTO request) {
        User user = findUserOrThrow(id);

        // Si cambia el email, hay que revalidar que el nuevo no esté tomado
        // por OTRO usuario (comparar contra sí mismo daría siempre "ya existe").
        if (!user.getEmail().equalsIgnoreCase(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Ya existe un usuario con el email " + request.email());
        }

        user.setName(request.name());
        user.setEmail(request.email());
        user.setRole(request.role());

        return UserMapper.toResponseDTO(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado con id " + id);
        }
        // Borrar un usuario con tickets creados/asignados rompería la FK
        // created_by/assigned_to -> se bloquea con un mensaje claro en vez de
        // dejar que explote como un error 500 de integridad de la BD.
        if (ticketRepository.existsByCreatedById(id) || ticketRepository.existsByAssignedToId(id)) {
            throw new UserHasTicketsException(
                    "No se puede eliminar: el usuario tiene tickets creados o asignados. Reasigná o eliminá esos tickets primero.");
        }
        userRepository.deleteById(id);
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id " + id));
    }
}
