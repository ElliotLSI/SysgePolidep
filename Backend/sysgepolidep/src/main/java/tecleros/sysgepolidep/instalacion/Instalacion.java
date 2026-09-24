package tecleros.sysgepolidep.instalacion;

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

    @Column(name = "Nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "Tipo", nullable = false, length = 50)
    private String tipo; // Ej: 'Cancha de Futbol', 'Pileta', etc.

    @Column(name = "Capacidad")
    private Integer capacidad;

    @Column(name = "Tarifa_Base", nullable = false)
    private Double tarifaBase;

    @Column(name = "Estado", length = 30)
    private String estado = "DISPONIBLE"; // 'DISPONIBLE', 'EN MANTENIMIENTO', 'OCUPADA'
}