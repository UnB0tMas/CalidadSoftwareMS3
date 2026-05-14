// ruta: src/main/java/com/upsjb/ms3/service/impl/ProductoPublicServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.PrecioSkuHistorial;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoImagenCloudinary;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.PromocionSkuDescuentoVersion;
import com.upsjb.ms3.domain.enums.EstadoProductoPublicacion;
import com.upsjb.ms3.domain.enums.EstadoProductoRegistro;
import com.upsjb.ms3.domain.enums.EstadoProductoVenta;
import com.upsjb.ms3.domain.enums.EstadoPromocion;
import com.upsjb.ms3.domain.enums.EstadoSku;
import com.upsjb.ms3.domain.enums.Moneda;
import com.upsjb.ms3.domain.enums.TipoDescuento;
import com.upsjb.ms3.dto.catalogo.producto.filter.ProductoPublicFilterDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoAtributoValorResponseDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoImagenResponseDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoPublicDetailResponseDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoPublicResponseDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoSkuResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.MoneyResponseDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.mapper.ProductoAtributoValorMapper;
import com.upsjb.ms3.mapper.ProductoImagenMapper;
import com.upsjb.ms3.mapper.ProductoMapper;
import com.upsjb.ms3.mapper.ProductoSkuMapper;
import com.upsjb.ms3.policy.ProductoPolicy;
import com.upsjb.ms3.repository.PrecioSkuHistorialRepository;
import com.upsjb.ms3.repository.ProductoAtributoValorRepository;
import com.upsjb.ms3.repository.ProductoImagenCloudinaryRepository;
import com.upsjb.ms3.repository.ProductoRepository;
import com.upsjb.ms3.repository.ProductoSkuRepository;
import com.upsjb.ms3.repository.PromocionSkuDescuentoVersionRepository;
import com.upsjb.ms3.repository.StockSkuRepository;
import com.upsjb.ms3.service.contract.ProductoPublicService;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.pagination.PaginationService;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.specification.ProductoPublicSpecifications;
import com.upsjb.ms3.util.MoneyUtil;
import com.upsjb.ms3.util.StringNormalizer;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductoPublicServiceImpl implements ProductoPublicService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "nombre",
            "codigoProducto",
            "slug",
            "categoria.nombre",
            "marca.nombre",
            "generoObjetivo",
            "temporada",
            "deporte",
            "fechaPublicacionInicio",
            "createdAt",
            "updatedAt"
    );

    private final ProductoRepository productoRepository;
    private final ProductoSkuRepository productoSkuRepository;
    private final ProductoImagenCloudinaryRepository productoImagenRepository;
    private final ProductoAtributoValorRepository productoAtributoValorRepository;
    private final PrecioSkuHistorialRepository precioSkuHistorialRepository;
    private final PromocionSkuDescuentoVersionRepository promocionSkuDescuentoRepository;
    private final StockSkuRepository stockSkuRepository;

    private final ProductoMapper productoMapper;
    private final ProductoSkuMapper productoSkuMapper;
    private final ProductoImagenMapper productoImagenMapper;
    private final ProductoAtributoValorMapper productoAtributoValorMapper;
    private final ProductoPolicy productoPolicy;
    private final PaginationService paginationService;
    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<ProductoPublicResponseDto>> listar(
            ProductoPublicFilterDto filter,
            PageRequestDto pageRequest
    ) {
        productoPolicy.canViewPublic();

        PageRequestDto safePage = safePageRequest(pageRequest);
        Pageable pageable = paginationService.pageable(
                safePage.page(),
                safePage.size(),
                safePage.sortBy(),
                safePage.sortDirection(),
                ALLOWED_SORT_FIELDS,
                "fechaPublicacionInicio"
        );

        PageResponseDto<ProductoPublicResponseDto> response = paginationService.toPageResponseDto(
                productoRepository.findAll(ProductoPublicSpecifications.fromFilter(filter), pageable),
                this::toPublicResponse
        );

        return apiResponseFactory.dtoOk(
                "Lista obtenida correctamente.",
                response
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<ProductoPublicDetailResponseDto> obtenerDetallePorSlug(String slug) {
        productoPolicy.canViewPublic();

        if (!StringNormalizer.hasText(slug)) {
            throw new ValidationException(
                    "PRODUCTO_SLUG_REQUERIDO",
                    "Debe indicar el producto solicitado."
            );
        }

        Producto producto = productoRepository.findBySlugIgnoreCaseAndEstadoTrueAndVisiblePublicoTrue(slug.trim())
                .filter(this::isProductoPublicoVisible)
                .orElseThrow(() -> new NotFoundException(
                        "PRODUCTO_PUBLICO_NO_ENCONTRADO",
                        "No se encontró el registro solicitado."
                ));

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                toPublicDetailResponse(producto)
        );
    }

    private ProductoPublicResponseDto toPublicResponse(Producto producto) {
        List<ProductoSku> skus = activeSkus(producto);
        PriceResume priceResume = resolvePriceResume(skus);
        String mainImageUrl = resolveMainImageUrl(producto);

        return productoMapper.toPublicResponse(
                producto,
                mainImageUrl,
                priceResume.precioDesde(),
                priceResume.precioFinalDesde(),
                priceResume.tienePromocion(),
                distinctColors(skus),
                distinctSizes(skus)
        );
    }

    private ProductoPublicDetailResponseDto toPublicDetailResponse(Producto producto) {
        List<ProductoSku> skus = activeSkus(producto);
        PriceResume priceResume = resolvePriceResume(skus);

        List<ProductoSkuResponseDto> skuResponses = skus.stream()
                .map(sku -> {
                    PrecioSkuHistorial precio = currentPrice(sku);
                    PromocionSkuDescuentoVersion descuento = currentDiscount(sku);
                    BigDecimal finalPrice = precio == null
                            ? null
                            : applyDiscount(precio.getPrecioVenta(), descuento);

                    return productoSkuMapper.toResponse(
                            sku,
                            precio == null ? null : toMoney(precio.getPrecioVenta(), precio.getMoneda()),
                            finalPrice == null ? null : toMoney(finalPrice, precio.getMoneda()),
                            null
                    );
                })
                .toList();

        List<ProductoAtributoValorResponseDto> atributos = productoAtributoValorRepository
                .findByProducto_IdProductoAndEstadoTrueOrderByIdProductoAtributoValorAsc(producto.getIdProducto())
                .stream()
                .filter(valor -> valor.getAtributo() != null && Boolean.TRUE.equals(valor.getAtributo().getVisiblePublico()))
                .map(productoAtributoValorMapper::toResponse)
                .toList();

        List<ProductoImagenResponseDto> imagenes = productoImagenRepository
                .findByProducto_IdProductoAndEstadoTrueOrderByPrincipalDescOrdenAscIdImagenAsc(producto.getIdProducto())
                .stream()
                .map(productoImagenMapper::toResponse)
                .toList();

        return productoMapper.toPublicDetailResponse(
                producto,
                priceResume.precioDesde(),
                priceResume.precioFinalDesde(),
                priceResume.tienePromocion(),
                skuResponses,
                atributos,
                imagenes
        );
    }

    private boolean isProductoPublicoVisible(Producto producto) {
        if (producto == null || !Boolean.TRUE.equals(producto.getEstado())) {
            return false;
        }

        if (!Boolean.TRUE.equals(producto.getVisiblePublico())) {
            return false;
        }

        if (producto.getEstadoRegistro() != EstadoProductoRegistro.ACTIVO) {
            return false;
        }

        if (producto.getEstadoPublicacion() != EstadoProductoPublicacion.PUBLICADO
                && producto.getEstadoPublicacion() != EstadoProductoPublicacion.PROGRAMADO) {
            return false;
        }

        if (producto.getEstadoVenta() == null) {
            return false;
        }

        return producto.getEstadoVenta() == EstadoProductoVenta.VENDIBLE
                || producto.getEstadoVenta() == EstadoProductoVenta.SOLO_VISIBLE
                || producto.getEstadoVenta() == EstadoProductoVenta.AGOTADO
                || producto.getEstadoVenta() == EstadoProductoVenta.PROXIMAMENTE;
    }

    private List<ProductoSku> activeSkus(Producto producto) {
        if (producto == null || producto.getIdProducto() == null) {
            return List.of();
        }

        return productoSkuRepository
                .findByProducto_IdProductoAndEstadoTrueAndEstadoSkuOrderByIdSkuAsc(
                        producto.getIdProducto(),
                        EstadoSku.ACTIVO
                );
    }

    private String resolveMainImageUrl(Producto producto) {
        if (producto == null || producto.getIdProducto() == null) {
            return null;
        }

        return productoImagenRepository
                .findFirstByProducto_IdProductoAndSkuIsNullAndPrincipalTrueAndEstadoTrueOrderByOrdenAscIdImagenAsc(
                        producto.getIdProducto()
                )
                .map(ProductoImagenCloudinary::getSecureUrl)
                .orElseGet(() -> productoImagenRepository
                        .findByProducto_IdProductoAndEstadoTrueOrderByPrincipalDescOrdenAscIdImagenAsc(producto.getIdProducto())
                        .stream()
                        .map(ProductoImagenCloudinary::getSecureUrl)
                        .filter(StringNormalizer::hasText)
                        .findFirst()
                        .orElse(null));
    }

    private PriceResume resolvePriceResume(List<ProductoSku> skus) {
        if (skus == null || skus.isEmpty()) {
            return PriceResume.empty();
        }

        List<PriceCandidate> candidates = skus.stream()
                .map(sku -> {
                    PrecioSkuHistorial precio = currentPrice(sku);

                    if (precio == null || precio.getPrecioVenta() == null) {
                        return null;
                    }

                    PromocionSkuDescuentoVersion descuento = currentDiscount(sku);
                    BigDecimal finalPrice = applyDiscount(precio.getPrecioVenta(), descuento);

                    return PriceCandidate.builder()
                            .precio(precio.getPrecioVenta())
                            .precioFinal(finalPrice)
                            .moneda(precio.getMoneda())
                            .tienePromocion(descuento != null && finalPrice != null
                                    && finalPrice.compareTo(precio.getPrecioVenta()) < 0)
                            .build();
                })
                .filter(candidate -> candidate != null && candidate.precio() != null)
                .toList();

        if (candidates.isEmpty()) {
            return PriceResume.empty();
        }

        PriceCandidate minBase = candidates.stream()
                .min(Comparator.comparing(PriceCandidate::precio))
                .orElse(null);

        PriceCandidate minFinal = candidates.stream()
                .filter(candidate -> candidate.precioFinal() != null)
                .min(Comparator.comparing(PriceCandidate::precioFinal))
                .orElse(minBase);

        boolean hasPromotion = candidates.stream().anyMatch(PriceCandidate::tienePromocion);

        Moneda monedaBase = minBase == null || minBase.moneda() == null ? Moneda.PEN : minBase.moneda();
        Moneda monedaFinal = minFinal == null || minFinal.moneda() == null ? monedaBase : minFinal.moneda();

        return PriceResume.builder()
                .precioDesde(minBase == null ? null : toMoney(minBase.precio(), monedaBase))
                .precioFinalDesde(minFinal == null ? null : toMoney(minFinal.precioFinal(), monedaFinal))
                .tienePromocion(hasPromotion)
                .build();
    }

    private PrecioSkuHistorial currentPrice(ProductoSku sku) {
        if (sku == null || sku.getIdSku() == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();

        return precioSkuHistorialRepository
                .findPreciosAplicablesBySkuAndFecha(sku.getIdSku(), now)
                .stream()
                .filter(precio -> Boolean.TRUE.equals(precio.getVigente()))
                .findFirst()
                .orElse(null);
    }

    private PromocionSkuDescuentoVersion currentDiscount(ProductoSku sku) {
        if (sku == null || sku.getIdSku() == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();

        return promocionSkuDescuentoRepository
                .findDescuentosAplicablesBySkuAt(
                        sku.getIdSku(),
                        List.of(EstadoPromocion.ACTIVA, EstadoPromocion.PROGRAMADA),
                        now
                )
                .stream()
                .findFirst()
                .orElse(null);
    }

    private BigDecimal applyDiscount(BigDecimal precioBase, PromocionSkuDescuentoVersion descuento) {
        if (precioBase == null) {
            return null;
        }

        if (descuento == null || descuento.getTipoDescuento() == null || descuento.getValorDescuento() == null) {
            return MoneyUtil.normalize(precioBase);
        }

        if (descuento.getPrecioFinalEstimado() != null) {
            return MoneyUtil.normalize(descuento.getPrecioFinalEstimado().max(BigDecimal.ZERO));
        }

        TipoDescuento tipo = descuento.getTipoDescuento();

        if (tipo.isPorcentaje()) {
            return MoneyUtil.applyPercentageDiscount(precioBase, descuento.getValorDescuento());
        }

        if (tipo.isMontoFijo()) {
            return MoneyUtil.applyDiscountAmount(precioBase, descuento.getValorDescuento());
        }

        if (tipo.isPrecioFinal()) {
            return MoneyUtil.normalize(descuento.getValorDescuento().max(BigDecimal.ZERO));
        }

        return MoneyUtil.normalize(precioBase);
    }

    private MoneyResponseDto toMoney(BigDecimal amount, Moneda moneda) {
        if (amount == null) {
            return null;
        }

        Moneda safeMoneda = moneda == null ? Moneda.PEN : moneda;
        BigDecimal normalized = MoneyUtil.normalize(amount);

        return MoneyResponseDto.builder()
                .amount(normalized)
                .currency(safeMoneda.getCode())
                .formatted(safeMoneda.getSymbol() + " " + normalized)
                .build();
    }

    private List<String> distinctColors(List<ProductoSku> skus) {
        if (skus == null || skus.isEmpty()) {
            return List.of();
        }

        return skus.stream()
                .map(ProductoSku::getColor)
                .filter(StringNormalizer::hasText)
                .map(String::trim)
                .distinct()
                .toList();
    }

    private List<String> distinctSizes(List<ProductoSku> skus) {
        if (skus == null || skus.isEmpty()) {
            return List.of();
        }

        return skus.stream()
                .map(ProductoSku::getTalla)
                .filter(StringNormalizer::hasText)
                .map(String::trim)
                .distinct()
                .toList();
    }

    private PageRequestDto safePageRequest(PageRequestDto pageRequest) {
        if (pageRequest == null) {
            return PageRequestDto.builder()
                    .page(0)
                    .size(20)
                    .sortBy("fechaPublicacionInicio")
                    .sortDirection("DESC")
                    .build();
        }

        return pageRequest;
    }

    @Builder
    private record PriceCandidate(
            BigDecimal precio,
            BigDecimal precioFinal,
            Moneda moneda,
            boolean tienePromocion
    ) {
    }

    @Builder
    private record PriceResume(
            MoneyResponseDto precioDesde,
            MoneyResponseDto precioFinalDesde,
            Boolean tienePromocion
    ) {

        private static PriceResume empty() {
            return PriceResume.builder()
                    .precioDesde(null)
                    .precioFinalDesde(null)
                    .tienePromocion(Boolean.FALSE)
                    .build();
        }
    }
}