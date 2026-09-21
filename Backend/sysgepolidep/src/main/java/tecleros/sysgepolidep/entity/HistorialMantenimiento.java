package tecleros.sysgepolidep.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_mantenimiento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialMantenimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id_Historial")
    private Long idHistorial;

    @Column(name = "Tipo_Operacion", nullable = false, length = 50)
    private String tipoOperacion;

    @Column(name = "Fecha_Operacion")
    private LocalDateTime fechaOperacion = LocalDateTime.now();

    @Column(name = "Observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @ManyToOne
    @JoinColumn(name = "Id_Instalacion", nullable = false)
    private Instalacion instalacion;

    @ManyToOne
    @JoinColumn(name = "Id_Empleado", nullable = false)
    private Empleado empleado;
}