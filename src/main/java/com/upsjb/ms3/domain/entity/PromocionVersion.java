package com.upsjb.ms3.domain.entity;

import com.upsjb.ms3.domain.enums.EstadoPromocion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "promocion_version")
public class PromocionVersion extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_promocion_version")
    private Long idPromocionVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_promocion", nullable = false)
    private Promocion promocion;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_promocion", nullable = false, length = 30)
    private EstadoPromocion estadoPromocion = EstadoPromocion.BORRADOR;

    @Column(name = "visible_publico", nullable = false)
    private Boolean visiblePublico = Boolean.TRUE;

    @Column(name = "vigente", nullable = false)
    private Boolean vigente = Boolean.TRUE;

    @Column(name = "motivo", nullable = false, length = 500, columnDefinition = "nvarchar(500)")
    private String motivo;

    @Column(name = "creado_por_id_usuario_ms1", nullable = false)
    private Long creadoPorIdUsuarioMs1;
}