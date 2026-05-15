package com.upsjb.ms3.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "marca")
public class Marca extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_marca")
    private Long idMarca;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 120, columnDefinition = "nvarchar(120)")
    private String nombre;

    @Column(name = "slug", nullable = false, length = 150)
    private String slug;

    @Column(name = "slug_generado", nullable = false)
    private Boolean slugGenerado = Boolean.TRUE;

    @Column(name = "descripcion", length = 300, columnDefinition = "nvarchar(300)")
    private String descripcion;
}