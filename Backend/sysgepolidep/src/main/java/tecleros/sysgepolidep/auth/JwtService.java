package tecleros.sysgepolidep.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(
                secretKey.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generarToken(
            Long idUsuario,
            String nombreUsuario,
            List<String> roles) {

        Date ahora = new Date();

        Date expiracion = new Date(
                ahora.getTime() + expirationTime
        );

        return Jwts.builder()
                .subject(nombreUsuario)
                .claim("idUsuario", idUsuario)
                .claim("roles", roles)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(getSigningKey())
                .compact();
    }

    public Claims obtenerClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean esTokenValido(String token) {

        try {

            Claims claims = obtenerClaims(token);

            return claims.getExpiration().after(new Date());

        } catch (Exception e) {

            return false;
        }
    }

    public String obtenerNombreUsuario(String token) {

        return obtenerClaims(token).getSubject();
    }

    public Long obtenerIdUsuario(String token) {

        Number id = obtenerClaims(token)
                .get("idUsuario", Number.class);

        return id.longValue();
    }
}