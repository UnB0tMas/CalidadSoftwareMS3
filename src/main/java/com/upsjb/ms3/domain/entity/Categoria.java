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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "categoria")
public class Categoria extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Long idCategoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria_padre")
    private Categoria categoriaPadre;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 150, columnDefinition = "nvarchar(150)")
    private String nombre;

    @Column(name = "slug", nullable = false, length = 180)
    private String slug;

    @Column(name = "slug_generado", nullable = false)
    private Boolean slugGenerado = Boolean.TRUE;

    @Column(name = "descripcion", length = 500, columnDefinition = "nvarchar(500)")
    private String descripcion;

    @Column(name = "nivel", nullable = false)
    private Integer nivel = 1;

    @Column(name = "orden", nullable = false)
    private Integer orden = 0;
}