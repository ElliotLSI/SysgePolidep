package tecleros.sysgepolidep.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "reserva")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id_Reserva")
    private Long idReserva;

    @Column(name = "Fecha_Reserva", nullable = false)
    private LocalDate fechaReserva;

    @Column(name = "Hora_Inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "Duracion_Horas", nullable = false)
    private Integer duracionHoras = 1;

    @Column(name = "Estado", length = 20)
    private String estado = "PENDIENTE";

    @Column(name = "Monto_Total", nullable = false)
    private Double montoTotal;

    @Column(name = "Fecha_Creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "Id_Usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "Id_Instalacion", nullable = false)
    private Instalacion instalacion;
}