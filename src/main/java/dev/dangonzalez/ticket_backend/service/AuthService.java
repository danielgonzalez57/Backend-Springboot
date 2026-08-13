package dev.dangonzalez.ticket_backend.service;

import dev.dangonzalez.ticket_backend.domain.User;
import dev.dangonzalez.ticket_backend.domain.dto.AuthResponseDTO;
import dev.dangonzalez.ticket_backend.domain.dto.LoginRequestDTO;
import dev.dangonzalez.ticket_backend.domain.dto.UserRequestDTO;
import dev.dangonzalez.ticket_backend.domain.dto.UserResponseDTO;
import dev.dangonzalez.ticket_backend.exception.EmailAlreadyExistsException;
import dev.dangonzalez.ticket_backend.mapper.UserMapper;
import dev.dangonzalez.ticket_backend.repository.UserRepository;
import dev.dangonzalez.ticket_backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// Capa de negocio de autenticación: registro y login.
// Aquí se ve el patrón típico de Spring Security "delegar la validación de
// credenciales al AuthenticationManager" en vez de comparar passwords a mano.
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // bean BCrypt definido en SecurityConfig
    private final AuthenticationManager authenticationManager; // bean definido en SecurityConfig
    private final JwtService jwtService;

    public UserResponseDTO register(UserRequestDTO request) {
        // Regla de negocio: no permitir emails duplicados. Podría delegarse
        // al índice UNIQUE de la BD (que también existe, ver User.java), pero
        // validarlo aquí primero permite devolver un error 409 claro en vez
        // de un error genérico de constraint de BD.
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Ya existe un usuario con el email " + request.email());
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                // NUNCA guardar el password tal cual: passwordEncoder.encode() aplica
                // BCrypt (hash + salt). Es el equivalente a bcrypt.hashSync() en Node.
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .build();

        return UserMapper.toResponseDTO(userRepository.save(user));
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        // authenticate() dispara internamente todo el flujo de Spring Security:
        // busca el usuario vía CustomUserDetailsService.loadUserByUsername(email),
        // compara el password con passwordEncoder, y si algo falla lanza
        // AuthenticationException (capturada por GlobalExceptionHandler -> 401).
        // Es el equivalente a llamar a `authService.validateUser()` en una
        // LocalStrategy de Passport, pero ya integrado en el framework.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        // Si llegamos aquí, las credenciales son válidas. Se vuelve a buscar el
        // User completo (el AuthenticationManager solo confirma que son válidas,
        // no te devuelve la entidad de dominio) para poder generar el JWT con sus datos.
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado: " + request.email()));

        String token = jwtService.generateToken(user);

        // "Bearer" es el esquema estándar que el cliente debe mandar como
        // `Authorization: Bearer <token>` en cada request protegido.
        return new AuthResponseDTO(token, "Bearer", UserMapper.toResponseDTO(user));
    }
}
