package com.upsjb.ms3.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
        name = "correlativo_codigo",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_correlativo_codigo_entidad",
                        columnNames = "entidad"
                )
        },
        indexes = {
                @Index(
                        name = "ix_correlativo_codigo_entidad_estado",
                        columnList = "entidad,estado"
                )
        }
)
public class CorrelativoCodigo extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_correlativo")
    private Long idCorrelativo;

    @Column(
            name = "entidad",
            nullable = false,
            length = 80
    )
    private String entidad;

    @Column(
            name = "prefijo",
            nullable = false,
            length = 30
    )
    private String prefijo;

    @Column(
            name = "ultimo_numero",
            nullable = false
    )
    private Long ultimoNumero = 0L;

    @Column(
            name = "longitud",
            nullable = false
    )
    private Integer longitud = 6;

    @Column(
            name = "descripcion",
            length = 300,
            columnDefinition = "nvarchar(300)"
    )
    private String descripcion;

    public Long siguienteNumero() {
        if (ultimoNumero == null) {
            ultimoNumero = 0L;
        }

        ultimoNumero++;
        return ultimoNumero;
    }
}
