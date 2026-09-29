
        package tecleros.sysgepolidep.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import tecleros.sysgepolidep.auth.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of("http://localhost:5173")
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type"
                )
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                // CORS
                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )

                // CSRF deshabilitado porque usamos JWT
                .csrf(csrf -> csrf.disable())

                // API REST sin sesiones
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // AUTORIZACIÓN
                .authorizeHttpRequests(auth -> auth

                        // ==============================
                        // CORS PREFLIGHT
                        // ==============================
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        // ==============================
                        // AUTENTICACIÓN
                        // ==============================
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/login"
                        ).permitAll()

                        // ==============================
                        // REGISTRO DE USUARIO
                        // ==============================
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/usuarios"
                        ).permitAll()

                        // ==============================
                        // PRUEBA DE ROL
                        // ==============================
                        .requestMatchers(
                                "/api/auth/prueba-rol"
                        ).hasRole("ADMINISTRADOR")

                        // ==============================
                        // CONSULTAR ROLES
                        // ==============================
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/auth/roles/**"
                        ).authenticated()

                        // ==============================
                        // ADMINISTRADORES
                        // ==============================
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/administradores/**"
                        ).hasRole("ADMINISTRADOR")

                        // ==============================
                        // CATEGORÍAS
                        // ==============================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/categorias/**"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/categorias/**"
                        ).hasRole("ADMINISTRADOR")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/categorias/**"
                        ).hasRole("ADMINISTRADOR")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/categorias/**"
                        ).hasRole("ADMINISTRADOR")

                        // ==============================
                        // INSTALACIONES
                        // ==============================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/instalaciones/**"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/instalaciones/**"
                        ).hasRole("ADMINISTRADOR")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/instalaciones/**"
                        ).hasRole("ADMINISTRADOR")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/instalaciones/**"
                        ).hasRole("ADMINISTRADOR")

                        // ==============================
                        // RESERVAS
                        // ==============================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/reservas/**"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/reservas"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "EMPLEADO",
                                "SOCIO",
                                "USUARIO"
                        )

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/reservas/*/cancelar"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "EMPLEADO",
                                "SOCIO",
                                "USUARIO"
                        )

                        // ==============================
                        // RESERVAS A FAVOR
                        // ==============================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/reservas-a-favor/**"
                        ).authenticated()

                        // La creación se realiza
                        // internamente al cancelar
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/reservas-a-favor"
                        ).denyAll()

                        // La utilización se realiza
                        // internamente al reprogramar
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/reservas-a-favor/*/utilizar"
                        ).denyAll()

                        // No permitimos borrar créditos
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/reservas-a-favor/**"
                        ).denyAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/reservas-a-favor/*/reprogramar"
                        ).hasAnyRole(
                                "SOCIO",
                                "USUARIO"
                        )

                        // ==============================
                        // PAGOS
                        // ==============================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/pagos/**"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/pagos"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "EMPLEADO",
                                "SOCIO",
                                "USUARIO"
                        )

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/pagos/**"
                        ).hasRole("ADMINISTRADOR")

                        // ==============================
                        // SOCIOS
                        // ==============================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/socios/**"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/socios/alta"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "EMPLEADO",
                                "USUARIO"
                        )

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/socios"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "EMPLEADO"
                        )

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/socios/**"
                        ).hasRole("ADMINISTRADOR")

                        // ==============================
                        // MEMBRESÍAS
                        // ==============================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/membresias/**"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/membresias"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "EMPLEADO"
                        )

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/membresias/*/renovar"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "EMPLEADO",
                                "SOCIO"
                        )

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/membresias/**"
                        ).hasRole("ADMINISTRADOR")

                        // ==============================
                        // MANTENIMIENTOS
                        // ==============================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/mantenimientos/**"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/mantenimientos"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "EMPLEADO"
                        )

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/mantenimientos/**"
                        ).hasRole("ADMINISTRADOR")

                        // ==============================
                        // USUARIOS
                        // ==============================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/usuarios/**"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/usuarios/**"
                        ).hasRole("ADMINISTRADOR")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/usuarios/**"
                        ).hasRole("ADMINISTRADOR")

                        // ==============================
                        // CUALQUIER OTRA RUTA
                        // ==============================

                        .anyRequest().authenticated()
                )

                // ==============================
                // JWT FILTER
                // ==============================

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}

