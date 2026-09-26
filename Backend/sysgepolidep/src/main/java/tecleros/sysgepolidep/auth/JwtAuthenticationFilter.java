package tecleros.sysgepolidep.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Obtener el encabezado Authorization
        String encabezado = request.getHeader("Authorization");

        // Comprobar que exista un token Bearer
        if (encabezado == null || !encabezado.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraer el token
        String token = encabezado.substring(7);

        // Validar el token
        if (!jwtService.esTokenValido(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Obtener el nombre de usuario y los roles
        String nombreUsuario = jwtService.obtenerNombreUsuario(token);

        List<String> roles = jwtService.obtenerClaims(token)
                .get("roles", List.class);

        // Convertir los roles al formato que utiliza Spring Security
        var autoridades = roles.stream()
                .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol))
                .collect(Collectors.toList());

        // Crear la autenticación
        UsernamePasswordAuthenticationToken autenticacion =
                new UsernamePasswordAuthenticationToken(
                        nombreUsuario,
                        null,
                        autoridades
                );

        // Registrar la autenticación en Spring Security
        SecurityContextHolder.getContext()
                .setAuthentication(autenticacion);

        // Continuar con la petición
        filterChain.doFilter(request, response);
    }
}