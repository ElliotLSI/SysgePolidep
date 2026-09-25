package tecleros.sysgepolidep.usuario;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id_Usuario")
    private Long idUsuario;

    @Column(name = "Nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "Apellido", nullable = false, length = 100)
    private String apellido;

    @Column(name = "DNI", nullable = false, unique = true, length = 20)
    private String dni;

    @Column(name = "Fecha_Nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "Domicilio", length = 200)
    private String domicilio;

    @Column(name = "Telefono", length = 30)
    private String telefono;

    @Column(name = "Email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "Nombre_Usuario", nullable = false, unique = true, length = 50)
    private String nombreUsuario;

    @Column(name = "Password", nullable = false, length = 255)
    private String password;

    @Column(name = "Pertenencia", length = 30)
    private String pertenencia = "NINGUNO";

    @Column(name = "Legajo", length = 30)
    private String legajo;

    @Column(name = "Fecha_Registro")
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Column(name = "Estado", length = 20)
    private String estado = "ACTIVO";

    @PrePersist
    public void prePersist() {

        if (this.fechaRegistro == null) {
            this.fechaRegistro = LocalDateTime.now();
        }

        if (this.estado == null) {
            this.estado = "ACTIVO";
        }

        if (this.pertenencia == null || this.pertenencia.trim().isEmpty()) {
            this.pertenencia = "NINGUNO";
        }
    }
}