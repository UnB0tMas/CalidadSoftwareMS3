// ruta: src/main/java/com/upsjb/ms3/service/impl/StockServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.Almacen;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.StockSku;
import com.upsjb.ms3.domain.enums.Moneda;
import com.upsjb.ms3.dto.inventario.stock.filter.StockSkuFilterDto;
import com.upsjb.ms3.dto.inventario.stock.response.StockDisponibleResponseDto;
import com.upsjb.ms3.dto.inventario.stock.response.StockSkuDetailResponseDto;
import com.upsjb.ms3.dto.inventario.stock.response.StockSkuResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.mapper.StockSkuMapper;
import com.upsjb.ms3.policy.StockPolicy;
import com.upsjb.ms3.repository.StockSkuRepository;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.security.principal.CurrentUserResolver;
import com.upsjb.ms3.service.contract.EmpleadoInventarioPermisoService;
import com.upsjb.ms3.service.contract.StockService;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.pagination.PaginationService;
import com.upsjb.ms3.shared.reference.AlmacenReferenceResolver;
import com.upsjb.ms3.shared.reference.ProductoSkuReferenceResolver;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.specification.StockSkuSpecifications;
import com.upsjb.ms3.util.StringNormalizer;
import com.upsjb.ms3.validator.StockValidator;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "idStock",
            "sku.codigoSku",
            "sku.barcode",
            "sku.estadoSku",
            "sku.color",
            "sku.talla",
            "sku.modelo",
            "sku.producto.codigoProducto",
            "sku.producto.nombre",
            "almacen.codigo",
            "almacen.nombre",
            "almacen.principal",
            "stockFisico",
            "stockReservado",
            "stockDisponible",
            "stockMinimo",
            "stockMaximo",
            "estado",
            "createdAt",
            "updatedAt"
    );

    private static final int MAX_SEARCH_LENGTH = 250;
    private static final int MAX_CODIGO_SKU_LENGTH = 100;
    private static final int MAX_BARCODE_LENGTH = 100;
    private static final int MAX_CODIGO_PRODUCTO_LENGTH = 80;
    private static final int MAX_NOMBRE_PRODUCTO_LENGTH = 180;
    private static final int MAX_CODIGO_ALMACEN_LENGTH = 50;
    private static final int MAX_NOMBRE_ALMACEN_LENGTH = 150;

    private final StockSkuRepository stockSkuRepository;
    private final ProductoSkuReferenceResolver productoSkuReferenceResolver;
    private final AlmacenReferenceResolver almacenReferenceResolver;
    private final StockSkuMapper stockSkuMapper;
    private final StockValidator stockValidator;
    private final StockPolicy stockPolicy;
    private final CurrentUserResolver currentUserResolver;
    private final EmpleadoInventarioPermisoService empleadoInventarioPermisoService;
    private final PaginationService paginationService;
    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<StockSkuResponseDto>> listar(
            StockSkuFilterDto filter,
            PageRequestDto pageRequest,
            Boolean incluirCostos
    ) {
        AuthenticatedUserContext actor = resolveActorAndEnsureCanViewInternalStock();

        boolean includeCosts = resolveCostVisibility(actor, incluirCostos);
        PageRequestDto safePage = safePageRequest(pageRequest, "updatedAt");
        StockSkuFilterDto normalizedFilter = normalizeFilter(filter);

        Pageable pageable = paginationService.pageable(
                safePage.page(),
                safePage.size(),
                safePage.sortBy(),
                safePage.sortDirection(),
                ALLOWED_SORT_FIELDS,
                "updatedAt"
        );

        PageResponseDto<StockSkuResponseDto> response = paginationService.toPageResponseDto(
                stockSkuRepository.findAll(StockSkuSpecifications.fromFilter(normalizedFilter), pageable),
                stock -> stockSkuMapper.toResponse(stock, Moneda.PEN, includeCosts)
        );

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<StockSkuDetailResponseDto> obtenerDetalle(
            Long idStock,
            Boolean incluirCostos
    ) {
        AuthenticatedUserContext actor = resolveActorAndEnsureCanViewInternalStock();

        boolean includeCosts = resolveCostVisibility(actor, incluirCostos);
        StockSku stock = findStockRequired(idStock);

        stockValidator.validateStockState(stock);

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                stockSkuMapper.toDetailResponse(stock, Moneda.PEN, includeCosts)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<StockSkuDetailResponseDto> obtenerPorSkuYAlmacen(
            EntityReferenceDto sku,
            EntityReferenceDto almacen,
            Boolean incluirCostos
    ) {
        AuthenticatedUserContext actor = resolveActorAndEnsureCanViewInternalStock();

        boolean includeCosts = resolveCostVisibility(actor, incluirCostos);
        ProductoSku resolvedSku = resolveSku(sku);
        Almacen resolvedAlmacen = resolveAlmacen(almacen);

        StockSku stock = stockSkuRepository
                .findBySku_IdSkuAndAlmacen_IdAlmacenAndEstadoTrue(
                        resolvedSku.getIdSku(),
                        resolvedAlmacen.getIdAlmacen()
                )
                .orElseThrow(this::stockNotFound);

        stockValidator.validateStockState(stock);

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                stockSkuMapper.toDetailResponse(stock, Moneda.PEN, includeCosts)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<StockDisponibleResponseDto> consultarDisponible(
            EntityReferenceDto sku,
            EntityReferenceDto almacen,
            Integer cantidadSolicitada
    ) {
        resolveActorAndEnsureCanViewInternalStock();

        stockValidator.validateAvailableQuery(cantidadSolicitada);

        ProductoSku resolvedSku = resolveSku(sku);
        Almacen resolvedAlmacen = resolveAlmacen(almacen);
        stockValidator.validateAvailabilityTarget(resolvedSku, resolvedAlmacen);

        StockSku stock = stockSkuRepository
                .findBySku_IdSkuAndAlmacen_IdAlmacenAndEstadoTrue(
                        resolvedSku.getIdSku(),
                        resolvedAlmacen.getIdAlmacen()
                )
                .orElse(null);

        if (stock != null) {
            stockValidator.validateStockState(stock);
        }

        StockDisponibleResponseDto response = stock == null
                ? stockSkuMapper.toDisponibleResponse(resolvedSku, resolvedAlmacen, cantidadSolicitada)
                : stockSkuMapper.toDisponibleResponse(stock, cantidadSolicitada);

        return apiResponseFactory.dtoOk(
                "Stock disponible obtenido correctamente.",
                response
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<StockSkuResponseDto>> listarPorSku(
            EntityReferenceDto sku,
            PageRequestDto pageRequest,
            Boolean incluirCostos
    ) {
        resolveActorAndEnsureCanViewInternalStock();

        ProductoSku resolvedSku = resolveSku(sku);

        StockSkuFilterDto filter = StockSkuFilterDto.builder()
                .idSku(resolvedSku.getIdSku())
                .estado(Boolean.TRUE)
                .build();

        return listar(filter, pageRequest, incluirCostos);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<StockSkuResponseDto>> listarPorAlmacen(
            EntityReferenceDto almacen,
            PageRequestDto pageRequest,
            Boolean incluirCostos
    ) {
        resolveActorAndEnsureCanViewInternalStock();

        Almacen resolvedAlmacen = resolveAlmacen(almacen);

        StockSkuFilterDto filter = StockSkuFilterDto.builder()
                .idAlmacen(resolvedAlmacen.getIdAlmacen())
                .estado(Boolean.TRUE)
                .build();

        return listar(filter, pageRequest, incluirCostos);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<StockSkuResponseDto>> listarBajoStock(
            PageRequestDto pageRequest,
            Boolean incluirCostos
    ) {
        resolveActorAndEnsureCanViewInternalStock();

        StockSkuFilterDto filter = StockSkuFilterDto.builder()
                .bajoStock(Boolean.TRUE)
                .estado(Boolean.TRUE)
                .build();

        return listar(filter, pageRequest, incluirCostos);
    }

    @Override
    @Transactional(readOnly = true)
    public Long obtenerStockDisponibleTotalPorSku(Long idSku) {
        if (idSku == null) {
            throw new ValidationException(
                    "SKU_ID_REQUERIDO",
                    "Debe indicar el SKU solicitado."
            );
        }

        Long total = stockSkuRepository.sumStockDisponibleBySku(idSku);
        return total == null ? 0L : total;
    }

    @Override
    @Transactional(readOnly = true)
    public Long obtenerStockDisponibleTotalPorProducto(Long idProducto) {
        if (idProducto == null) {
            throw new ValidationException(
                    "PRODUCTO_ID_REQUERIDO",
                    "Debe indicar el producto solicitado."
            );
        }

        Long total = stockSkuRepository.sumStockDisponibleByProducto(idProducto);
        return total == null ? 0L : total;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeStockDisponible(Long idSku, Long idAlmacen, Integer cantidad) {
        stockValidator.validateRequiredAvailableQuery(cantidad);

        if (idSku == null) {
            throw new ValidationException(
                    "SKU_ID_REQUERIDO",
                    "Debe indicar el SKU solicitado."
            );
        }

        if (idAlmacen == null) {
            return obtenerStockDisponibleTotalPorSku(idSku) >= cantidad.longValue();
        }

        return stockSkuRepository.existsBySku_IdSkuAndAlmacen_IdAlmacenAndEstadoTrueAndStockDisponibleGreaterThanEqual(
                idSku,
                idAlmacen,
                cantidad
        );
    }

    private AuthenticatedUserContext resolveActorAndEnsureCanViewInternalStock() {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();

        stockPolicy.ensureCanViewInternalStock(actor, employeeHasInventoryPermission(actor));

        return actor;
    }

    private StockSku findStockRequired(Long idStock) {
        if (idStock == null) {
            throw new ValidationException(
                    "STOCK_ID_REQUERIDO",
                    "Debe indicar el stock solicitado."
            );
        }

        return stockSkuRepository.findByIdStock(idStock)
                .orElseThrow(this::stockNotFound);
    }

    private ProductoSku resolveSku(EntityReferenceDto reference) {
        if (reference == null) {
            throw new ValidationException(
                    "SKU_REFERENCIA_REQUERIDA",
                    "Debe indicar el SKU solicitado."
            );
        }

        return productoSkuReferenceResolver.resolve(
                reference.id(),
                firstText(reference.codigoSku(), reference.codigo()),
                reference.barcode()
        );
    }

    private Almacen resolveAlmacen(EntityReferenceDto reference) {
        if (reference == null) {
            throw new ValidationException(
                    "ALMACEN_REFERENCIA_REQUERIDA",
                    "Debe indicar el almacén solicitado."
            );
        }

        return almacenReferenceResolver.resolve(
                reference.id(),
                firstText(reference.codigoAlmacen(), reference.codigo()),
                reference.nombre()
        );
    }

    private boolean resolveCostVisibility(AuthenticatedUserContext actor, Boolean incluirCostos) {
        if (Boolean.TRUE.equals(incluirCostos)) {
            stockPolicy.ensureCanViewCost(actor);
            return true;
        }

        if (Boolean.FALSE.equals(incluirCostos)) {
            return false;
        }

        return stockPolicy.canViewCost(actor);
    }

    private StockSkuFilterDto normalizeFilter(StockSkuFilterDto filter) {
        if (filter == null) {
            return null;
        }

        return StockSkuFilterDto.builder()
                .search(StringNormalizer.truncateOrNull(filter.search(), MAX_SEARCH_LENGTH))
                .idSku(filter.idSku())
                .codigoSku(StringNormalizer.truncateOrNull(filter.codigoSku(), MAX_CODIGO_SKU_LENGTH))
                .barcode(StringNormalizer.truncateOrNull(filter.barcode(), MAX_BARCODE_LENGTH))
                .estadoSku(filter.estadoSku())
                .idProducto(filter.idProducto())
                .codigoProducto(StringNormalizer.truncateOrNull(filter.codigoProducto(), MAX_CODIGO_PRODUCTO_LENGTH))
                .nombreProducto(StringNormalizer.truncateOrNull(filter.nombreProducto(), MAX_NOMBRE_PRODUCTO_LENGTH))
                .idAlmacen(filter.idAlmacen())
                .codigoAlmacen(StringNormalizer.truncateOrNull(filter.codigoAlmacen(), MAX_CODIGO_ALMACEN_LENGTH))
                .nombreAlmacen(StringNormalizer.truncateOrNull(filter.nombreAlmacen(), MAX_NOMBRE_ALMACEN_LENGTH))
                .bajoStock(filter.bajoStock())
                .sobreStock(filter.sobreStock())
                .conStockDisponible(filter.conStockDisponible())
                .stockFisicoMin(filter.stockFisicoMin())
                .stockFisicoMax(filter.stockFisicoMax())
                .stockReservadoMin(filter.stockReservadoMin())
                .stockReservadoMax(filter.stockReservadoMax())
                .stockDisponibleMin(filter.stockDisponibleMin())
                .stockDisponibleMax(filter.stockDisponibleMax())
                .stockMinimoMin(filter.stockMinimoMin())
                .stockMinimoMax(filter.stockMinimoMax())
                .stockMaximoMin(filter.stockMaximoMin())
                .stockMaximoMax(filter.stockMaximoMax())
                .estado(filter.estado())
                .incluirTodosLosEstados(Boolean.TRUE.equals(filter.incluirTodosLosEstados()))
                .fechaCreacion(filter.fechaCreacion())
                .fechaActualizacion(filter.fechaActualizacion())
                .build();
    }

    private boolean employeeHasInventoryPermission(AuthenticatedUserContext actor) {
        return actor != null
                && actor.isEmpleado()
                && actor.getIdUsuarioMs1() != null
                && empleadoInventarioPermisoService.tienePermisoVigente(actor.getIdUsuarioMs1());
    }

    private PageRequestDto safePageRequest(PageRequestDto pageRequest, String defaultSortBy) {
        if (pageRequest == null) {
            return PageRequestDto.builder()
                    .page(0)
                    .size(20)
                    .sortBy(defaultSortBy)
                    .sortDirection("DESC")
                    .build();
        }

        return pageRequest;
    }

    private String firstText(String first, String second) {
        return StringNormalizer.hasText(first) ? first : second;
    }

    private NotFoundException stockNotFound() {
        return new NotFoundException(
                "STOCK_SKU_NO_ENCONTRADO",
                "No se encontró el registro solicitado."
        );
    }
}