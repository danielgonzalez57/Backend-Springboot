package dev.dangonzalez.ticket_backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Entidad JPA para la tabla "tickets". Ver User.java para la explicación general
// de @Entity/@Table y las anotaciones de Lombok (@Getter/@Setter/@Builder/...).
@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000) // VARCHAR(2000), opcional (puede ser null)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketPriority priority;

    // Relación N:1 -> muchos tickets pueden apuntar al mismo usuario asignado.
    // FetchType.LAZY = Hibernate NO carga el User de la BD hasta que llames
    // ticket.getAssignedTo().algo(); evita cargar datos que no vas a usar
    // (equivalente a no hacer el `populate`/`relations` de más en TypeORM/Mongoose).
    // Puede ser null: un ticket puede no estar asignado a nadie todavía.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to") // FK en la tabla tickets
    private User assignedTo;

    // Igual que arriba, pero obligatorio: todo ticket tiene que tener un creador.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
}
