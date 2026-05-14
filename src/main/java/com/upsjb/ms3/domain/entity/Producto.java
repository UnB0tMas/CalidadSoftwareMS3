package com.upsjb.ms3.domain.entity;

import com.upsjb.ms3.domain.enums.EstadoProductoPublicacion;
import com.upsjb.ms3.domain.enums.EstadoProductoRegistro;
import com.upsjb.ms3.domain.enums.EstadoProductoVenta;
import com.upsjb.ms3.domain.enums.GeneroObjetivo;
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
@Table(name = "producto")
public class Producto extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long idProducto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_producto", nullable = false)
    private TipoProducto tipoProducto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_marca")
    private Marca marca;

    @Column(name = "codigo_producto", nullable = false, length = 80)
    private String codigoProducto;

    @Column(name = "codigo_generado", nullable = false)
    private Boolean codigoGenerado = Boolean.TRUE;

    @Column(name = "nombre", nullable = false, length = 180, columnDefinition = "nvarchar(180)")
    private String nombre;

    @Column(name = "slug", nullable = false, length = 240)
    private String slug;

    @Column(name = "slug_generado", nullable = false)
    private Boolean slugGenerado = Boolean.TRUE;

    @Column(name = "descripcion_corta", length = 500, columnDefinition = "nvarchar(500)")
    private String descripcionCorta;

    @Column(name = "descripcion_larga", columnDefinition = "nvarchar(max)")
    private String descripcionLarga;

    @Enumerated(EnumType.STRING)
    @Column(name = "genero_objetivo", length = 30)
    private GeneroObjetivo generoObjetivo;

    @Column(name = "temporada", length = 80, columnDefinition = "nvarchar(80)")
    private String temporada;

    @Column(name = "deporte", length = 80, columnDefinition = "nvarchar(80)")
    private String deporte;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_registro", nullable = false, length = 30)
    private EstadoProductoRegistro estadoRegistro = EstadoProductoRegistro.BORRADOR;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_publicacion", nullable = false, length = 30)
    private EstadoProductoPublicacion estadoPublicacion = EstadoProductoPublicacion.NO_PUBLICADO;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_venta", nullable = false, length = 30)
    private EstadoProductoVenta estadoVenta = EstadoProductoVenta.NO_VENDIBLE;

    @Column(name = "visible_publico", nullable = false)
    private Boolean visiblePublico = Boolean.FALSE;

    @Column(name = "vendible", nullable = false)
    private Boolean vendible = Boolean.FALSE;

    @Column(name = "fecha_publicacion_inicio")
    private LocalDateTime fechaPublicacionInicio;

    @Column(name = "fecha_publicacion_fin")
    private LocalDateTime fechaPublicacionFin;

    @Column(name = "motivo_estado", length = 500, columnDefinition = "nvarchar(500)")
    private String motivoEstado;

    @Column(name = "creado_por_id_usuario_ms1", nullable = false)
    private Long creadoPorIdUsuarioMs1;

    @Column(name = "actualizado_por_id_usuario_ms1")
    private Long actualizadoPorIdUsuarioMs1;
}