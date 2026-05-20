package cl.apipedidos.authservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    /**
     * Genera un token JWT firmado con HS256.
     */
    public String generarToken(String email, String rol) {
        return Jwts.builder()
            .subject(email)
            .claim("rol", rol)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expirationMs))
            .signWith(getKey())
            .compact();
    }

    /**
     * Extrae el email (subject) del token.
     */
    public String extraerEmail(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extrae el rol del token.
     */
    public String extraerRol(String token) {
        return getClaims(token).get("rol", String.class);
    }

    /**
     * Verifica firma y expiración — retorna false si es inválido.
     */
    public boolean esValido(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Retorna el tiempo de expiración configurado en milisegundos.
     */
    public long getExpirationMs() {
        return expirationMs;
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
            .verifyWith(getKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
