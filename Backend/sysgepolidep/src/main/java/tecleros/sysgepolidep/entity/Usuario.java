package tecleros.sysgepolidep.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdUsuario")
    private Long idUsuario;

    @Column(name = "DNI", nullable = false, unique = true, length = 15)
    private String dni;

    @Column(name = "Nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "Apellido", nullable = false, length = 50)
    private String apellido;

    @Column(name = "Fecha_nac")
    private LocalDate fechaNac;

    @Column(name = "Domicilio", length = 100)
    private String domicilio;

    @Column(name = "Tel", length = 30)
    private String tel;

    @Column(name = "Email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "Nombre_Usuario", nullable = false, unique = true, length = 50)
    private String nombreUsuario;

    @Column(name = "Contrasena", nullable = false)
    private String contrasena;

    @Column(name = "Estado", nullable = false, length = 3)
    private String estado = "ACT";

    @Column(name = "Pertenencia", length = 20)
    private String pertenencia = "NINGUNO";

    @Column(name = "Legajo", unique = true, length = 20)
    private String legajo;
}