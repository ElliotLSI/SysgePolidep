package tecleros.sysgepolidep.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "reserva_a_favor")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaAFavor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id_ReservaAFavor")
    private Long idReservaAFavor;

    @Column(name = "Monto_Acreditado", nullable = false)
    private Double montoAcreditado;

    @Column(name = "Fecha_Generacion", nullable = false)
    private LocalDate fechaGeneracion;

    @Column(name = "Fecha_Vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "Utilizada")
    private Boolean utilizada = false;

    @OneToOne
    @JoinColumn(name = "Id_Reserva_Origen", nullable = false, unique = true)
    private Reserva reservaOrigen;

    @ManyToOne
    @JoinColumn(name = "Id_Usuario", nullable = false)
    private Usuario usuario;
}