package dev.dangonzalez.ticket_backend.domain.dto;

import dev.dangonzalez.ticket_backend.domain.Role;

import java.time.LocalDateTime;

// DTO de salida: version "segura" y pública de User, sin el campo password.
// UserMapper es el único lugar que convierte User (entidad) -> UserResponseDTO.
public record UserResponseDTO(
        Long id,
        String name,
        String email,
        Role role,
        LocalDateTime createdAt
) {
}
