package dev.dangonzalez.ticket_backend.domain.dto;

// DTO de RESPUESTA (no lleva validaciones, esas son solo para lo que entra).
// Lo que se devuelve tras un login exitoso: el JWT, el tipo de token (para el
// header `Authorization: Bearer <token>`) y los datos públicos del usuario.
public record AuthResponseDTO(
        String token,
        String tokenType,
        UserResponseDTO user
) {
}
