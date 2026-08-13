package dev.dangonzalez.ticket_backend.repository;

import dev.dangonzalez.ticket_backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data JPA parsea el NOMBRE del método y genera el SQL solo:
    // "findByEmail" -> SELECT * FROM users WHERE email = ?
    // No hace falta escribir la query (aunque también podrías con @Query si
    // fuera algo más complejo). Es "magia" basada en convención de nombres,
    // parecido a los métodos que genera Mongoose (findOne({ email })) pero
    // aquí se derivan solo del nombre del método.
    //
    // Optional<User> = puede o no haber resultado, en vez de devolver null.
    // Obliga a manejar el caso "no encontrado" explícitamente (ver .orElseThrow
    // en AuthService/CustomUserDetailsService), similar a usar `User | null` en TS
    // pero con una API que fuerza a decidir qué hacer con el "vacío".
    Optional<User> findByEmail(String email);

    // "existsByEmail" -> SELECT COUNT(*) > 0 ... más barato que traer la entidad
    // completa solo para chequear si ya existe (se usa en el registro).
    boolean existsByEmail(String email);
}
