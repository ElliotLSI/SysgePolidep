package tecleros.sysgepolidep.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "empleado")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Empleado {

    @Id
    @Column(name = "Id_Usuario")
    private Long idUsuario;

    @Column(name = "Sector", length = 50)
    private String sector;

    @Column(name = "Cargo", length = 50)
    private String cargo;

    @Column(name = "FechaIngreso")
    private LocalDate fechaIngreso;

    @OneToOne
    @MapsId
    @JoinColumn(name = "Id_Usuario")
    private Usuario usuario;
}