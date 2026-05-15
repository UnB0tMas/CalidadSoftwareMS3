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
@Table(name = "producto_imagen_cloudinary")
public class ProductoImagenCloudinary extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_imagen")
    private Long idImagen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sku")
    private ProductoSku sku;

    @Column(name = "cloudinary_asset_id", length = 150)
    private String cloudinaryAssetId;

    @Column(name = "cloudinary_public_id", nullable = false, length = 300)
    private String cloudinaryPublicId;

    @Column(name = "cloudinary_version")
    private Long cloudinaryVersion;

    @Column(name = "secure_url", nullable = false, length = 1000, columnDefinition = "nvarchar(1000)")
    private String secureUrl;

    @Column(name = "url", length = 1000, columnDefinition = "nvarchar(1000)")
    private String url;

    @Column(name = "resource_type", nullable = false, length = 30)
    private String resourceType = "image";

    @Column(name = "format", length = 30)
    private String format;

    @Column(name = "bytes")
    private Long bytes;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "folder", length = 300, columnDefinition = "nvarchar(300)")
    private String folder;

    @Column(name = "original_filename", length = 300, columnDefinition = "nvarchar(300)")
    private String originalFilename;

    @Column(name = "alt_text", length = 250, columnDefinition = "nvarchar(250)")
    private String altText;

    @Column(name = "titulo", length = 180, columnDefinition = "nvarchar(180)")
    private String titulo;

    @Column(name = "orden", nullable = false)
    private Integer orden = 0;

    @Column(name = "principal", nullable = false)
    private Boolean principal = Boolean.FALSE;

    @Column(name = "creado_por_id_usuario_ms1", nullable = false)
    private Long creadoPorIdUsuarioMs1;
}