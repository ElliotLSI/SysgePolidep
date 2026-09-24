package tecleros.sysgepolidep.instalacion;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "historial_mantenimiento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialMantenimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id_Mantenimiento")
    private Long idMantenimiento;

    @ManyToOne
    @JoinColumn(name = "Id_Instalacion", nullable = false)
    private Instalacion instalacion;

    @Column(name = "Fecha_Inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "Fecha_Fin")
    private LocalDate fechaFin;

    @Column(name = "Motivo", columnDefinition = "TEXT", nullable = false)
    private String motivo;
}