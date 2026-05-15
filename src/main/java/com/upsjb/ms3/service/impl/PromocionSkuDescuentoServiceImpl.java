// ruta: src/main/java/com/upsjb/ms3/service/impl/PromocionSkuDescuentoServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.PrecioSkuHistorial;
import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.Promocion;
import com.upsjb.ms3.domain.entity.PromocionSkuDescuentoVersion;
import com.upsjb.ms3.domain.entity.PromocionVersion;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.EstadoPromocion;
import com.upsjb.ms3.domain.enums.Moneda;
import com.upsjb.ms3.domain.enums.PromocionEventType;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.dto.promocion.filter.PromocionSkuDescuentoFilterDto;
import com.upsjb.ms3.dto.promocion.request.PromocionSkuDescuentoCreateRequestDto;
import com.upsjb.ms3.dto.promocion.request.PromocionSkuDescuentoUpdateRequestDto;
import com.upsjb.ms3.dto.promocion.response.PromocionSkuDescuentoCalculoResponseDto;
import com.upsjb.ms3.dto.promocion.response.PromocionSkuDescuentoResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.MoneyResponseDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.mapper.PromocionSkuDescuentoMapper;
import com.upsjb.ms3.policy.PromocionPolicy;
import com.upsjb.ms3.repository.PromocionSkuDescuentoVersionRepository;
import com.upsjb.ms3.repository.PromocionVersionRepository;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.security.principal.CurrentUserResolver;
import com.upsjb.ms3.service.contract.AuditoriaFuncionalService;
import com.upsjb.ms3.service.contract.PromocionSkuDescuentoService;
import com.upsjb.ms3.service.support.PromocionPricingSupport;
import com.upsjb.ms3.service.support.PromocionSnapshotOutboxSupport;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.pagination.PaginationService;
import com.upsjb.ms3.shared.reference.ProductoSkuReferenceResolver;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.specification.PromocionSkuDescuentoSpecifications;
import com.upsjb.ms3.util.DateTimeUtil;
import com.upsjb.ms3.util.MoneyUtil;
import com.upsjb.ms3.util.StringNormalizer;
import com.upsjb.ms3.validator.PromocionSkuDescuentoValidator;
import com.upsjb.ms3.validator.PromocionVersionValidator;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromocionSkuDescuentoServiceImpl implements PromocionSkuDescuentoService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "idPromocionSkuDescuentoVersion",
            "promocionVersion.idPromocionVersion",
            "promocionVersion.fechaInicio",
            "promocionVersion.fechaFin",
            "promocionVersion.promocion.codigo",
            "promocionVersion.promocion.nombre",
            "sku.codigoSku",
            "sku.barcode",
            "sku.producto.codigoProducto",
            "sku.producto.nombre",
            "tipoDescuento",
            "valorDescuento",
            "precioFinalEstimado",
            "margenEstimado",
            "limiteUnidades",
            "prioridad",
            "estado",
            "createdAt",
            "updatedAt"
    );

    private final PromocionSkuDescuentoVersionRepository promocionSkuDescuentoRepository;
    private final PromocionVersionRepository promocionVersionRepository;

    private final ProductoSkuReferenceResolver productoSkuReferenceResolver;

    private final PromocionSkuDescuentoMapper promocionSkuDescuentoMapper;

    private final PromocionPolicy promocionPolicy;
    private final PromocionVersionValidator promocionVersionValidator;
    private final PromocionSkuDescuentoValidator promocionSkuDescuentoValidator;

    private final PromocionPricingSupport promocionPricingSupport;
    private final PromocionSnapshotOutboxSupport promocionSnapshotOutboxSupport;

    private final CurrentUserResolver currentUserResolver;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final PaginationService paginationService;
    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional
    public ApiResponseDto<PromocionSkuDescuentoResponseDto> agregar(
            Long idPromocionVersion,
            PromocionSkuDescuentoCreateRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        promocionPolicy.ensureCanManageSkuDiscounts(actor);

        PromocionVersion version = findVersionRequired(idPromocionVersion);
        PromocionSkuDescuentoCreateRequestDto normalized = normalizeCreateRequest(request);
        ProductoSku sku = resolveSku(normalized.sku());

        PrecioSkuHistorial precio = promocionPricingSupport.currentPriceRequired(sku);
        BigDecimal costoEstimado = promocionPricingSupport.costoPromedioEstimado(sku);
        BigDecimal precioFinal = promocionPricingSupport.resolvePrecioFinal(
                normalized.tipoDescuento(),
                normalized.valorDescuento(),
                precio.getPrecioVenta()
        );
        BigDecimal margen = promocionPricingSupport.resolveMargen(precioFinal, costoEstimado);

        promocionSkuDescuentoValidator.validateCreate(
                version,
                sku,
                normalized.tipoDescuento(),
                normalized.valorDescuento(),
                precio.getPrecioVenta(),
                costoEstimado,
                normalized.limiteUnidades(),
                normalized.prioridad(),
                promocionSkuDescuentoRepository.existsByPromocionVersion_IdPromocionVersionAndSku_IdSkuAndEstadoTrue(
                        version.getIdPromocionVersion(),
                        sku.getIdSku()
                )
        );

        PromocionSkuDescuentoCreateRequestDto requestWithCalculatedValues = PromocionSkuDescuentoCreateRequestDto.builder()
                .sku(normalized.sku())
                .tipoDescuento(normalized.tipoDescuento())
                .valorDescuento(normalized.valorDescuento())
                .precioFinalEstimado(precioFinal)
                .margenEstimado(margen)
                .limiteUnidades(normalized.limiteUnidades())
                .prioridad(normalized.prioridad())
                .build();

        PromocionSkuDescuentoVersion entity = promocionSkuDescuentoMapper.toEntity(
                requestWithCalculatedValues,
                version,
                sku
        );
        entity.activar();

        PromocionSkuDescuentoVersion saved = promocionSkuDescuentoRepository.saveAndFlush(entity);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.PROMOCION_VERSIONADA,
                EntidadAuditada.PROMOCION_SKU_DESCUENTO_VERSION,
                String.valueOf(saved.getIdPromocionSkuDescuentoVersion()),
                "AGREGAR_DESCUENTO_SKU_PROMOCION",
                "Operación realizada correctamente.",
                auditMetadata(saved, actor, Map.of())
        );

        promocionSnapshotOutboxSupport.registrarSnapshot(
                version.getPromocion(),
                version,
                PromocionEventType.PROMOCION_SNAPSHOT_ACTUALIZADA,
                "PromocionSkuDescuentoService"
        );

        log.info(
                "Descuento de SKU agregado a promoción. idDescuento={}, idPromocionVersion={}, idSku={}, actor={}",
                saved.getIdPromocionSkuDescuentoVersion(),
                version.getIdPromocionVersion(),
                sku.getIdSku(),
                actor.actorLabel()
        );

        return apiResponseFactory.dtoCreated(
                "Operación realizada correctamente.",
                toResponse(saved)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PromocionSkuDescuentoCalculoResponseDto> calcular(
            Long idPromocionVersion,
            PromocionSkuDescuentoCreateRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        promocionPolicy.ensureCanManageSkuDiscounts(actor);

        PromocionVersion version = findVersionRequired(idPromocionVersion);
        PromocionSkuDescuentoCreateRequestDto normalized = normalizeCreateRequest(request);
        ProductoSku sku = resolveSku(normalized.sku());

        PrecioSkuHistorial precio = promocionPricingSupport.currentPriceRequired(sku);
        BigDecimal costoEstimado = promocionPricingSupport.costoPromedioEstimado(sku);
        BigDecimal precioFinal = promocionPricingSupport.resolvePrecioFinal(
                normalized.tipoDescuento(),
                normalized.valorDescuento(),
                precio.getPrecioVenta()
        );
        BigDecimal margen = promocionPricingSupport.resolveMargen(precioFinal, costoEstimado);

        promocionSkuDescuentoValidator.validateCreate(
                version,
                sku,
                normalized.tipoDescuento(),
                normalized.valorDescuento(),
                precio.getPrecioVenta(),
                costoEstimado,
                normalized.limiteUnidades(),
                normalized.prioridad(),
                promocionSkuDescuentoRepository.existsByPromocionVersion_IdPromocionVersionAndSku_IdSkuAndEstadoTrue(
                        version.getIdPromocionVersion(),
                        sku.getIdSku()
                )
        );

        Producto producto = sku.getProducto();
        Moneda moneda = precio.getMoneda() == null ? Moneda.PEN : precio.getMoneda();

        PromocionSkuDescuentoCalculoResponseDto response = PromocionSkuDescuentoCalculoResponseDto.builder()
                .idPromocionVersion(version.getIdPromocionVersion())
                .idSku(sku.getIdSku())
                .codigoSku(sku.getCodigoSku())
                .codigoProducto(producto == null ? null : producto.getCodigoProducto())
                .nombreProducto(producto == null ? null : producto.getNombre())
                .tipoDescuento(normalized.tipoDescuento())
                .valorDescuento(normalized.valorDescuento())
                .precioBase(promocionPricingSupport.toMoney(precio.getPrecioVenta(), moneda))
                .precioFinalEstimado(promocionPricingSupport.toMoney(precioFinal, moneda))
                .margenEstimado(promocionPricingSupport.toMoney(margen, moneda))
                .generaMargenNegativo(margen != null && margen.compareTo(BigDecimal.ZERO) < 0)
                .limiteUnidades(normalized.limiteUnidades())
                .prioridad(normalized.prioridad())
                .build();

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                response
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<PromocionSkuDescuentoResponseDto> actualizar(
            Long idPromocionSkuDescuentoVersion,
            PromocionSkuDescuentoUpdateRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        promocionPolicy.ensureCanManageSkuDiscounts(actor);

        PromocionSkuDescuentoVersion descuento = findDescuentoRequired(idPromocionSkuDescuentoVersion);
        PromocionSkuDescuentoUpdateRequestDto normalized = normalizeUpdateRequest(request);

        PromocionVersion version = descuento.getPromocionVersion();
        ProductoSku sku = descuento.getSku();
        PrecioSkuHistorial precio = promocionPricingSupport.currentPriceRequired(sku);
        BigDecimal costoEstimado = promocionPricingSupport.costoPromedioEstimado(sku);
        BigDecimal precioFinal = promocionPricingSupport.resolvePrecioFinal(
                normalized.tipoDescuento(),
                normalized.valorDescuento(),
                precio.getPrecioVenta()
        );
        BigDecimal margen = promocionPricingSupport.resolveMargen(precioFinal, costoEstimado);

        promocionSkuDescuentoValidator.validateUpdate(
                version,
                normalized.tipoDescuento(),
                normalized.valorDescuento(),
                precio.getPrecioVenta(),
                costoEstimado,
                normalized.limiteUnidades(),
                normalized.prioridad()
        );

        Map<String, Object> before = auditSnapshot(descuento);

        PromocionSkuDescuentoUpdateRequestDto requestWithCalculatedValues = PromocionSkuDescuentoUpdateRequestDto.builder()
                .tipoDescuento(normalized.tipoDescuento())
                .valorDescuento(normalized.valorDescuento())
                .precioFinalEstimado(precioFinal)
                .margenEstimado(margen)
                .limiteUnidades(normalized.limiteUnidades())
                .prioridad(normalized.prioridad())
                .build();

        promocionSkuDescuentoMapper.updateEntity(descuento, requestWithCalculatedValues);

        PromocionSkuDescuentoVersion saved = promocionSkuDescuentoRepository.saveAndFlush(descuento);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.PROMOCION_VERSIONADA,
                EntidadAuditada.PROMOCION_SKU_DESCUENTO_VERSION,
                String.valueOf(saved.getIdPromocionSkuDescuentoVersion()),
                "ACTUALIZAR_DESCUENTO_SKU_PROMOCION",
                "Operación realizada correctamente.",
                auditMetadata(saved, actor, Map.of("before", before))
        );

        promocionSnapshotOutboxSupport.registrarSnapshot(
                version.getPromocion(),
                version,
                PromocionEventType.PROMOCION_SNAPSHOT_ACTUALIZADA,
                "PromocionSkuDescuentoService"
        );

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<PromocionSkuDescuentoResponseDto> inactivar(
            Long idPromocionSkuDescuentoVersion,
            EstadoChangeRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        promocionPolicy.ensureCanManageSkuDiscounts(actor);

        PromocionSkuDescuentoVersion descuento = findDescuentoRequired(idPromocionSkuDescuentoVersion);
        requireEstadoFalse(request);

        promocionSkuDescuentoValidator.validateCanInactivate(descuento);

        PromocionVersion version = descuento.getPromocionVersion();

        descuento.inactivar();

        PromocionSkuDescuentoVersion saved = promocionSkuDescuentoRepository.saveAndFlush(descuento);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.PROMOCION_VERSIONADA,
                EntidadAuditada.PROMOCION_SKU_DESCUENTO_VERSION,
                String.valueOf(saved.getIdPromocionSkuDescuentoVersion()),
                "INACTIVAR_DESCUENTO_SKU_PROMOCION",
                "Operación realizada correctamente.",
                auditMetadata(saved, actor, Map.of("motivo", request.motivo()))
        );

        promocionSnapshotOutboxSupport.registrarSnapshot(
                version.getPromocion(),
                version,
                PromocionEventType.PROMOCION_SNAPSHOT_ACTUALIZADA,
                "PromocionSkuDescuentoService"
        );

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                toResponse(saved)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PromocionSkuDescuentoResponseDto> obtenerDetalle(Long idPromocionSkuDescuentoVersion) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        promocionPolicy.ensureCanViewAdmin(actor);

        PromocionSkuDescuentoVersion descuento = findDescuentoRequired(idPromocionSkuDescuentoVersion);

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                toResponse(descuento)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<PromocionSkuDescuentoResponseDto>> listar(
            PromocionSkuDescuentoFilterDto filter,
            PageRequestDto pageRequest
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        promocionPolicy.ensureCanViewAdmin(actor);

        PageRequestDto safePage = safePageRequest(pageRequest, "prioridad");

        Pageable pageable = paginationService.pageable(
                safePage.page(),
                safePage.size(),
                safePage.sortBy(),
                safePage.sortDirection(),
                ALLOWED_SORT_FIELDS,
                "prioridad"
        );

        PageResponseDto<PromocionSkuDescuentoResponseDto> response = paginationService.toPageResponseDto(
                promocionSkuDescuentoRepository.findAll(
                        PromocionSkuDescuentoSpecifications.fromFilter(filter),
                        pageable
                ),
                this::toResponse
        );

        return apiResponseFactory.dtoOk(
                "Lista obtenida correctamente.",
                response
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<PromocionSkuDescuentoResponseDto>> listarPorVersion(
            Long idPromocionVersion,
            PageRequestDto pageRequest
    ) {
        PromocionVersion version = findVersionRequired(idPromocionVersion);

        PromocionSkuDescuentoFilterDto filter = PromocionSkuDescuentoFilterDto.builder()
                .idPromocionVersion(version.getIdPromocionVersion())
                .estado(Boolean.TRUE)
                .build();

        return listar(filter, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<PromocionSkuDescuentoResponseDto>> listarPorSku(
            EntityReferenceDto skuReference,
            PageRequestDto pageRequest
    ) {
        ProductoSku sku = resolveSku(skuReference);

        PromocionSkuDescuentoFilterDto filter = PromocionSkuDescuentoFilterDto.builder()
                .idSku(sku.getIdSku())
                .estado(Boolean.TRUE)
                .build();

        return listar(filter, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<PromocionSkuDescuentoResponseDto>> listarAplicablesPorSku(EntityReferenceDto skuReference) {
        promocionPolicy.canViewPublic();

        ProductoSku sku = resolveSku(skuReference);

        List<PromocionSkuDescuentoResponseDto> response = promocionSkuDescuentoRepository
                .findDescuentosAplicablesBySkuAt(
                        sku.getIdSku(),
                        List.of(EstadoPromocion.ACTIVA, EstadoPromocion.PROGRAMADA),
                        DateTimeUtil.nowUtc()
                )
                .stream()
                .map(this::toPublicResponse)
                .toList();

        return apiResponseFactory.dtoOk(
                "Lista obtenida correctamente.",
                response
        );
    }

    private PromocionSkuDescuentoCreateRequestDto normalizeCreateRequest(PromocionSkuDescuentoCreateRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "PROMOCION_DESCUENTO_REQUEST_REQUERIDO",
                    "Debe enviar los datos del descuento."
            );
        }

        return PromocionSkuDescuentoCreateRequestDto.builder()
                .sku(request.sku())
                .tipoDescuento(request.tipoDescuento())
                .valorDescuento(MoneyUtil.normalizeNullable(request.valorDescuento()))
                .precioFinalEstimado(MoneyUtil.normalizeNullable(request.precioFinalEstimado()))
                .margenEstimado(MoneyUtil.normalizeNullable(request.margenEstimado()))
                .limiteUnidades(request.limiteUnidades())
                .prioridad(request.prioridad() == null ? 1 : request.prioridad())
                .build();
    }

    private PromocionSkuDescuentoUpdateRequestDto normalizeUpdateRequest(PromocionSkuDescuentoUpdateRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "PROMOCION_DESCUENTO_REQUEST_REQUERIDO",
                    "Debe enviar los datos del descuento."
            );
        }

        return PromocionSkuDescuentoUpdateRequestDto.builder()
                .tipoDescuento(request.tipoDescuento())
                .valorDescuento(MoneyUtil.normalizeNullable(request.valorDescuento()))
                .precioFinalEstimado(MoneyUtil.normalizeNullable(request.precioFinalEstimado()))
                .margenEstimado(MoneyUtil.normalizeNullable(request.margenEstimado()))
                .limiteUnidades(request.limiteUnidades())
                .prioridad(request.prioridad() == null ? 1 : request.prioridad())
                .build();
    }

    private PromocionVersion findVersionRequired(Long idPromocionVersion) {
        if (idPromocionVersion == null) {
            throw new ValidationException(
                    "PROMOCION_VERSION_ID_REQUERIDO",
                    "Debe indicar la versión de promoción solicitada."
            );
        }

        PromocionVersion version = promocionVersionRepository
                .findByIdPromocionVersionAndEstadoTrue(idPromocionVersion)
                .orElseThrow(() -> new NotFoundException(
                        "PROMOCION_VERSION_NO_ENCONTRADA",
                        "No se encontró el registro solicitado."
                ));

        promocionVersionValidator.requireActive(version);
        return version;
    }

    private PromocionSkuDescuentoVersion findDescuentoRequired(Long idPromocionSkuDescuentoVersion) {
        if (idPromocionSkuDescuentoVersion == null) {
            throw new ValidationException(
                    "PROMOCION_DESCUENTO_ID_REQUERIDO",
                    "Debe indicar el descuento solicitado."
            );
        }

        return promocionSkuDescuentoRepository
                .findByIdPromocionSkuDescuentoVersionAndEstadoTrue(idPromocionSkuDescuentoVersion)
                .orElseThrow(() -> new NotFoundException(
                        "PROMOCION_DESCUENTO_NO_ENCONTRADO",
                        "No se encontró el registro solicitado."
                ));
    }

    private ProductoSku resolveSku(EntityReferenceDto reference) {
        if (reference == null) {
            throw new ValidationException(
                    "SKU_REFERENCIA_REQUERIDA",
                    "Debe indicar el SKU."
            );
        }

        return productoSkuReferenceResolver.resolve(
                reference.id(),
                firstText(reference.codigoSku(), reference.codigo()),
                reference.barcode()
        );
    }

    private PromocionSkuDescuentoResponseDto toResponse(PromocionSkuDescuentoVersion descuento) {
        Optional<PrecioSkuHistorial> precio = descuento.getSku() == null
                ? Optional.empty()
                : promocionPricingSupport.currentPriceOptional(descuento.getSku());

        MoneyResponseDto precioBase = precio
                .map(value -> promocionPricingSupport.toMoney(value.getPrecioVenta(), value.getMoneda()))
                .orElse(null);

        Moneda moneda = precio
                .map(PrecioSkuHistorial::getMoneda)
                .orElse(Moneda.PEN);

        return promocionSkuDescuentoMapper.toResponse(descuento, precioBase, moneda);
    }

    private PromocionSkuDescuentoResponseDto toPublicResponse(PromocionSkuDescuentoVersion descuento) {
        Optional<PrecioSkuHistorial> precio = descuento.getSku() == null
                ? Optional.empty()
                : promocionPricingSupport.currentPriceOptional(descuento.getSku());

        MoneyResponseDto precioBase = precio
                .map(value -> promocionPricingSupport.toMoney(value.getPrecioVenta(), value.getMoneda()))
                .orElse(null);

        Moneda moneda = precio
                .map(PrecioSkuHistorial::getMoneda)
                .orElse(Moneda.PEN);

        return promocionSkuDescuentoMapper.toPublicResponse(descuento, precioBase, moneda);
    }

    private Map<String, Object> auditMetadata(
            PromocionSkuDescuentoVersion descuento,
            AuthenticatedUserContext actor,
            Map<String, Object> extra
    ) {
        Map<String, Object> metadata = auditSnapshot(descuento);
        metadata.put("actor", actor.actorLabel());
        metadata.put("idUsuarioMs1", actor.getIdUsuarioMs1());

        if (extra != null && !extra.isEmpty()) {
            metadata.putAll(extra);
        }

        return metadata;
    }

    private Map<String, Object> auditSnapshot(PromocionSkuDescuentoVersion descuento) {
        Map<String, Object> metadata = new LinkedHashMap<>();

        PromocionVersion version = descuento.getPromocionVersion();
        Promocion promocion = version == null ? null : version.getPromocion();
        ProductoSku sku = descuento.getSku();

        metadata.put("idPromocionSkuDescuentoVersion", descuento.getIdPromocionSkuDescuentoVersion());
        metadata.put("idPromocionVersion", version == null ? null : version.getIdPromocionVersion());
        metadata.put("idPromocion", promocion == null ? null : promocion.getIdPromocion());
        metadata.put("codigoPromocion", promocion == null ? null : promocion.getCodigo());
        metadata.put("idSku", sku == null ? null : sku.getIdSku());
        metadata.put("codigoSku", sku == null ? null : sku.getCodigoSku());
        metadata.put("tipoDescuento", descuento.getTipoDescuento() == null ? null : descuento.getTipoDescuento().getCode());
        metadata.put("valorDescuento", descuento.getValorDescuento());
        metadata.put("precioFinalEstimado", descuento.getPrecioFinalEstimado());
        metadata.put("margenEstimado", descuento.getMargenEstimado());
        metadata.put("limiteUnidades", descuento.getLimiteUnidades());
        metadata.put("prioridad", descuento.getPrioridad());
        metadata.put("estado", descuento.getEstado());

        return metadata;
    }

    private PageRequestDto safePageRequest(PageRequestDto pageRequest, String defaultSortBy) {
        if (pageRequest == null) {
            return PageRequestDto.builder()
                    .page(0)
                    .size(20)
                    .sortBy(defaultSortBy)
                    .sortDirection("ASC")
                    .build();
        }

        return pageRequest;
    }

    private void requireEstadoFalse(EstadoChangeRequestDto request) {
        requireMotivo(request);

        if (!Boolean.FALSE.equals(request.estado())) {
            throw new ValidationException(
                    "ESTADO_INACTIVACION_INVALIDO",
                    "Para inactivar el descuento debe enviar estado=false."
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

    private String firstText(String first, String second) {
        return StringNormalizer.hasText(first) ? first : second;
    }
}