package tecleros.sysgepolidep.usuario;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "administrador")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Administrador {

    @Id
    @Column(name = "Id_Usuario")
    private Long idUsuario;

    @Column(name = "Nivel_Acceso", nullable = false)
    private Integer nivelAcceso = 1;

    @OneToOne
    @MapsId
    @JoinColumn(name = "Id_Usuario")
    private Usuario usuario;
}