package tecleros.sysgepolidep.pago;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tecleros.sysgepolidep.socio.Socio;

import java.time.LocalDate;

@Entity
@Table(name = "membresia")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Membresia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id_Membresia")
    private Long idMembresia;

    @ManyToOne
    @JoinColumn(name = "Id_Categoria", nullable = false)
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "Id_Usuario", nullable = false)
    private Socio socio;

    @Column(name = "Fecha_Inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "Fecha_Venc", nullable = false)
    private LocalDate fechaVenc;

    @Column(name = "Estado", length = 20)
    private String estado = "VIGENTE";
}