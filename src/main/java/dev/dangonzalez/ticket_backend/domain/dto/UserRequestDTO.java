package dev.dangonzalez.ticket_backend.domain.dto;

import dev.dangonzalez.ticket_backend.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// DTO de entrada para el registro (POST /api/v1/auth/register).
// La password viaja en texto plano SOLO en este request (por HTTPS en prod);
// AuthService la cifra con BCrypt antes de guardarla, nunca se persiste tal cual.
public record UserRequestDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String name,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe tener un formato válido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password,

        @NotNull(message = "El rol es obligatorio")
        Role role
) {
}
