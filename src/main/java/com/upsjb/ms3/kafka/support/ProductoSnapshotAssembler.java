package com.upsjb.ms3.kafka.support;

import com.upsjb.ms3.domain.entity.Atributo;
import com.upsjb.ms3.domain.entity.Categoria;
import com.upsjb.ms3.domain.entity.CategoriaAtributo;
import com.upsjb.ms3.domain.entity.Marca;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoAtributoValor;
import com.upsjb.ms3.domain.entity.ProductoImagenCloudinary;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.SkuAtributoValor;
import com.upsjb.ms3.kafka.event.ProductoImagenSnapshotPayload;
import com.upsjb.ms3.kafka.event.ProductoSkuSnapshotPayload;
import com.upsjb.ms3.kafka.event.ProductoSnapshotPayload;
import com.upsjb.ms3.repository.CategoriaAtributoRepository;
import com.upsjb.ms3.repository.ProductoAtributoValorRepository;
import com.upsjb.ms3.repository.ProductoImagenCloudinaryRepository;
import com.upsjb.ms3.repository.ProductoRepository;
import com.upsjb.ms3.repository.ProductoSkuRepository;
import com.upsjb.ms3.repository.SkuAtributoValorRepository;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class ProductoSnapshotAssembler {

    private static final String CATEGORY_CODE_SEPARATOR =
            "/";

    private static final String CATEGORY_NAME_SEPARATOR =
            " > ";

    private final ProductoRepository productoRepository;

    private final CategoriaAtributoRepository
            categoriaAtributoRepository;

    private final ProductoAtributoValorRepository
            productoAtributoValorRepository;

    private final ProductoSkuRepository
            productoSkuRepository;

    private final SkuAtributoValorRepository
            skuAtributoValorRepository;

    private final ProductoImagenCloudinaryRepository
            productoImagenRepository;

    @Transactional(readOnly = true)
    public ProductoSnapshotPayload assemble(
            Long idProducto
    ) {
        if (idProducto == null) {
            throw new ValidationException(
                    "PRODUCTO_SNAPSHOT_ID_REQUERIDO",
                    "Debe indicar el producto para construir el snapshot Kafka."
            );
        }

        Producto producto =
                productoRepository
                        .findById(
                                idProducto
                        )
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "PRODUCTO_SNAPSHOT_NO_ENCONTRADO",
                                                "No se encontró el producto para construir el snapshot Kafka."
                                        )
                        );

        return assembleManaged(
                producto
        );
    }

    @Transactional(readOnly = true)
    public ProductoSnapshotPayload assemble(
            Producto producto
    ) {
        if (
                producto == null
                        || producto.getIdProducto() == null
        ) {
            throw new ValidationException(
                    "PRODUCTO_SNAPSHOT_INVALIDO",
                    "El producto del snapshot Kafka es obligatorio."
            );
        }

        Producto managed =
                productoRepository
                        .findById(
                                producto.getIdProducto()
                        )
                        .orElse(producto);

        return assembleManaged(
                managed
        );
    }

    private ProductoSnapshotPayload assembleManaged(
            Producto producto
    ) {
        Categoria categoria =
                producto.getCategoria();

        Marca marca =
                producto.getMarca();

        List<CategoriaAtributo> plantilla =
                loadTemplate(
                        categoria
                );

        Map<Long, CategoriaAtributo> templateByAttributeId =
                plantilla.stream()
                        .filter(
                                item ->
                                        item.getAtributo() != null
                                                && item.getAtributo()
                                                .getIdAtributo() != null
                        )
                        .collect(
                                Collectors.toMap(
                                        item ->
                                                item.getAtributo()
                                                        .getIdAtributo(),
                                        item -> item,
                                        (first, ignored) ->
                                                first,
                                        LinkedHashMap::new
                                )
                        );

        List<ProductoSnapshotPayload.CategoriaRutaSnapshotPayload>
                categoryPath =
                buildCategoryPath(
                        categoria
                );

        List<ProductoSnapshotPayload.ProductoAtributoSnapshotPayload>
                atributos =
                productoAtributoValorRepository
                        .findByProducto_IdProductoAndEstadoTrueOrderByIdProductoAtributoValorAsc(
                                producto.getIdProducto()
                        )
                        .stream()
                        .map(
                                value ->
                                        toProductAttributePayload(
                                                value,
                                                templateByAttributeId
                                        )
                        )
                        .toList();

        List<ProductoSkuSnapshotPayload> skus =
                productoSkuRepository
                        .findByProducto_IdProductoAndEstadoTrueOrderByIdSkuAsc(
                                producto.getIdProducto()
                        )
                        .stream()
                        .map(
                                sku ->
                                        toSkuPayload(
                                                sku,
                                                templateByAttributeId
                                        )
                        )
                        .toList();

        List<ProductoImagenSnapshotPayload> imagenes =
                productoImagenRepository
                        .findByProducto_IdProductoAndEstadoTrueOrderByPrincipalDescOrdenAscIdImagenAsc(
                                producto.getIdProducto()
                        )
                        .stream()
                        .map(
                                this::toImagePayload
                        )
                        .toList();

        Categoria padre =
                categoria == null
                        ? null
                        : categoria.getCategoriaPadre();

        return ProductoSnapshotPayload.builder()
                .snapshotCompleto(
                        Boolean.TRUE
                )
                .snapshotGeneradoAt(
                        LocalDateTime.now()
                )
                .idProducto(
                        producto.getIdProducto()
                )
                .codigoProducto(
                        producto.getCodigoProducto()
                )
                .nombre(
                        producto.getNombre()
                )
                .slug(
                        producto.getSlug()
                )
                .idCategoria(
                        categoryId(
                                categoria
                        )
                )
                .codigoCategoria(
                        categoryCode(
                                categoria
                        )
                )
                .nombreCategoria(
                        categoryName(
                                categoria
                        )
                )
                .slugCategoria(
                        categorySlug(
                                categoria
                        )
                )
                .nivelCategoria(
                        categoria == null
                                ? null
                                : categoria.getNivel()
                )
                .ordenCategoria(
                        categoria == null
                                ? null
                                : categoria.getOrden()
                )
                .categoriaPermiteProductos(
                        categoria == null
                                ? null
                                : categoria.getPermiteProductos()
                )
                .categoriaEstado(
                        categoria == null
                                ? null
                                : categoria.getEstado()
                )
                .idCategoriaPadre(
                        categoryId(
                                padre
                        )
                )
                .codigoCategoriaPadre(
                        categoryCode(
                                padre
                        )
                )
                .nombreCategoriaPadre(
                        categoryName(
                                padre
                        )
                )
                .slugCategoriaPadre(
                        categorySlug(
                                padre
                        )
                )
                .categoriaRutaCodigo(
                        joinPathCodes(
                                categoryPath
                        )
                )
                .categoriaRutaNombre(
                        joinPathNames(
                                categoryPath
                        )
                )
                .categoriaRuta(
                        categoryPath
                )
                .idMarca(
                        marca == null
                                ? null
                                : marca.getIdMarca()
                )
                .codigoMarca(
                        marca == null
                                ? null
                                : marca.getCodigo()
                )
                .nombreMarca(
                        marca == null
                                ? null
                                : marca.getNombre()
                )
                .slugMarca(
                        marca == null
                                ? null
                                : marca.getSlug()
                )
                .marcaEstado(
                        marca == null
                                ? null
                                : marca.getEstado()
                )
                .descripcionCorta(
                        producto.getDescripcionCorta()
                )
                .descripcionLarga(
                        producto.getDescripcionLarga()
                )
                .generoObjetivo(
                        producto.getGeneroObjetivo() == null
                                ? null
                                : producto.getGeneroObjetivo()
                                .getCode()
                )
                .temporada(
                        producto.getTemporada()
                )
                .deporte(
                        producto.getDeporte()
                )
                .estadoRegistro(
                        producto.getEstadoRegistro() == null
                                ? null
                                : producto.getEstadoRegistro()
                                .getCode()
                )
                .estadoPublicacion(
                        producto.getEstadoPublicacion() == null
                                ? null
                                : producto.getEstadoPublicacion()
                                .getCode()
                )
                .estadoVenta(
                        producto.getEstadoVenta() == null
                                ? null
                                : producto.getEstadoVenta()
                                .getCode()
                )
                .visiblePublico(
                        producto.getVisiblePublico()
                )
                .vendible(
                        producto.getVendible()
                )
                .fechaPublicacionInicio(
                        producto.getFechaPublicacionInicio()
                )
                .fechaPublicacionFin(
                        producto.getFechaPublicacionFin()
                )
                .motivoEstado(
                        producto.getMotivoEstado()
                )
                .imagenPrincipalUrl(
                        resolveMainImageUrl(
                                imagenes
                        )
                )
                .estado(
                        producto.getEstado()
                )
                .createdAt(
                        producto.getCreatedAt()
                )
                .updatedAt(
                        producto.getUpdatedAt()
                )
                .plantillaAtributos(
                        plantilla.stream()
                                .map(
                                        this::toTemplateAttributePayload
                                )
                                .toList()
                )
                .atributos(
                        atributos
                )
                .skus(
                        skus
                )
                .imagenes(
                        imagenes
                )
                .build();
    }

    private List<CategoriaAtributo> loadTemplate(
            Categoria categoria
    ) {
        if (
                categoria == null
                        || categoria.getIdCategoria() == null
        ) {
            return List.of();
        }

        return categoriaAtributoRepository
                .findByCategoria_IdCategoriaAndEstadoTrueOrderByOrdenAscIdCategoriaAtributoAsc(
                        categoria.getIdCategoria()
                );
    }

    private List<ProductoSnapshotPayload.CategoriaRutaSnapshotPayload>
    buildCategoryPath(
            Categoria categoria
    ) {
        if (categoria == null) {
            return List.of();
        }

        Deque<ProductoSnapshotPayload.CategoriaRutaSnapshotPayload>
                path =
                new ArrayDeque<>();

        Set<Long> visitedIds =
                new HashSet<>();

        Set<Categoria> visitedInstances =
                java.util.Collections.newSetFromMap(
                        new IdentityHashMap<>()
                );

        Categoria current =
                categoria;

        while (current != null) {
            Long currentId =
                    current.getIdCategoria();

            boolean repeated =
                    currentId == null
                            ? !visitedInstances.add(
                            current
                    )
                            : !visitedIds.add(
                            currentId
                    );

            if (repeated) {
                throw new ValidationException(
                        "CATEGORIA_JERARQUIA_CIRCULAR",
                        "La jerarquía de categorías contiene un ciclo y no puede publicarse en Kafka."
                );
            }

            path.addFirst(
                    toCategoryPathPayload(
                            current
                    )
            );

            current =
                    current.getCategoriaPadre();
        }

        return List.copyOf(
                path
        );
    }

    private ProductoSnapshotPayload.CategoriaRutaSnapshotPayload
    toCategoryPathPayload(
            Categoria categoria
    ) {
        return ProductoSnapshotPayload
                .CategoriaRutaSnapshotPayload
                .builder()
                .idCategoria(
                        categoria.getIdCategoria()
                )
                .codigo(
                        categoria.getCodigo()
                )
                .nombre(
                        categoria.getNombre()
                )
                .slug(
                        categoria.getSlug()
                )
                .nivel(
                        categoria.getNivel()
                )
                .orden(
                        categoria.getOrden()
                )
                .permiteProductos(
                        categoria.getPermiteProductos()
                )
                .estado(
                        categoria.getEstado()
                )
                .build();
    }

    private ProductoSnapshotPayload.CategoriaAtributoSnapshotPayload
    toTemplateAttributePayload(
            CategoriaAtributo relation
    ) {
        Atributo atributo =
                relation.getAtributo();

        Boolean requeridoBase =
                atributo == null
                        ? null
                        : atributo.getRequerido();

        Boolean requeridoCategoria =
                relation.getRequerido();

        return ProductoSnapshotPayload
                .CategoriaAtributoSnapshotPayload
                .builder()
                .idCategoriaAtributo(
                        relation.getIdCategoriaAtributo()
                )
                .idAtributo(
                        attributeId(
                                atributo
                        )
                )
                .codigoAtributo(
                        attributeCode(
                                atributo
                        )
                )
                .nombreAtributo(
                        attributeName(
                                atributo
                        )
                )
                .tipoDato(
                        attributeType(
                                atributo
                        )
                )
                .unidadMedida(
                        atributo == null
                                ? null
                                : atributo.getUnidadMedida()
                )
                .requeridoBase(
                        requeridoBase
                )
                .requeridoCategoria(
                        requeridoCategoria
                )
                .requerido(
                        effectiveRequired(
                                requeridoBase,
                                requeridoCategoria
                        )
                )
                .filtrable(
                        atributo == null
                                ? null
                                : atributo.getFiltrable()
                )
                .visiblePublico(
                        atributo == null
                                ? null
                                : atributo.getVisiblePublico()
                )
                .orden(
                        relation.getOrden()
                )
                .estado(
                        relation.getEstado()
                )
                .build();
    }

    private ProductoSnapshotPayload.ProductoAtributoSnapshotPayload
    toProductAttributePayload(
            ProductoAtributoValor value,
            Map<Long, CategoriaAtributo> templateByAttributeId
    ) {
        Producto producto =
                value.getProducto();

        Atributo atributo =
                value.getAtributo();

        CategoriaAtributo relation =
                templateRelation(
                        atributo,
                        templateByAttributeId
                );

        Boolean requeridoBase =
                atributo == null
                        ? null
                        : atributo.getRequerido();

        Boolean requeridoCategoria =
                relation == null
                        ? null
                        : relation.getRequerido();

        return ProductoSnapshotPayload
                .ProductoAtributoSnapshotPayload
                .builder()
                .idProductoAtributoValor(
                        value.getIdProductoAtributoValor()
                )
                .idProducto(
                        producto == null
                                ? null
                                : producto.getIdProducto()
                )
                .codigoProducto(
                        producto == null
                                ? null
                                : producto.getCodigoProducto()
                )
                .idAtributo(
                        attributeId(
                                atributo
                        )
                )
                .codigoAtributo(
                        attributeCode(
                                atributo
                        )
                )
                .nombreAtributo(
                        attributeName(
                                atributo
                        )
                )
                .tipoDato(
                        attributeType(
                                atributo
                        )
                )
                .unidadMedida(
                        atributo == null
                                ? null
                                : atributo.getUnidadMedida()
                )
                .requeridoBase(
                        requeridoBase
                )
                .requeridoCategoria(
                        requeridoCategoria
                )
                .requerido(
                        effectiveRequired(
                                requeridoBase,
                                requeridoCategoria
                        )
                )
                .filtrable(
                        atributo == null
                                ? null
                                : atributo.getFiltrable()
                )
                .visiblePublico(
                        atributo == null
                                ? null
                                : atributo.getVisiblePublico()
                )
                .valorTexto(
                        value.getValorTexto()
                )
                .valorNumero(
                        value.getValorNumero()
                )
                .valorBoolean(
                        value.getValorBoolean()
                )
                .valorFecha(
                        value.getValorFecha()
                )
                .valorDisplay(
                        valueDisplay(
                                atributo,
                                value.getValorTexto(),
                                value.getValorNumero(),
                                value.getValorBoolean(),
                                value.getValorFecha()
                        )
                )
                .estado(
                        value.getEstado()
                )
                .createdAt(
                        value.getCreatedAt()
                )
                .updatedAt(
                        value.getUpdatedAt()
                )
                .build();
    }

    private ProductoSkuSnapshotPayload toSkuPayload(
            ProductoSku sku,
            Map<Long, CategoriaAtributo> templateByAttributeId
    ) {
        Producto producto =
                sku.getProducto();

        List<ProductoSkuSnapshotPayload.SkuAtributoSnapshotPayload>
                atributos =
                skuAtributoValorRepository
                        .findBySku_IdSkuAndEstadoTrueOrderByIdSkuAtributoValorAsc(
                                sku.getIdSku()
                        )
                        .stream()
                        .map(
                                value ->
                                        toSkuAttributePayload(
                                                value,
                                                templateByAttributeId
                                        )
                        )
                        .toList();

        return ProductoSkuSnapshotPayload.builder()
                .idSku(
                        sku.getIdSku()
                )
                .idProducto(
                        producto == null
                                ? null
                                : producto.getIdProducto()
                )
                .codigoProducto(
                        producto == null
                                ? null
                                : producto.getCodigoProducto()
                )
                .codigoSku(
                        sku.getCodigoSku()
                )
                .barcode(
                        sku.getBarcode()
                )
                .color(
                        sku.getColor()
                )
                .talla(
                        sku.getTalla()
                )
                .material(
                        sku.getMaterial()
                )
                .modelo(
                        sku.getModelo()
                )
                .stockMinimo(
                        sku.getStockMinimo()
                )
                .stockMaximo(
                        sku.getStockMaximo()
                )
                .pesoGramos(
                        sku.getPesoGramos()
                )
                .altoCm(
                        sku.getAltoCm()
                )
                .anchoCm(
                        sku.getAnchoCm()
                )
                .largoCm(
                        sku.getLargoCm()
                )
                .estadoSku(
                        sku.getEstadoSku() == null
                                ? null
                                : sku.getEstadoSku()
                                .getCode()
                )
                .estado(
                        sku.getEstado()
                )
                .createdAt(
                        sku.getCreatedAt()
                )
                .updatedAt(
                        sku.getUpdatedAt()
                )
                .atributos(
                        atributos
                )
                .build();
    }

    private ProductoSkuSnapshotPayload.SkuAtributoSnapshotPayload
    toSkuAttributePayload(
            SkuAtributoValor value,
            Map<Long, CategoriaAtributo> templateByAttributeId
    ) {
        Atributo atributo =
                value.getAtributo();

        CategoriaAtributo relation =
                templateRelation(
                        atributo,
                        templateByAttributeId
                );

        Boolean requeridoBase =
                atributo == null
                        ? null
                        : atributo.getRequerido();

        Boolean requeridoCategoria =
                relation == null
                        ? null
                        : relation.getRequerido();

        return ProductoSkuSnapshotPayload
                .SkuAtributoSnapshotPayload
                .builder()
                .idSkuAtributoValor(
                        value.getIdSkuAtributoValor()
                )
                .idAtributo(
                        attributeId(
                                atributo
                        )
                )
                .codigoAtributo(
                        attributeCode(
                                atributo
                        )
                )
                .nombreAtributo(
                        attributeName(
                                atributo
                        )
                )
                .tipoDato(
                        attributeType(
                                atributo
                        )
                )
                .unidadMedida(
                        atributo == null
                                ? null
                                : atributo.getUnidadMedida()
                )
                .requeridoBase(
                        requeridoBase
                )
                .requeridoCategoria(
                        requeridoCategoria
                )
                .requerido(
                        effectiveRequired(
                                requeridoBase,
                                requeridoCategoria
                        )
                )
                .filtrable(
                        atributo == null
                                ? null
                                : atributo.getFiltrable()
                )
                .visiblePublico(
                        atributo == null
                                ? null
                                : atributo.getVisiblePublico()
                )
                .valorTexto(
                        value.getValorTexto()
                )
                .valorNumero(
                        value.getValorNumero()
                )
                .valorBoolean(
                        value.getValorBoolean()
                )
                .valorFecha(
                        value.getValorFecha()
                )
                .valorDisplay(
                        valueDisplay(
                                atributo,
                                value.getValorTexto(),
                                value.getValorNumero(),
                                value.getValorBoolean(),
                                value.getValorFecha()
                        )
                )
                .estado(
                        value.getEstado()
                )
                .createdAt(
                        value.getCreatedAt()
                )
                .updatedAt(
                        value.getUpdatedAt()
                )
                .build();
    }

    private ProductoImagenSnapshotPayload toImagePayload(
            ProductoImagenCloudinary image
    ) {
        Producto producto =
                image.getProducto();

        ProductoSku sku =
                image.getSku();

        return ProductoImagenSnapshotPayload.builder()
                .idImagen(
                        image.getIdImagen()
                )
                .idProducto(
                        producto == null
                                ? null
                                : producto.getIdProducto()
                )
                .idSku(
                        sku == null
                                ? null
                                : sku.getIdSku()
                )
                .codigoSku(
                        sku == null
                                ? null
                                : sku.getCodigoSku()
                )
                .cloudinaryAssetId(
                        image.getCloudinaryAssetId()
                )
                .cloudinaryPublicId(
                        image.getCloudinaryPublicId()
                )
                .cloudinaryVersion(
                        image.getCloudinaryVersion()
                )
                .secureUrl(
                        image.getSecureUrl()
                )
                .url(
                        image.getUrl()
                )
                .resourceType(
                        image.getResourceType()
                )
                .format(
                        image.getFormat()
                )
                .bytes(
                        image.getBytes()
                )
                .width(
                        image.getWidth()
                )
                .height(
                        image.getHeight()
                )
                .folder(
                        image.getFolder()
                )
                .originalFilename(
                        image.getOriginalFilename()
                )
                .altText(
                        image.getAltText()
                )
                .titulo(
                        image.getTitulo()
                )
                .orden(
                        image.getOrden()
                )
                .principal(
                        image.getPrincipal()
                )
                .estado(
                        image.getEstado()
                )
                .createdAt(
                        image.getCreatedAt()
                )
                .updatedAt(
                        image.getUpdatedAt()
                )
                .build();
    }

    private CategoriaAtributo templateRelation(
            Atributo atributo,
            Map<Long, CategoriaAtributo> templateByAttributeId
    ) {
        return atributo == null
                || atributo.getIdAtributo() == null
                ? null
                : templateByAttributeId.get(
                atributo.getIdAtributo()
        );
    }

    private Boolean effectiveRequired(
            Boolean base,
            Boolean category
    ) {
        return Boolean.TRUE.equals(base)
                || Boolean.TRUE.equals(category);
    }

    private String valueDisplay(
            Atributo atributo,
            String text,
            BigDecimal number,
            Boolean bool,
            LocalDate date
    ) {
        if (
                atributo == null
                        || atributo.getTipoDato() == null
        ) {
            return null;
        }

        return switch (
                atributo.getTipoDato()
                ) {
            case TEXTO ->
                    text;

            case NUMERO, DECIMAL ->
                    number == null
                            ? null
                            : number
                            .stripTrailingZeros()
                            .toPlainString();

            case BOOLEANO ->
                    bool == null
                            ? null
                            : bool.toString();

            case FECHA ->
                    date == null
                            ? null
                            : date.toString();
        };
    }

    private String resolveMainImageUrl(
            List<ProductoImagenSnapshotPayload> images
    ) {
        if (
                images == null
                        || images.isEmpty()
        ) {
            return null;
        }

        return images.stream()
                .filter(
                        image ->
                                image.idSku() == null
                                        && Boolean.TRUE.equals(
                                        image.principal()
                                )
                )
                .map(
                        this::preferredImageUrl
                )
                .filter(
                        StringUtils::hasText
                )
                .findFirst()
                .orElseGet(
                        () ->
                                images.stream()
                                        .filter(
                                                image ->
                                                        Boolean.TRUE.equals(
                                                                image.principal()
                                                        )
                                        )
                                        .map(
                                                this::preferredImageUrl
                                        )
                                        .filter(
                                                StringUtils::hasText
                                        )
                                        .findFirst()
                                        .orElseGet(
                                                () ->
                                                        images.stream()
                                                                .map(
                                                                        this::preferredImageUrl
                                                                )
                                                                .filter(
                                                                        StringUtils::hasText
                                                                )
                                                                .findFirst()
                                                                .orElse(null)
                                        )
                );
    }

    private String preferredImageUrl(
            ProductoImagenSnapshotPayload image
    ) {
        if (image == null) {
            return null;
        }

        if (StringUtils.hasText(
                image.secureUrl()
        )) {
            return image.secureUrl()
                    .trim();
        }

        return StringUtils.hasText(
                image.url()
        )
                ? image.url()
                .trim()
                : null;
    }

    private String joinPathCodes(
            List<ProductoSnapshotPayload.CategoriaRutaSnapshotPayload>
                    path
    ) {
        return path.stream()
                .map(
                        ProductoSnapshotPayload
                                .CategoriaRutaSnapshotPayload
                                ::codigo
                )
                .filter(
                        StringUtils::hasText
                )
                .collect(
                        Collectors.joining(
                                CATEGORY_CODE_SEPARATOR
                        )
                );
    }

    private String joinPathNames(
            List<ProductoSnapshotPayload.CategoriaRutaSnapshotPayload>
                    path
    ) {
        return path.stream()
                .map(
                        ProductoSnapshotPayload
                                .CategoriaRutaSnapshotPayload
                                ::nombre
                )
                .filter(
                        StringUtils::hasText
                )
                .collect(
                        Collectors.joining(
                                CATEGORY_NAME_SEPARATOR
                        )
                );
    }

    private Long categoryId(
            Categoria categoria
    ) {
        return categoria == null
                ? null
                : categoria.getIdCategoria();
    }

    private String categoryCode(
            Categoria categoria
    ) {
        return categoria == null
                ? null
                : categoria.getCodigo();
    }

    private String categoryName(
            Categoria categoria
    ) {
        return categoria == null
                ? null
                : categoria.getNombre();
    }

    private String categorySlug(
            Categoria categoria
    ) {
        return categoria == null
                ? null
                : categoria.getSlug();
    }

    private Long attributeId(
            Atributo atributo
    ) {
        return atributo == null
                ? null
                : atributo.getIdAtributo();
    }

    private String attributeCode(
            Atributo atributo
    ) {
        return atributo == null
                ? null
                : atributo.getCodigo();
    }

    private String attributeName(
            Atributo atributo
    ) {
        return atributo == null
                ? null
                : atributo.getNombre();
    }

    private String attributeType(
            Atributo atributo
    ) {
        return atributo == null
                || atributo.getTipoDato() == null
                ? null
                : atributo.getTipoDato()
                .getCode();
    }
}