package tecleros.sysgepolidep.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    // Clave secreta para firmar los tokens.
    // Más adelante la vamos a sacar del código y llevar a application.properties.
    private static final String SECRET_KEY =
            "SysGePoliDepClaveSecretaParaJWT2026Segura";

    // Tiempo de duración del token: 1 hora
    private static final long EXPIRATION_TIME =
            1000 * 60 * 60;

    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(
                SECRET_KEY.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generarToken(
            Long idUsuario,
            String nombreUsuario,
            List<String> roles) {

        Date ahora = new Date();

        Date expiracion = new Date(
                ahora.getTime() + EXPIRATION_TIME
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