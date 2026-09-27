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
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                // Desactivar CSRF para nuestra API REST
                .csrf(csrf -> csrf.disable())

                // No utilizar sesiones HTTP
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // Configurar los permisos de los endpoints
                .authorizeHttpRequests(auth -> auth

                        // Permitir el registro de usuarios
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/usuarios"
                        ).permitAll()

                        // Permitir el login
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/login"
                        ).permitAll()

                        // Endpoint de prueba: solamente ADMINISTRADOR
                        .requestMatchers(
                                "/api/auth/prueba-rol"
                        ).hasRole("ADMINISTRADOR")

                        // Solo los administradores pueden crear administradores
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/administradores/**"
                        ).hasRole("ADMINISTRADOR")

                        // Proteger el resto de los endpoints
                        .anyRequest().authenticated()
                )

                // Ejecutar el filtro JWT antes del filtro de usuario y contraseña
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }


}
