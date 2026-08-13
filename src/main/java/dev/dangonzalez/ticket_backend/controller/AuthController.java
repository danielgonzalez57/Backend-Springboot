package dev.dangonzalez.ticket_backend.controller;

import dev.dangonzalez.ticket_backend.domain.dto.AuthResponseDTO;
import dev.dangonzalez.ticket_backend.domain.dto.LoginRequestDTO;
import dev.dangonzalez.ticket_backend.domain.dto.UserRequestDTO;
import dev.dangonzalez.ticket_backend.domain.dto.UserResponseDTO;
import dev.dangonzalez.ticket_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Único controller cuyas rutas son públicas (ver SecurityConfig: "/api/v1/auth/**"
// está en permitAll()). Todo lo demás en la API exige JWT.
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
