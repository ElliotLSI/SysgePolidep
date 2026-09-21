package tecleros.sysgepolidep.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "pago")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id_Pago")
    private Long idPago;

    @Column(name = "Fecha")
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "Monto_Total", nullable = false)
    private Double montoTotal;

    @Column(name = "Medio_Pago", nullable = false, length = 50)
    private String medioPago;

    @Column(name = "Estado", length = 20)
    private String estado = "APROBADO";

    @ManyToOne
    @JoinColumn(name = "Id_Reserva")
    private Reserva reserva;

    @ManyToOne
    @JoinColumn(name = "Id_Membresia")
    private Membresia membresia;
}