package tecleros.sysgepolidep.pago;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tecleros.sysgepolidep.reserva.Reserva;

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

    // Relación opcional con Reserva
    @ManyToOne
    @JoinColumn(name = "Id_Reserva", nullable = true)
    private Reserva reserva;

    // Relación opcional con Membresía (si creas la entidad Membresía más adelante)
    @Column(name = "Id_Membresia", nullable = true)
    private Long idMembresia;

    @Column(name = "Fecha")
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "Monto_Total", nullable = false)
    private Double montoTotal;

    @Column(name = "Medio_Pago", nullable = false, length = 50)
    private String medioPago; // 'MERCADOPAGO', 'TRANSFERENCIA', 'TARJETA'

    @Column(name = "Estado", length = 20)
    private String estado = "APROBADO";
}