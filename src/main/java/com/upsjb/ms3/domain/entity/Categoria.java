package com.upsjb.ms3.domain.entity;

import com.upsjb.ms3.domain.value.SlugValue;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "categoria",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_categoria_codigo",
                        columnNames = "codigo"
                ),
                @UniqueConstraint(
                        name = "uk_categoria_slug",
                        columnNames = "slug"
                )
        },
        indexes = {
                @Index(
                        name = "ix_categoria_padre_nombre_estado",
                        columnList = "id_categoria_padre,nombre,estado"
                ),
                @Index(
                        name = "ix_categoria_nivel_orden",
                        columnList = "nivel,orden"
                )
        }
)
public class Categoria extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Long idCategoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria_padre")
    private Categoria categoriaPadre;

    @Column(
            name = "codigo",
            nullable = false,
            length = 50
    )
    private String codigo;

    @Column(
            name = "nombre",
            nullable = false,
            length = 150,
            columnDefinition = "nvarchar(150)"
    )
    private String nombre;

    @Column(
            name = "slug",
            nullable = false,
            length = SlugValue.MAX_LENGTH
    )
    private String slug;

    @Column(
            name = "slug_generado",
            nullable = false
    )
    private Boolean slugGenerado = Boolean.TRUE;

    @Column(
            name = "descripcion",
            length = 500,
            columnDefinition = "nvarchar(500)"
    )
    private String descripcion;

    @Column(
            name = "nivel",
            nullable = false
    )
    private Integer nivel = 1;

    @Column(
            name = "orden",
            nullable = false
    )
    private Integer orden = 0;

    @Column(
            name = "permite_productos",
            nullable = false
    )
    private Boolean permiteProductos = Boolean.FALSE;

    public boolean aceptaProductos() {
        return Boolean.TRUE.equals(
                permiteProductos
        );
    }
}
