package com.upsjb.ms3.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
        name = "categoria_atributo",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_categoria_atributo",
                        columnNames = {
                                "id_categoria",
                                "id_atributo"
                        }
                )
        }
)
public class CategoriaAtributo
        extends AuditableEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "id_categoria_atributo")
    private Long idCategoriaAtributo;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "id_categoria",
            nullable = false
    )
    private Categoria categoria;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "id_atributo",
            nullable = false
    )
    private Atributo atributo;

    @Column(
            name = "requerido",
            nullable = false
    )
    private Boolean requerido = Boolean.FALSE;

    @Column(
            name = "orden",
            nullable = false
    )
    private Integer orden = 0;
}