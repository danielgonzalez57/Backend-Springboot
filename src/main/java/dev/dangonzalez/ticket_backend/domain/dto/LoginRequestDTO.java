package dev.dangonzalez.ticket_backend.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// `record` de Java = una clase inmutable de solo datos, generada automáticamente
// con constructor, getters (aquí se llaman email()/password(), sin "get"),
// equals/hashCode/toString. Es el equivalente a una interfaz/type de TS pero
// con identidad de clase real y sin poder mutarse después de creado.
//
// Las anotaciones @NotBlank/@Email son de Bean Validation (jakarta.validation),
// el equivalente exacto a los decoradores de `class-validator` en Nest
// (@IsNotEmpty(), @IsEmail()). Se activan cuando el controller recibe
// @Valid @RequestBody LoginRequestDTO (ver AuthController) -> si fallan,
// Spring lanza MethodArgumentNotValidException, que captura GlobalExceptionHandler.
public record LoginRequestDTO(
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe tener un formato válido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {
}
