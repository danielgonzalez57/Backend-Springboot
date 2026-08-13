package dev.dangonzalez.ticket_backend.security;

import dev.dangonzalez.ticket_backend.domain.User;
import dev.dangonzalez.ticket_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Implementa la interfaz UserDetailsService, el "puente" que Spring Security
// usa para saber cómo buscar un usuario en TU base de datos. Es el equivalente
// a implementar una LocalStrategy/JwtStrategy de Passport en Nest: le dices al
// framework "así es como cargo un usuario a partir de su identificador".
//
// La usan tanto AuthService (indirectamente, vía AuthenticationManager en el
// login) como JwtAuthenticationFilter (para reconstruir el usuario a partir
// del email guardado en el JWT).
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // "username" aquí es en realidad el email (en este proyecto el login es
    // por email, no por username); el nombre del parámetro viene fijo de la
    // interfaz de Spring Security, que originalmente asume username.
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email " + email));

        // Convierte TU entidad User a un UserDetails "genérico" que Spring Security
        // entiende. org.springframework.security.core.userdetails.User es la
        // implementación por defecto que trae el framework (no confundir con
        // nuestra propia entidad User del dominio, de ahí el nombre completo).
        // "ROLE_" + role es la convención que exige Spring Security para mapear
        // authorities a roles (p. ej. lo que usarías en hasRole("ADMIN")).
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword()) // hash BCrypt, para que el AuthenticationManager lo compare
                .authorities(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                .build();
    }
}
