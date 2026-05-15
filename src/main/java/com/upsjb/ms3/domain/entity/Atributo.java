package com.upsjb.ms3.domain.entity;

import com.upsjb.ms3.domain.enums.TipoDatoAtributo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "atributo")
public class Atributo extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_atributo")
    private Long idAtributo;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 120, columnDefinition = "nvarchar(120)")
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_dato", nullable = false, length = 30)
    private TipoDatoAtributo tipoDato;

    @Column(name = "unidad_medida", length = 30, columnDefinition = "nvarchar(30)")
    private String unidadMedida;

    @Column(name = "requerido", nullable = false)
    private Boolean requerido = Boolean.FALSE;

    @Column(name = "filtrable", nullable = false)
    private Boolean filtrable = Boolean.FALSE;

    @Column(name = "visible_publico", nullable = false)
    private Boolean visiblePublico = Boolean.TRUE;
}