package tecleros.sysgepolidep.socio;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tecleros.sysgepolidep.usuario.Usuario;

@Entity
@Table(name = "socio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Socio {

    @Id
    @Column(name = "Id_Usuario")
    private Long idUsuario;

    @Column(name = "Nro_Socio", nullable = false, unique = true)
    private Integer nroSocio;

    @OneToOne
    @MapsId
    @JoinColumn(name = "Id_Usuario")
    private Usuario usuario;
}