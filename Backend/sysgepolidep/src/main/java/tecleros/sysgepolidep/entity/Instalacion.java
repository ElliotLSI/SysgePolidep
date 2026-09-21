package tecleros.sysgepolidep.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "instalacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Instalacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id_Instalacion")
    private Long idInstalacion;

    @Column(name = "Nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "Tipo", nullable = false, length = 50)
    private String tipo;

    @Column(name = "Estado", length = 20)
    private String estado = "DISPONIBLE";

    @Column(name = "Tarifa", nullable = false)
    private Double tarifa = 0.00;
}