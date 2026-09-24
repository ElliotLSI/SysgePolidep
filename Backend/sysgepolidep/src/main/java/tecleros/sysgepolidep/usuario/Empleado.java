package tecleros.sysgepolidep.usuario;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "empleado")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Empleado {

    @Id
    @Column(name = "Id_Usuario")
    private Long idUsuario;

    @Column(name = "Legajo", nullable = false, unique = true)
    private Integer legajo;

    @Column(name = "Turno", length = 50)
    private String turno;

    @OneToOne
    @MapsId
    @JoinColumn(name = "Id_Usuario")
    private Usuario usuario;
}