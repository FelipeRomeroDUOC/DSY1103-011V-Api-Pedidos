package cl.apipedidos.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

/**
 * Utilidad JWT de solo lectura — valida tokens emitidos por auth-service.
 * No genera tokens nuevos.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String extraerEmail(String token) {
        return getClaims(token).getSubject();
    }

    public String extraerRol(String token) {
        return getClaims(token).get("rol", String.class);
    }

    public boolean esValido(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
            .verifyWith(getKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
