package tecleros.sysgepolidep.categoria;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "categoria")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id_Categoria")
    private Long idCategoria;

    @Column(name = "Nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(name = "Costo", nullable = false)
    private Double costo = 0.00;

    @Column(name = "PorcDescuento")
    private Double porcDescuento = 0.00;

    @Column(name = "DescripBeneficios", columnDefinition = "TEXT")
    private String descripBeneficios;

    @Column(name = "Activo")
    private Boolean activo = true;
}