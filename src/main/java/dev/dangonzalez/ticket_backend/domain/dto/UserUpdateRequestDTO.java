package dev.dangonzalez.ticket_backend.domain.dto;

import dev.dangonzalez.ticket_backend.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// DTO de entrada para PUT /api/v1/users/{id}. A diferencia de UserRequestDTO
// (registro) no lleva password: este endpoint solo edita perfil/rol, no
// resetea credenciales -> eso necesitaría su propio flujo (fuera de alcance acá).
public record UserUpdateRequestDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String name,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe tener un formato válido")
        String email,

        @NotNull(message = "El rol es obligatorio")
        Role role
) {
}
