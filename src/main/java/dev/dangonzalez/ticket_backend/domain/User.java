package dev.dangonzalez.ticket_backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// Esta clase es una ENTIDAD JPA: representa una fila de la tabla "users".
// Es el equivalente a una @Entity de TypeORM o a un schema de Mongoose,
// pero usando Hibernate (la implementación de JPA) como ORM.
// OJO: esto NO es el objeto que se devuelve por la API (para eso están los DTOs
// en domain/dto). Mezclar entidad con respuesta HTTP es un anti-patrón común
// en ambos mundos (Nest/TypeORM también lo evita usando DTOs separados).
@Entity
@Table(name = "users") // nombre real de la tabla en Postgres
// Estas 4 anotaciones son de Lombok, una librería que genera código en tiempo
// de compilación para no escribirlo a mano (getters/setters/constructores).
// Piensa en Lombok como un "codegen" que reemplaza lo que en TS obtienes gratis
// con `public` fields o con clases normales.
@Getter // genera getName(), getEmail(), etc. (como acceder directo a this.name en TS)
@Setter // genera setName(...), setEmail(...), etc.
@NoArgsConstructor // constructor vacío `new User()` -> JPA lo necesita internamente
@AllArgsConstructor // constructor con todos los campos `new User(id, name, ...)`
@Builder // habilita el patrón builder: User.builder().name("x").build()
         // (similar a construir un objeto literal en TS pero con validación de tipos paso a paso)
public class User {

    @Id // clave primaria (PK)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // autoincremental, delega en la BD (SERIAL en Postgres)
    private Long id;

    @Column(nullable = false) // NOT NULL en la tabla
    private String name;

    @Column(nullable = false, unique = true) // NOT NULL + índice UNIQUE
    private String email;

    // Nunca se guarda en texto plano: en AuthService se cifra con BCrypt antes de llegar aquí.
    @Column(nullable = false)
    private String password;

    // Guarda el enum como texto ("ADMIN", "AGENT", "USER") en vez de un número (EnumType.ORDINAL),
    // para que la BD sea legible y no se rompa si reordenas el enum en el futuro.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Hibernate rellena esta columna automáticamente al hacer INSERT (como un
    // default: now() en SQL, o @CreateDateColumn en TypeORM).
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
