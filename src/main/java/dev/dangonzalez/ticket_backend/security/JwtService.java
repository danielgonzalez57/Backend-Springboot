package dev.dangonzalez.ticket_backend.security;

import dev.dangonzalez.ticket_backend.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

// Encapsula TODO lo relacionado a generar/leer/validar JWT (usa la librería jjwt,
// el equivalente Java a `jsonwebtoken` de Node). No sabe nada de HTTP ni de
// Spring Security, solo de tokens -> responsabilidad única.
@Service
public class JwtService {

    private final SecretKey signingKey; // clave secreta ya decodificada, lista para firmar/verificar
    private final long expirationMs;

    // @Value("${jwt.secret}") inyecta el valor de application.properties
    // (jwt.secret=...), equivalente a leer process.env.JWT_SECRET en Node,
    // pero resuelto por Spring en el momento de construir el bean.
    public JwtService(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.expiration-ms}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMs = expirationMs;
    }

    // Genera un JWT firmado con HMAC-SHA a partir de los datos del usuario.
    // "subject" = a quién pertenece el token (aquí, el email). "claim" = dato
    // extra embebido en el payload (aquí, el rol) -> igual que el payload
    // que le pasas a jwt.sign({ sub, role }, secret) en Node.
    public String generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact(); // serializa a la típica cadena "header.payload.signature"
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Un token es válido si el email coincide con el usuario cargado Y no expiró.
    // Se usa en JwtAuthenticationFilter en cada request protegido.
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String email = extractEmail(token);
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    // Método genérico (usa Function<Claims, T>, similar a un callback tipado en TS)
    // para no duplicar el parseo del token en extractEmail/isTokenExpired.
    // Jwts.parser()...parseSignedClaims(token) valida la firma (con signingKey)
    // y lanza JwtException si el token fue alterado o está mal formado.
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }
}
