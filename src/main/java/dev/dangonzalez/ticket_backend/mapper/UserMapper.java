package dev.dangonzalez.ticket_backend.mapper;

import dev.dangonzalez.ticket_backend.domain.User;
import dev.dangonzalez.ticket_backend.domain.dto.UserResponseDTO;

// Mapper "a mano" (sin librería tipo MapStruct/AutoMapper): una clase final
// con constructor privado y solo métodos static -> es el equivalente Java a
// un archivo de funciones puras de utilidades en TS (export function toDto(...)),
// pero empaquetado en una clase porque Java no permite funciones sueltas a nivel
// de módulo, todo tiene que vivir dentro de una clase.
public final class UserMapper {

    // Constructor privado: evita que alguien haga `new UserMapper()`, ya que
    // la clase solo tiene métodos estáticos y no tiene sentido instanciarla.
    private UserMapper() {
    }

    // Convierte la entidad (con password incluido) al DTO público (sin password).
    // Este es el punto donde se garantiza que el hash de la contraseña
    // JAMÁS sale hacia el cliente.
    public static UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
