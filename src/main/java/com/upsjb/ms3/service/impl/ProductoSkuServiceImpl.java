// ruta: src/main/java/com/upsjb/ms3/service/impl/ProductoSkuServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.Atributo;
import com.upsjb.ms3.domain.entity.Categoria;
import com.upsjb.ms3.domain.entity.Marca;
import com.upsjb.ms3.domain.entity.PrecioSkuHistorial;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoImagenCloudinary;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.PromocionSkuDescuentoVersion;
import com.upsjb.ms3.domain.entity.SkuAtributoValor;
import com.upsjb.ms3.domain.entity.StockSku;
import com.upsjb.ms3.domain.entity.TipoProducto;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.EstadoPromocion;
import com.upsjb.ms3.domain.enums.EstadoSku;
import com.upsjb.ms3.domain.enums.Moneda;
import com.upsjb.ms3.domain.enums.ProductoEventType;
import com.upsjb.ms3.domain.enums.TipoDescuento;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.dto.catalogo.producto.filter.ProductoSkuFilterDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoSkuCreateRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoSkuUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.request.SkuAtributoValorRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoImagenResponseDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoSkuDetailResponseDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoSkuResponseDto;
import com.upsjb.ms3.dto.catalogo.producto.response.SkuAtributoValorResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.MoneyResponseDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.dto.shared.StockResumenResponseDto;
import com.upsjb.ms3.kafka.event.ProductoImagenSnapshotPayload;
import com.upsjb.ms3.kafka.event.ProductoSkuSnapshotPayload;
import com.upsjb.ms3.kafka.event.ProductoSnapshotEvent;
import com.upsjb.ms3.kafka.event.ProductoSnapshotPayload;
import com.upsjb.ms3.mapper.ProductoImagenMapper;
import com.upsjb.ms3.mapper.ProductoSkuMapper;
import com.upsjb.ms3.mapper.SkuAtributoValorMapper;
import com.upsjb.ms3.policy.ProductoSkuPolicy;
import com.upsjb.ms3.repository.PrecioSkuHistorialRepository;
import com.upsjb.ms3.repository.ProductoImagenCloudinaryRepository;
import com.upsjb.ms3.repository.ProductoSkuRepository;
import com.upsjb.ms3.repository.PromocionSkuDescuentoVersionRepository;
import com.upsjb.ms3.repository.ReservaStockRepository;
import com.upsjb.ms3.repository.SkuAtributoValorRepository;
import com.upsjb.ms3.repository.StockSkuRepository;
import com.upsjb.ms3.repository.TipoProductoAtributoRepository;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.security.principal.CurrentUserResolver;
import com.upsjb.ms3.service.contract.AuditoriaFuncionalService;
import com.upsjb.ms3.service.contract.CodigoGeneradorService;
import com.upsjb.ms3.service.contract.EmpleadoInventarioPermisoService;
import com.upsjb.ms3.service.contract.EventoDominioOutboxService;
import com.upsjb.ms3.service.contract.ProductoSkuService;
import com.upsjb.ms3.shared.audit.AuditContext;
import com.upsjb.ms3.shared.audit.AuditContextHolder;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.pagination.PaginationService;
import com.upsjb.ms3.shared.reference.AtributoReferenceResolver;
import com.upsjb.ms3.shared.reference.ProductoReferenceResolver;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.specification.ProductoSkuSpecifications;
import com.upsjb.ms3.util.DateTimeUtil;
import com.upsjb.ms3.util.MoneyUtil;
import com.upsjb.ms3.util.PercentageUtil;
import com.upsjb.ms3.util.StringNormalizer;
import com.upsjb.ms3.validator.AtributoValidator;
import com.upsjb.ms3.validator.ProductoSkuValidator;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoSkuServiceImpl implements ProductoSkuService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "idSku",
            "producto.codigoProducto",
            "producto.nombre",
            "codigoSku",
            "barcode",
            "color",
            "talla",
            "material",
            "modelo",
            "stockMinimo",
            "stockMaximo",
            "pesoGramos",
            "altoCm",
            "anchoCm",
            "largoCm",
            "estadoSku",
            "estado",
            "createdAt",
            "updatedAt"
    );

    private final ProductoSkuRepository productoSkuRepository;
    private final PrecioSkuHistorialRepository precioSkuHistorialRepository;
    private final PromocionSkuDescuentoVersionRepository promocionSkuDescuentoRepository;
    private final StockSkuRepository stockSkuRepository;
    private final ReservaStockRepository reservaStockRepository;
    private final SkuAtributoValorRepository skuAtributoValorRepository;
    private final ProductoImagenCloudinaryRepository productoImagenRepository;
    private final TipoProductoAtributoRepository tipoProductoAtributoRepository;

    private final ProductoReferenceResolver productoReferenceResolver;
    private final AtributoReferenceResolver atributoReferenceResolver;

    private final ProductoSkuMapper productoSkuMapper;
    private final SkuAtributoValorMapper skuAtributoValorMapper;
    private final ProductoImagenMapper productoImagenMapper;

    private final ProductoSkuValidator productoSkuValidator;
    private final AtributoValidator atributoValidator;
    private final ProductoSkuPolicy productoSkuPolicy;

    private final CodigoGeneradorService codigoGeneradorService;
    private final CurrentUserResolver currentUserResolver;
    private final EmpleadoInventarioPermisoService empleadoInventarioPermisoService;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final EventoDominioOutboxService eventoDominioOutboxService;
    private final PaginationService paginationService;
    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional
    public ApiResponseDto<ProductoSkuResponseDto> crear(ProductoSkuCreateRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoSkuPolicy.ensureCanCreate(actor, employeeCanCreateProductBasic(actor));

        ProductoSkuCreateRequestDto normalized = normalizeCreateRequest(request);
        Producto producto = resolveProducto(normalized.producto());

        if (normalized.atributos() != null && !normalized.atributos().isEmpty()) {
            productoSkuPolicy.ensureCanUpdateAttributes(actor, employeeCanUpdateAttributes(actor));
        }

        String codigoSku = codigoGeneradorService.generarCodigoSku();

        productoSkuValidator.validateCreate(
                producto,
                codigoSku,
                normalized.barcode(),
                normalized.stockMinimo(),
                normalized.stockMaximo(),
                normalized.pesoGramos(),
                normalized.altoCm(),
                normalized.anchoCm(),
                normalized.largoCm(),
                productoSkuRepository.existsByCodigoSkuIgnoreCaseAndEstadoTrue(codigoSku),
                hasDuplicatedBarcode(normalized.barcode(), null)
        );

        ProductoSku sku = productoSkuMapper.toEntity(normalized, producto, codigoSku);
        sku.activar();

        ProductoSku saved = productoSkuRepository.saveAndFlush(sku);

        reemplazarAtributos(saved, normalized.atributos());

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.SKU_CREADO,
                EntidadAuditada.PRODUCTO_SKU,
                String.valueOf(saved.getIdSku()),
                "CREAR_SKU",
                "SKU creado correctamente.",
                auditMetadata(saved, actor, Map.of())
        );

        registrarOutboxProducto(saved.getProducto(), ProductoEventType.SKU_SNAPSHOT_CREADO);

        log.info(
                "SKU creado. idSku={}, codigoSku={}, idProducto={}, actor={}",
                saved.getIdSku(),
                saved.getCodigoSku(),
                producto.getIdProducto(),
                actor.actorLabel()
        );

        return apiResponseFactory.dtoCreated(
                "SKU creado correctamente.",
                toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<ProductoSkuResponseDto> actualizar(Long idSku, ProductoSkuUpdateRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();

        ProductoSku sku = findActiveRequired(idSku);
        ProductoSkuUpdateRequestDto normalized = normalizeUpdateRequest(request);

        boolean changingToDiscontinued = normalized.estadoSku() == EstadoSku.DESCONTINUADO;
        if (changingToDiscontinued) {
            productoSkuPolicy.ensureCanDiscontinue(actor);
            productoSkuValidator.validateCanDiscontinue(sku, hasPendingReservations(sku));
        } else {
            productoSkuPolicy.ensureCanUpdate(actor, employeeCanEditProductBasic(actor));
        }

        if (normalized.atributos() != null) {
            productoSkuPolicy.ensureCanUpdateAttributes(actor, employeeCanUpdateAttributes(actor));
        }

        productoSkuValidator.validateUpdate(
                sku,
                normalized.estadoSku(),
                normalized.barcode(),
                normalized.stockMinimo(),
                normalized.stockMaximo(),
                normalized.pesoGramos(),
                normalized.altoCm(),
                normalized.anchoCm(),
                normalized.largoCm(),
                hasDuplicatedBarcode(normalized.barcode(), sku.getIdSku())
        );

        Map<String, Object> before = auditSnapshot(sku);

        productoSkuMapper.updateEntity(sku, normalized);

        ProductoSku saved = productoSkuRepository.saveAndFlush(sku);

        if (normalized.atributos() != null) {
            reemplazarAtributos(saved, normalized.atributos());
        }

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.SKU_ACTUALIZADO,
                EntidadAuditada.PRODUCTO_SKU,
                String.valueOf(saved.getIdSku()),
                "ACTUALIZAR_SKU",
                "SKU actualizado correctamente.",
                auditMetadata(saved, actor, Map.of("before", before))
        );

        registrarOutboxProducto(saved.getProducto(), ProductoEventType.SKU_SNAPSHOT_ACTUALIZADO);

        return apiResponseFactory.dtoOk(
                "SKU actualizado correctamente.",
                toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<ProductoSkuResponseDto> inactivar(Long idSku, EstadoChangeRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoSkuPolicy.ensureCanDeactivate(actor);

        ProductoSku sku = findActiveRequired(idSku);
        requireEstadoFalse(request);

        productoSkuValidator.validateCanDeactivate(
                sku,
                hasStockQuantity(sku),
                hasPendingReservations(sku)
        );

        sku.inactivar();
        sku.setEstadoSku(EstadoSku.INACTIVO);

        ProductoSku saved = productoSkuRepository.saveAndFlush(sku);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.SKU_INACTIVADO,
                EntidadAuditada.PRODUCTO_SKU,
                String.valueOf(saved.getIdSku()),
                "INACTIVAR_SKU",
                "Operación realizada correctamente.",
                auditMetadata(saved, actor, Map.of("motivo", request.motivo()))
        );

        registrarOutboxProducto(saved.getProducto(), ProductoEventType.SKU_SNAPSHOT_INACTIVADO);

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<ProductoSkuResponseDto> descontinuar(Long idSku, EstadoChangeRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoSkuPolicy.ensureCanDiscontinue(actor);

        ProductoSku sku = findActiveRequired(idSku);
        requireMotivo(request);

        productoSkuValidator.validateCanDiscontinue(sku, hasPendingReservations(sku));

        sku.setEstadoSku(EstadoSku.DESCONTINUADO);

        ProductoSku saved = productoSkuRepository.saveAndFlush(sku);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.SKU_INACTIVADO,
                EntidadAuditada.PRODUCTO_SKU,
                String.valueOf(saved.getIdSku()),
                "DESCONTINUAR_SKU",
                "Operación realizada correctamente.",
                auditMetadata(saved, actor, Map.of("motivo", request.motivo()))
        );

        registrarOutboxProducto(saved.getProducto(), ProductoEventType.SKU_SNAPSHOT_INACTIVADO);

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                toResponse(saved)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<ProductoSkuResponseDto> obtenerPorId(Long idSku) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoSkuPolicy.ensureCanViewAdmin(actor);

        ProductoSku sku = findActiveRequired(idSku);

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                toResponse(sku)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<ProductoSkuDetailResponseDto> obtenerDetalle(Long idSku) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoSkuPolicy.ensureCanViewAdmin(actor);

        ProductoSku sku = findActiveRequired(idSku);

        List<StockResumenResponseDto> stocks = stockSkuRepository
                .findBySku_IdSkuAndEstadoTrueOrderByAlmacen_PrincipalDescAlmacen_NombreAsc(sku.getIdSku())
                .stream()
                .map(this::toStockResumen)
                .toList();

        List<SkuAtributoValorResponseDto> atributos = skuAtributoValorRepository
                .findBySku_IdSkuAndEstadoTrueOrderByIdSkuAtributoValorAsc(sku.getIdSku())
                .stream()
                .map(skuAtributoValorMapper::toResponse)
                .toList();

        List<ProductoImagenResponseDto> imagenes = productoImagenRepository
                .findBySku_IdSkuAndEstadoTrueOrderByPrincipalDescOrdenAscIdImagenAsc(sku.getIdSku())
                .stream()
                .map(productoImagenMapper::toResponse)
                .toList();

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                productoSkuMapper.toDetailResponse(
                        sku,
                        currentPriceMoney(sku),
                        currentPromotionPrice(sku),
                        stocks,
                        atributos,
                        imagenes
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<ProductoSkuResponseDto>> listar(
            ProductoSkuFilterDto filter,
            PageRequestDto pageRequest
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoSkuPolicy.ensureCanViewAdmin(actor);

        PageRequestDto safePage = safePageRequest(pageRequest, "updatedAt");

        Pageable pageable = paginationService.pageable(
                safePage.page(),
                safePage.size(),
                safePage.sortBy(),
                safePage.sortDirection(),
                ALLOWED_SORT_FIELDS,
                "updatedAt"
        );

        PageResponseDto<ProductoSkuResponseDto> response = paginationService.toPageResponseDto(
                productoSkuRepository.findAll(ProductoSkuSpecifications.fromFilter(filter), pageable),
                this::toResponse
        );

        return apiResponseFactory.dtoOk(
                "Lista obtenida correctamente.",
                response
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<ProductoSkuResponseDto>> listarPorProducto(
            EntityReferenceDto productoReference,
            PageRequestDto pageRequest
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoSkuPolicy.ensureCanViewAdmin(actor);

        Producto producto = resolveProducto(productoReference);

        ProductoSkuFilterDto filter = ProductoSkuFilterDto.builder()
                .idProducto(producto.getIdProducto())
                .estado(Boolean.TRUE)
                .build();

        PageRequestDto safePage = safePageRequest(pageRequest, "updatedAt");

        Pageable pageable = paginationService.pageable(
                safePage.page(),
                safePage.size(),
                safePage.sortBy(),
                safePage.sortDirection(),
                ALLOWED_SORT_FIELDS,
                "updatedAt"
        );

        PageResponseDto<ProductoSkuResponseDto> response = paginationService.toPageResponseDto(
                productoSkuRepository.findAll(ProductoSkuSpecifications.fromFilter(filter), pageable),
                this::toResponse
        );

        return apiResponseFactory.dtoOk(
                "Lista obtenida correctamente.",
                response
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<ProductoSkuResponseDto>> listarActivosPorProducto(EntityReferenceDto productoReference) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        productoSkuPolicy.ensureCanViewAdmin(actor);

        Producto producto = resolveProducto(productoReference);

        List<ProductoSkuResponseDto> response = productoSkuRepository
                .findByProducto_IdProductoAndEstadoTrueAndEstadoSkuOrderByIdSkuAsc(
                        producto.getIdProducto(),
                        EstadoSku.ACTIVO
                )
                .stream()
                .map(this::toResponse)
                .toList();

        return apiResponseFactory.dtoOk(
                "Lista obtenida correctamente.",
                response
        );
    }

    private ProductoSkuCreateRequestDto normalizeCreateRequest(ProductoSkuCreateRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "SKU_REQUEST_REQUERIDO",
                    "Debe enviar los datos del SKU."
            );
        }

        return ProductoSkuCreateRequestDto.builder()
                .producto(request.producto())
                .barcode(StringNormalizer.truncateOrNull(request.barcode(), 100))
                .color(StringNormalizer.truncateOrNull(request.color(), 80))
                .talla(StringNormalizer.truncateOrNull(request.talla(), 50))
                .material(StringNormalizer.truncateOrNull(request.material(), 120))
                .modelo(StringNormalizer.truncateOrNull(request.modelo(), 120))
                .stockMinimo(request.stockMinimo() == null ? 0 : request.stockMinimo())
                .stockMaximo(request.stockMaximo())
                .pesoGramos(MoneyUtil.normalizeNullable(request.pesoGramos()))
                .altoCm(MoneyUtil.normalizeNullable(request.altoCm()))
                .anchoCm(MoneyUtil.normalizeNullable(request.anchoCm()))
                .largoCm(MoneyUtil.normalizeNullable(request.largoCm()))
                .atributos(request.atributos())
                .build();
    }

    private ProductoSkuUpdateRequestDto normalizeUpdateRequest(ProductoSkuUpdateRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "SKU_REQUEST_REQUERIDO",
                    "Debe enviar los datos del SKU."
            );
        }

        return ProductoSkuUpdateRequestDto.builder()
                .barcode(StringNormalizer.truncateOrNull(request.barcode(), 100))
                .color(StringNormalizer.truncateOrNull(request.color(), 80))
                .talla(StringNormalizer.truncateOrNull(request.talla(), 50))
                .material(StringNormalizer.truncateOrNull(request.material(), 120))
                .modelo(StringNormalizer.truncateOrNull(request.modelo(), 120))
                .stockMinimo(request.stockMinimo() == null ? 0 : request.stockMinimo())
                .stockMaximo(request.stockMaximo())
                .pesoGramos(MoneyUtil.normalizeNullable(request.pesoGramos()))
                .altoCm(MoneyUtil.normalizeNullable(request.altoCm()))
                .anchoCm(MoneyUtil.normalizeNullable(request.anchoCm()))
                .largoCm(MoneyUtil.normalizeNullable(request.largoCm()))
                .estadoSku(request.estadoSku())
                .atributos(request.atributos())
                .build();
    }

    private Producto resolveProducto(EntityReferenceDto reference) {
        if (reference == null) {
            throw new ValidationException(
                    "PRODUCTO_REFERENCIA_REQUERIDA",
                    "Debe indicar el producto del SKU."
            );
        }

        return productoReferenceResolver.resolve(
                reference.id(),
                firstText(reference.codigoProducto(), reference.codigo()),
                reference.slug(),
                reference.nombre()
        );
    }

    private ProductoSku findActiveRequired(Long idSku) {
        if (idSku == null) {
            throw new ValidationException(
                    "SKU_ID_REQUERIDO",
                    "Debe indicar el SKU solicitado."
            );
        }

        ProductoSku sku = productoSkuRepository.findByIdSkuAndEstadoTrue(idSku)
                .orElseThrow(() -> new NotFoundException(
                        "SKU_NO_ENCONTRADO",
                        "No se encontró el registro solicitado."
                ));

        productoSkuValidator.requireActive(sku);
        return sku;
    }

    private void reemplazarAtributos(ProductoSku sku, List<SkuAtributoValorRequestDto> atributosRequest) {
        if (atributosRequest == null) {
            return;
        }

        List<SkuAtributoValor> actuales = skuAtributoValorRepository
                .findBySku_IdSkuAndEstadoTrueOrderByIdSkuAtributoValorAsc(sku.getIdSku());

        actuales.forEach(SkuAtributoValor::inactivar);
        skuAtributoValorRepository.saveAll(actuales);

        Set<Long> atributosProcesados = new LinkedHashSet<>();

        for (SkuAtributoValorRequestDto item : atributosRequest) {
            if (item == null || item.atributo() == null) {
                throw new ValidationException(
                        "SKU_ATRIBUTO_REQUERIDO",
                        "Debe indicar el atributo del SKU."
                );
            }

            Atributo atributo = atributoReferenceResolver.resolve(
                    item.atributo().id(),
                    item.atributo().codigo(),
                    item.atributo().nombre()
            );

            if (!atributosProcesados.add(atributo.getIdAtributo())) {
                throw new ConflictException(
                        "SKU_ATRIBUTO_DUPLICADO",
                        "No se puede registrar el mismo atributo más de una vez para el SKU."
                );
            }

            validarAtributoPermitidoParaProducto(sku, atributo);
            atributoValidator.validateValueByType(
                    atributo,
                    item.valorTexto(),
                    item.valorNumero(),
                    item.valorBoolean(),
                    item.valorFecha()
            );

            SkuAtributoValor nuevo = skuAtributoValorMapper.toEntity(item, sku, atributo);
            nuevo.activar();
            skuAtributoValorRepository.save(nuevo);
        }

        skuAtributoValorRepository.flush();
    }

    private void validarAtributoPermitidoParaProducto(ProductoSku sku, Atributo atributo) {
        Producto producto = sku.getProducto();
        TipoProducto tipoProducto = producto == null ? null : producto.getTipoProducto();

        if (tipoProducto == null || tipoProducto.getIdTipoProducto() == null) {
            throw new ConflictException(
                    "PRODUCTO_SIN_TIPO",
                    "No se puede registrar atributos porque el producto no tiene tipo configurado."
            );
        }

        boolean asociado = tipoProductoAtributoRepository.existsByTipoProducto_IdTipoProductoAndAtributo_IdAtributoAndEstadoTrue(
                tipoProducto.getIdTipoProducto(),
                atributo.getIdAtributo()
        );

        if (!asociado) {
            throw new ConflictException(
                    "SKU_ATRIBUTO_NO_PERMITIDO",
                    "El atributo no está asociado al tipo de producto del SKU."
            );
        }
    }

    private ProductoSkuResponseDto toResponse(ProductoSku sku) {
        return productoSkuMapper.toResponse(
                sku,
                currentPriceMoney(sku),
                currentPromotionPrice(sku),
                stockResumenGlobal(sku)
        );
    }

    private MoneyResponseDto currentPriceMoney(ProductoSku sku) {
        PrecioSkuHistorial precio = currentPrice(sku);
        return precio == null ? null : toMoney(precio.getPrecioVenta(), precio.getMoneda());
    }

    private PrecioSkuHistorial currentPrice(ProductoSku sku) {
        if (sku == null || sku.getIdSku() == null) {
            return null;
        }

        return precioSkuHistorialRepository
                .findFirstBySku_IdSkuAndVigenteTrueAndEstadoTrueOrderByFechaInicioDescIdPrecioHistorialDesc(sku.getIdSku())
                .orElse(null);
    }

    private MoneyResponseDto currentPromotionPrice(ProductoSku sku) {
        PrecioSkuHistorial precio = currentPrice(sku);
        if (precio == null) {
            return null;
        }

        PromocionSkuDescuentoVersion descuento = promocionSkuDescuentoRepository
                .findDescuentosAplicablesBySkuAt(
                        sku.getIdSku(),
                        List.of(EstadoPromocion.ACTIVA, EstadoPromocion.PROGRAMADA),
                        DateTimeUtil.nowUtc()
                )
                .stream()
                .findFirst()
                .orElse(null);

        if (descuento == null) {
            return null;
        }

        BigDecimal precioFinal = resolvePrecioFinal(
                descuento.getTipoDescuento(),
                descuento.getValorDescuento(),
                precio.getPrecioVenta()
        );

        return toMoney(precioFinal, precio.getMoneda());
    }

    private BigDecimal resolvePrecioFinal(
            TipoDescuento tipoDescuento,
            BigDecimal valorDescuento,
            BigDecimal precioBase
    ) {
        if (tipoDescuento == null || valorDescuento == null || precioBase == null) {
            return null;
        }

        if (tipoDescuento.isPorcentaje()) {
            return PercentageUtil.applyDiscount(precioBase, valorDescuento);
        }

        if (tipoDescuento.isMontoFijo()) {
            return MoneyUtil.applyDiscountAmount(precioBase, valorDescuento);
        }

        return MoneyUtil.normalize(valorDescuento);
    }

    private MoneyResponseDto toMoney(BigDecimal amount, Moneda moneda) {
        if (amount == null || moneda == null) {
            return null;
        }

        BigDecimal normalized = MoneyUtil.normalize(amount);

        return MoneyResponseDto.builder()
                .amount(normalized)
                .currency(moneda.getCode())
                .formatted(moneda.getSymbol() + " " + normalized)
                .build();
    }

    private StockResumenResponseDto stockResumenGlobal(ProductoSku sku) {
        Long stockFisico = stockSkuRepository.sumStockFisicoBySku(sku.getIdSku());
        Long stockReservado = stockSkuRepository.sumStockReservadoBySku(sku.getIdSku());
        Long stockDisponible = stockSkuRepository.sumStockDisponibleBySku(sku.getIdSku());

        return StockResumenResponseDto.builder()
                .idSku(sku.getIdSku())
                .codigoSku(sku.getCodigoSku())
                .stockFisico(toInteger(stockFisico))
                .stockReservado(toInteger(stockReservado))
                .stockDisponible(toInteger(stockDisponible))
                .stockMinimo(sku.getStockMinimo())
                .stockMaximo(sku.getStockMaximo())
                .bajoStock(isBajoStock(toInteger(stockDisponible), sku.getStockMinimo()))
                .build();
    }

    private StockResumenResponseDto toStockResumen(StockSku stock) {
        ProductoSku sku = stock.getSku();

        return StockResumenResponseDto.builder()
                .idSku(sku == null ? null : sku.getIdSku())
                .codigoSku(sku == null ? null : sku.getCodigoSku())
                .idAlmacen(stock.getAlmacen() == null ? null : stock.getAlmacen().getIdAlmacen())
                .codigoAlmacen(stock.getAlmacen() == null ? null : stock.getAlmacen().getCodigo())
                .nombreAlmacen(stock.getAlmacen() == null ? null : stock.getAlmacen().getNombre())
                .stockFisico(stock.getStockFisico())
                .stockReservado(stock.getStockReservado())
                .stockDisponible(resolveStockDisponible(stock))
                .stockMinimo(stock.getStockMinimo())
                .stockMaximo(stock.getStockMaximo())
                .bajoStock(isBajoStock(resolveStockDisponible(stock), stock.getStockMinimo()))
                .build();
    }

    private Integer resolveStockDisponible(StockSku stock) {
        if (stock.getStockDisponible() != null) {
            return stock.getStockDisponible();
        }

        int fisico = stock.getStockFisico() == null ? 0 : stock.getStockFisico();
        int reservado = stock.getStockReservado() == null ? 0 : stock.getStockReservado();

        return fisico - reservado;
    }

    private boolean isBajoStock(Integer stockDisponible, Integer stockMinimo) {
        if (stockDisponible == null || stockMinimo == null) {
            return false;
        }

        return stockDisponible <= stockMinimo;
    }

    private boolean hasDuplicatedBarcode(String barcode, Long excludedIdSku) {
        if (!StringNormalizer.hasText(barcode)) {
            return false;
        }

        if (excludedIdSku == null) {
            return productoSkuRepository.existsByBarcodeIgnoreCaseAndEstadoTrue(barcode);
        }

        return productoSkuRepository.existsByBarcodeIgnoreCaseAndEstadoTrueAndIdSkuNot(barcode, excludedIdSku);
    }

    private boolean hasStockQuantity(ProductoSku sku) {
        return toInteger(stockSkuRepository.sumStockFisicoBySku(sku.getIdSku())) > 0
                || toInteger(stockSkuRepository.sumStockReservadoBySku(sku.getIdSku())) > 0;
    }

    private boolean hasPendingReservations(ProductoSku sku) {
        return reservaStockRepository
                .findBySku_IdSkuAndEstadoTrue(sku.getIdSku(), Pageable.unpaged())
                .stream()
                .anyMatch(reserva -> reserva.getEstadoReserva() != null && reserva.getEstadoReserva().isPendiente());
    }

    private void registrarOutboxProducto(Producto producto, ProductoEventType eventType) {
        AuditContext context = AuditContextHolder.getOrEmpty();

        ProductoSnapshotEvent event = ProductoSnapshotEvent.of(
                eventType,
                producto.getIdProducto(),
                traceValue(context.requestId()),
                traceValue(context.correlationId()),
                toProductoSnapshotPayload(producto),
                Map.of("source", "ProductoSkuService")
        );

        eventoDominioOutboxService.registrarEvento(event);
    }

    private ProductoSnapshotPayload toProductoSnapshotPayload(Producto producto) {
        List<ProductoSkuSnapshotPayload> skus = productoSkuRepository
                .findByProducto_IdProductoAndEstadoTrueOrderByIdSkuAsc(producto.getIdProducto())
                .stream()
                .map(this::toSkuSnapshotPayload)
                .toList();

        List<ProductoImagenSnapshotPayload> imagenes = productoImagenRepository
                .findByProducto_IdProductoAndEstadoTrueOrderByPrincipalDescOrdenAscIdImagenAsc(producto.getIdProducto())
                .stream()
                .map(this::toImagenSnapshotPayload)
                .toList();

        TipoProducto tipoProducto = producto.getTipoProducto();
        Categoria categoria = producto.getCategoria();
        Marca marca = producto.getMarca();

        return ProductoSnapshotPayload.builder()
                .idProducto(producto.getIdProducto())
                .codigoProducto(producto.getCodigoProducto())
                .nombre(producto.getNombre())
                .slug(producto.getSlug())
                .idTipoProducto(tipoProducto == null ? null : tipoProducto.getIdTipoProducto())
                .codigoTipoProducto(tipoProducto == null ? null : tipoProducto.getCodigo())
                .nombreTipoProducto(tipoProducto == null ? null : tipoProducto.getNombre())
                .idCategoria(categoria == null ? null : categoria.getIdCategoria())
                .codigoCategoria(categoria == null ? null : categoria.getCodigo())
                .nombreCategoria(categoria == null ? null : categoria.getNombre())
                .slugCategoria(categoria == null ? null : categoria.getSlug())
                .idMarca(marca == null ? null : marca.getIdMarca())
                .codigoMarca(marca == null ? null : marca.getCodigo())
                .nombreMarca(marca == null ? null : marca.getNombre())
                .slugMarca(marca == null ? null : marca.getSlug())
                .descripcionCorta(producto.getDescripcionCorta())
                .descripcionLarga(producto.getDescripcionLarga())
                .generoObjetivo(producto.getGeneroObjetivo() == null ? null : producto.getGeneroObjetivo().getCode())
                .temporada(producto.getTemporada())
                .deporte(producto.getDeporte())
                .estadoRegistro(producto.getEstadoRegistro() == null ? null : producto.getEstadoRegistro().getCode())
                .estadoPublicacion(producto.getEstadoPublicacion() == null ? null : producto.getEstadoPublicacion().getCode())
                .estadoVenta(producto.getEstadoVenta() == null ? null : producto.getEstadoVenta().getCode())
                .visiblePublico(producto.getVisiblePublico())
                .vendible(producto.getVendible())
                .fechaPublicacionInicio(producto.getFechaPublicacionInicio())
                .fechaPublicacionFin(producto.getFechaPublicacionFin())
                .motivoEstado(producto.getMotivoEstado())
                .estado(producto.getEstado())
                .createdAt(producto.getCreatedAt())
                .updatedAt(producto.getUpdatedAt())
                .skus(skus)
                .imagenes(imagenes)
                .build();
    }

    private ProductoSkuSnapshotPayload toSkuSnapshotPayload(ProductoSku sku) {
        return ProductoSkuSnapshotPayload.builder()
                .idSku(sku.getIdSku())
                .idProducto(sku.getProducto() == null ? null : sku.getProducto().getIdProducto())
                .codigoProducto(sku.getProducto() == null ? null : sku.getProducto().getCodigoProducto())
                .codigoSku(sku.getCodigoSku())
                .barcode(sku.getBarcode())
                .color(sku.getColor())
                .talla(sku.getTalla())
                .material(sku.getMaterial())
                .modelo(sku.getModelo())
                .stockMinimo(sku.getStockMinimo())
                .stockMaximo(sku.getStockMaximo())
                .pesoGramos(sku.getPesoGramos())
                .altoCm(sku.getAltoCm())
                .anchoCm(sku.getAnchoCm())
                .largoCm(sku.getLargoCm())
                .estadoSku(sku.getEstadoSku() == null ? null : sku.getEstadoSku().getCode())
                .estado(sku.getEstado())
                .createdAt(sku.getCreatedAt())
                .updatedAt(sku.getUpdatedAt())
                .atributos(toSkuAtributoSnapshotPayloads(sku))
                .build();
    }

    private List<ProductoSkuSnapshotPayload.SkuAtributoSnapshotPayload> toSkuAtributoSnapshotPayloads(ProductoSku sku) {
        if (sku == null || sku.getIdSku() == null) {
            return List.of();
        }

        return skuAtributoValorRepository
                .findBySku_IdSkuAndEstadoTrueOrderByIdSkuAtributoValorAsc(sku.getIdSku())
                .stream()
                .map(this::toSkuAtributoSnapshotPayload)
                .toList();
    }

    private ProductoSkuSnapshotPayload.SkuAtributoSnapshotPayload toSkuAtributoSnapshotPayload(
            SkuAtributoValor valor
    ) {
        Atributo atributo = valor.getAtributo();

        return ProductoSkuSnapshotPayload.SkuAtributoSnapshotPayload.builder()
                .idSkuAtributoValor(valor.getIdSkuAtributoValor())
                .idAtributo(atributo == null ? null : atributo.getIdAtributo())
                .codigoAtributo(atributo == null ? null : atributo.getCodigo())
                .nombreAtributo(atributo == null ? null : atributo.getNombre())
                .tipoDato(atributo == null || atributo.getTipoDato() == null ? null : atributo.getTipoDato().getCode())
                .unidadMedida(atributo == null ? null : atributo.getUnidadMedida())
                .requerido(atributo == null ? null : atributo.getRequerido())
                .filtrable(atributo == null ? null : atributo.getFiltrable())
                .visiblePublico(atributo == null ? null : atributo.getVisiblePublico())
                .valorTexto(valor.getValorTexto())
                .valorNumero(valor.getValorNumero())
                .valorBoolean(valor.getValorBoolean())
                .valorFecha(valor.getValorFecha())
                .estado(valor.getEstado())
                .createdAt(valor.getCreatedAt())
                .updatedAt(valor.getUpdatedAt())
                .build();
    }

    private ProductoImagenSnapshotPayload toImagenSnapshotPayload(ProductoImagenCloudinary imagen) {
        return ProductoImagenSnapshotPayload.builder()
                .idImagen(imagen.getIdImagen())
                .idProducto(imagen.getProducto() == null ? null : imagen.getProducto().getIdProducto())
                .idSku(imagen.getSku() == null ? null : imagen.getSku().getIdSku())
                .cloudinaryAssetId(imagen.getCloudinaryAssetId())
                .cloudinaryPublicId(imagen.getCloudinaryPublicId())
                .cloudinaryVersion(imagen.getCloudinaryVersion())
                .secureUrl(imagen.getSecureUrl())
                .format(imagen.getFormat())
                .resourceType(imagen.getResourceType())
                .width(imagen.getWidth())
                .height(imagen.getHeight())
                .bytes(imagen.getBytes())
                .principal(imagen.getPrincipal())
                .orden(imagen.getOrden())
                .altText(imagen.getAltText())
                .estado(imagen.getEstado())
                .createdAt(imagen.getCreatedAt())
                .updatedAt(imagen.getUpdatedAt())
                .build();
    }

    private Map<String, Object> auditMetadata(
            ProductoSku sku,
            AuthenticatedUserContext actor,
            Map<String, Object> extra
    ) {
        Map<String, Object> metadata = auditSnapshot(sku);
        metadata.put("actor", actor.actorLabel());
        metadata.put("idUsuarioMs1", actor.getIdUsuarioMs1());

        if (extra != null && !extra.isEmpty()) {
            metadata.putAll(extra);
        }

        return metadata;
    }

    private Map<String, Object> auditSnapshot(ProductoSku sku) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        Producto producto = sku.getProducto();

        metadata.put("idSku", sku.getIdSku());
        metadata.put("codigoSku", sku.getCodigoSku());
        metadata.put("barcode", sku.getBarcode());
        metadata.put("idProducto", producto == null ? null : producto.getIdProducto());
        metadata.put("codigoProducto", producto == null ? null : producto.getCodigoProducto());
        metadata.put("estadoSku", sku.getEstadoSku() == null ? null : sku.getEstadoSku().getCode());
        metadata.put("estado", sku.getEstado());

        return metadata;
    }

    private boolean employeeCanCreateProductBasic(AuthenticatedUserContext actor) {
        return actor != null
                && actor.getIdUsuarioMs1() != null
                && empleadoInventarioPermisoService.puedeCrearProductoBasico(actor.getIdUsuarioMs1());
    }

    private boolean employeeCanEditProductBasic(AuthenticatedUserContext actor) {
        return actor != null
                && actor.getIdUsuarioMs1() != null
                && empleadoInventarioPermisoService.puedeEditarProductoBasico(actor.getIdUsuarioMs1());
    }

    private boolean employeeCanUpdateAttributes(AuthenticatedUserContext actor) {
        return actor != null
                && actor.getIdUsuarioMs1() != null
                && empleadoInventarioPermisoService.puedeActualizarAtributos(actor.getIdUsuarioMs1());
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

    private void requireEstadoFalse(EstadoChangeRequestDto request) {
        requireMotivo(request);

        if (!Boolean.FALSE.equals(request.estado())) {
            throw new ValidationException(
                    "ESTADO_INACTIVACION_INVALIDO",
                    "Para inactivar el SKU debe enviar estado=false."
            );
        }
    }

    private void requireMotivo(EstadoChangeRequestDto request) {
        if (request == null || !StringNormalizer.hasText(request.motivo())) {
            throw new ValidationException(
                    "MOTIVO_REQUERIDO",
                    "Debe indicar el motivo de la operación."
            );
        }
    }

    private int toInteger(Long value) {
        if (value == null) {
            return 0;
        }

        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }

        if (value < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }

        return value.intValue();
    }

    private String firstText(String first, String second) {
        return StringNormalizer.hasText(first) ? first : second;
    }

    private String traceValue(String value) {
        return StringNormalizer.hasText(value) ? value : UUID.randomUUID().toString();
    }
}