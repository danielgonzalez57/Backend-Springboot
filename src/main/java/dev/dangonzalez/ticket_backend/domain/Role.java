package dev.dangonzalez.ticket_backend.domain;

// Enum de Java: un tipo cerrado de valores, muy parecido a
// `type Role = "ADMIN" | "AGENT" | "USER"` en TypeScript, o a un enum de TS,
// pero con más capacidades (puede tener métodos, campos, etc.).
// Spring Security lo usa anteponiendo "ROLE_" (ver CustomUserDetailsService)
// para construir la autoridad/permiso del usuario autenticado.
public enum Role {
    ADMIN,
    AGENT,
    USER
}
