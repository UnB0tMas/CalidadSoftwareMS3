// ruta: src/main/java/com/upsjb/ms3/service/impl/PromocionVersionServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.PrecioSkuHistorial;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.Promocion;
import com.upsjb.ms3.domain.entity.PromocionSkuDescuentoVersion;
import com.upsjb.ms3.domain.entity.PromocionVersion;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.EstadoPromocion;
import com.upsjb.ms3.domain.enums.Moneda;
import com.upsjb.ms3.domain.enums.PromocionEventType;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.dto.promocion.filter.PromocionVersionFilterDto;
import com.upsjb.ms3.dto.promocion.request.PromocionSkuDescuentoCreateRequestDto;
import com.upsjb.ms3.dto.promocion.request.PromocionVersionCreateRequestDto;
import com.upsjb.ms3.dto.promocion.request.PromocionVersionEstadoRequestDto;
import com.upsjb.ms3.dto.promocion.response.PromocionPublicResponseDto;
import com.upsjb.ms3.dto.promocion.response.PromocionSkuDescuentoResponseDto;
import com.upsjb.ms3.dto.promocion.response.PromocionVersionResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.MoneyResponseDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.mapper.PromocionSkuDescuentoMapper;
import com.upsjb.ms3.mapper.PromocionVersionMapper;
import com.upsjb.ms3.policy.PromocionPolicy;
import com.upsjb.ms3.repository.PromocionSkuDescuentoVersionRepository;
import com.upsjb.ms3.repository.PromocionVersionRepository;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.security.principal.CurrentUserResolver;
import com.upsjb.ms3.service.contract.AuditoriaFuncionalService;
import com.upsjb.ms3.service.contract.PromocionVersionService;
import com.upsjb.ms3.service.support.PromocionPricingSupport;
import com.upsjb.ms3.service.support.PromocionSnapshotOutboxSupport;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.pagination.PaginationService;
import com.upsjb.ms3.shared.reference.ProductoSkuReferenceResolver;
import com.upsjb.ms3.shared.reference.PromocionReferenceResolver;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.specification.PromocionVersionSpecifications;
import com.upsjb.ms3.util.DateTimeUtil;
import com.upsjb.ms3.util.MoneyUtil;
import com.upsjb.ms3.util.StringNormalizer;
import com.upsjb.ms3.validator.PromocionSkuDescuentoValidator;
import com.upsjb.ms3.validator.PromocionVersionValidator;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromocionVersionServiceImpl implements PromocionVersionService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "idPromocionVersion",
            "promocion.codigo",
            "promocion.nombre",
            "fechaInicio",
            "fechaFin",
            "estadoPromocion",
            "visiblePublico",
            "vigente",
            "creadoPorIdUsuarioMs1",
            "estado",
            "createdAt",
            "updatedAt"
    );

    private final PromocionVersionRepository promocionVersionRepository;
    private final PromocionSkuDescuentoVersionRepository promocionSkuDescuentoRepository;

    private final PromocionReferenceResolver promocionReferenceResolver;
    private final ProductoSkuReferenceResolver productoSkuReferenceResolver;

    private final PromocionVersionMapper promocionVersionMapper;
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
    public ApiResponseDto<PromocionVersionResponseDto> crear(PromocionVersionCreateRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        promocionPolicy.ensureCanCreateVersion(actor);

        PromocionVersionCreateRequestDto normalized = normalizeCreateRequest(request);
        Promocion promocion = resolvePromocion(normalized.promocion());

        PromocionVersion current = currentVersion(promocion);
        Long idCurrent = current == null ? null : current.getIdPromocionVersion();

        boolean hasOverlappingVersion = !promocionVersionRepository
                .findVersionesSolapadas(
                        promocion.getIdPromocion(),
                        normalized.fechaInicio(),
                        normalized.fechaFin(),
                        idCurrent
                )
                .isEmpty();

        promocionVersionValidator.validateCreate(
                promocion,
                normalized.fechaInicio(),
                normalized.fechaFin(),
                normalized.estadoPromocion(),
                normalized.motivo(),
                actor.getIdUsuarioMs1(),
                false,
                hasOverlappingVersion
        );

        closeCurrentVersion(current, "Reemplazada por nueva versión: " + normalized.motivo());

        PromocionVersion version = promocionVersionMapper.toEntity(
                normalized,
                promocion,
                actor.getIdUsuarioMs1()
        );
        version.activar();

        PromocionVersion saved = promocionVersionRepository.saveAndFlush(version);

        if (normalized.descuentos() != null) {
            for (PromocionSkuDescuentoCreateRequestDto descuentoRequest : normalized.descuentos()) {
                crearDescuentoInterno(saved, descuentoRequest);
            }
        }

        if (saved.getEstadoPromocion() == EstadoPromocion.ACTIVA
                || saved.getEstadoPromocion() == EstadoPromocion.PROGRAMADA) {
            promocionVersionValidator.validateCanActivate(saved, hasDiscounts(saved));
        }

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.PROMOCION_VERSIONADA,
                EntidadAuditada.PROMOCION_VERSION,
                String.valueOf(saved.getIdPromocionVersion()),
                "CREAR_VERSION_PROMOCION",
                "Promoción versionada correctamente.",
                versionAuditMetadata(saved, actor, Map.of())
        );

        promocionSnapshotOutboxSupport.registrarSnapshot(
                promocion,
                saved,
                PromocionEventType.PROMOCION_SNAPSHOT_ACTUALIZADA,
                "PromocionVersionService"
        );

        log.info(
                "Versión de promoción creada. idPromocionVersion={}, idPromocion={}, actor={}",
                saved.getIdPromocionVersion(),
                promocion.getIdPromocion(),
                actor.actorLabel()
        );

        return apiResponseFactory.dtoCreated(
                "Promoción versionada correctamente.",
                toVersionResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<PromocionVersionResponseDto> cambiarEstado(
            Long idPromocionVersion,
            PromocionVersionEstadoRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        promocionPolicy.ensureCanChangeVersionState(actor);

        PromocionVersion version = findVersionRequired(idPromocionVersion);
        PromocionVersionEstadoRequestDto normalized = normalizeEstadoRequest(request);

        if (normalized.estadoPromocion() == EstadoPromocion.CANCELADA) {
            return cancelar(
                    idPromocionVersion,
                    EstadoChangeRequestDto.builder()
                            .estado(Boolean.FALSE)
                            .motivo(normalized.motivo())
                            .build()
            );
        }

        if (normalized.estadoPromocion() == EstadoPromocion.ACTIVA
                || normalized.estadoPromocion() == EstadoPromocion.PROGRAMADA) {
            promocionVersionValidator.validateCanActivate(version, hasDiscounts(version));
            closeOtherCurrentVersion(version);
            version.setVigente(Boolean.TRUE);
        }

        if (normalized.estadoPromocion() == EstadoPromocion.FINALIZADA) {
            version.setVigente(Boolean.FALSE);
            version.setVisiblePublico(Boolean.FALSE);
        }

        Map<String, Object> before = versionAuditSnapshot(version);

        promocionVersionMapper.applyEstado(version, normalized);

        PromocionVersion saved = promocionVersionRepository.saveAndFlush(version);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.PROMOCION_VERSIONADA,
                EntidadAuditada.PROMOCION_VERSION,
                String.valueOf(saved.getIdPromocionVersion()),
                "CAMBIAR_ESTADO_PROMOCION_VERSION",
                "Operación realizada correctamente.",
                versionAuditMetadata(saved, actor, Map.of("before", before))
        );

        PromocionEventType eventType = saved.getEstadoPromocion() == EstadoPromocion.FINALIZADA
                ? PromocionEventType.PROMOCION_SNAPSHOT_FINALIZADA
                : PromocionEventType.PROMOCION_SNAPSHOT_ACTUALIZADA;

        promocionSnapshotOutboxSupport.registrarSnapshot(
                saved.getPromocion(),
                saved,
                eventType,
                "PromocionVersionService"
        );

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                toVersionResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<PromocionVersionResponseDto> cancelar(
            Long idPromocionVersion,
            EstadoChangeRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        promocionPolicy.ensureCanCancel(actor);

        PromocionVersion version = findVersionRequired(idPromocionVersion);
        requireMotivo(request);

        promocionVersionValidator.validateCanCancel(version, request.motivo());

        version.setEstadoPromocion(EstadoPromocion.CANCELADA);
        version.setVisiblePublico(Boolean.FALSE);
        version.setVigente(Boolean.FALSE);
        version.setMotivo(StringNormalizer.truncateOrNull(request.motivo(), 500));

        PromocionVersion saved = promocionVersionRepository.saveAndFlush(version);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.PROMOCION_CANCELADA,
                EntidadAuditada.PROMOCION_VERSION,
                String.valueOf(saved.getIdPromocionVersion()),
                "CANCELAR_PROMOCION_VERSION",
                "Operación realizada correctamente.",
                versionAuditMetadata(saved, actor, Map.of("motivo", request.motivo()))
        );

        promocionSnapshotOutboxSupport.registrarSnapshot(
                saved.getPromocion(),
                saved,
                PromocionEventType.PROMOCION_SNAPSHOT_CANCELADA,
                "PromocionVersionService"
        );

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                toVersionResponse(saved)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PromocionVersionResponseDto> obtenerDetalle(Long idPromocionVersion) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        promocionPolicy.ensureCanViewAdmin(actor);

        PromocionVersion version = findVersionRequired(idPromocionVersion);

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                toVersionResponse(version)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<PromocionVersionResponseDto>> listar(
            PromocionVersionFilterDto filter,
            PageRequestDto pageRequest
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        promocionPolicy.ensureCanViewAdmin(actor);

        PageRequestDto safePage = safePageRequest(pageRequest, "fechaInicio");

        Pageable pageable = paginationService.pageable(
                safePage.page(),
                safePage.size(),
                safePage.sortBy(),
                safePage.sortDirection(),
                ALLOWED_SORT_FIELDS,
                "fechaInicio"
        );

        PageResponseDto<PromocionVersionResponseDto> response = paginationService.toPageResponseDto(
                promocionVersionRepository.findAll(PromocionVersionSpecifications.fromFilter(filter), pageable),
                this::toVersionResponse
        );

        return apiResponseFactory.dtoOk(
                "Lista obtenida correctamente.",
                response
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<PromocionVersionResponseDto>> listarPorPromocion(
            EntityReferenceDto promocionReference,
            PageRequestDto pageRequest
    ) {
        Promocion promocion = resolvePromocion(promocionReference);

        PromocionVersionFilterDto filter = PromocionVersionFilterDto.builder()
                .idPromocion(promocion.getIdPromocion())
                .estado(Boolean.TRUE)
                .build();

        return listar(filter, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<PromocionPublicResponseDto>> listarPublicasVigentes() {
        promocionPolicy.canViewPublic();

        List<PromocionPublicResponseDto> response = promocionVersionRepository
                .findPublicasAplicablesAt(
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

    private PromocionVersionCreateRequestDto normalizeCreateRequest(PromocionVersionCreateRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "PROMOCION_VERSION_REQUEST_REQUERIDO",
                    "Debe enviar los datos de la versión de promoción."
            );
        }

        return PromocionVersionCreateRequestDto.builder()
                .promocion(request.promocion())
                .fechaInicio(request.fechaInicio())
                .fechaFin(request.fechaFin())
                .estadoPromocion(request.estadoPromocion() == null
                        ? EstadoPromocion.BORRADOR
                        : request.estadoPromocion())
                .visiblePublico(request.visiblePublico() == null ? Boolean.TRUE : request.visiblePublico())
                .motivo(StringNormalizer.truncateOrNull(request.motivo(), 500))
                .descuentos(request.descuentos())
                .build();
    }

    private PromocionVersionEstadoRequestDto normalizeEstadoRequest(PromocionVersionEstadoRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "PROMOCION_VERSION_ESTADO_REQUEST_REQUERIDO",
                    "Debe enviar el estado de la versión de promoción."
            );
        }

        return PromocionVersionEstadoRequestDto.builder()
                .estadoPromocion(request.estadoPromocion())
                .visiblePublico(request.visiblePublico())
                .motivo(StringNormalizer.truncateOrNull(request.motivo(), 500))
                .build();
    }

    private PromocionSkuDescuentoCreateRequestDto normalizeDescuentoRequest(
            PromocionSkuDescuentoCreateRequestDto request
    ) {
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

    private Promocion resolvePromocion(EntityReferenceDto reference) {
        if (reference == null) {
            throw new ValidationException(
                    "PROMOCION_REFERENCIA_REQUERIDA",
                    "Debe indicar la promoción."
            );
        }

        return promocionReferenceResolver.resolve(
                reference.id(),
                firstText(reference.codigoPromocion(), reference.codigo()),
                reference.nombre()
        );
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

    private PromocionVersion currentVersion(Promocion promocion) {
        if (promocion == null || promocion.getIdPromocion() == null) {
            return null;
        }

        return promocionVersionRepository
                .findFirstByPromocion_IdPromocionAndVigenteTrueAndEstadoTrueOrderByFechaInicioDescIdPromocionVersionDesc(
                        promocion.getIdPromocion()
                )
                .orElse(null);
    }

    private void closeCurrentVersion(PromocionVersion current, String motivo) {
        if (current == null) {
            return;
        }

        current.setVigente(Boolean.FALSE);
        current.setVisiblePublico(Boolean.FALSE);

        if (current.getEstadoPromocion() == EstadoPromocion.ACTIVA
                || current.getEstadoPromocion() == EstadoPromocion.PROGRAMADA) {
            current.setEstadoPromocion(EstadoPromocion.FINALIZADA);
        }

        if (StringNormalizer.hasText(motivo)) {
            current.setMotivo(StringNormalizer.truncateOrNull(motivo, 500));
        }

        promocionVersionRepository.save(current);
    }

    private void closeOtherCurrentVersion(PromocionVersion version) {
        Promocion promocion = version.getPromocion();
        PromocionVersion current = currentVersion(promocion);

        if (current == null || current.getIdPromocionVersion().equals(version.getIdPromocionVersion())) {
            return;
        }

        closeCurrentVersion(current, "Reemplazada por activación de versión: " + version.getIdPromocionVersion());
    }

    private boolean hasDiscounts(PromocionVersion version) {
        return version != null
                && version.getIdPromocionVersion() != null
                && !promocionSkuDescuentoRepository
                .findByPromocionVersion_IdPromocionVersionAndEstadoTrueOrderByPrioridadAscIdPromocionSkuDescuentoVersionAsc(
                        version.getIdPromocionVersion()
                )
                .isEmpty();
    }

    private PromocionSkuDescuentoVersion crearDescuentoInterno(
            PromocionVersion version,
            PromocionSkuDescuentoCreateRequestDto request
    ) {
        PromocionSkuDescuentoCreateRequestDto normalized = normalizeDescuentoRequest(request);
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

        return promocionSkuDescuentoRepository.save(entity);
    }

    private PromocionVersionResponseDto toVersionResponse(PromocionVersion version) {
        List<PromocionSkuDescuentoResponseDto> descuentos = promocionSkuDescuentoRepository
                .findByPromocionVersion_IdPromocionVersionAndEstadoTrueOrderByPrioridadAscIdPromocionSkuDescuentoVersionAsc(
                        version.getIdPromocionVersion()
                )
                .stream()
                .map(this::toDescuentoResponse)
                .toList();

        return promocionVersionMapper.toResponse(version, descuentos);
    }

    private PromocionSkuDescuentoResponseDto toDescuentoResponse(PromocionSkuDescuentoVersion descuento) {
        PrecioSkuHistorial precio = descuento.getSku() == null
                ? null
                : promocionPricingSupport.currentPriceRequired(descuento.getSku());

        MoneyResponseDto precioBase = precio == null
                ? null
                : promocionPricingSupport.toMoney(precio.getPrecioVenta(), precio.getMoneda());

        Moneda moneda = precio == null ? Moneda.PEN : precio.getMoneda();

        return promocionSkuDescuentoMapper.toResponse(descuento, precioBase, moneda);
    }

    private PromocionPublicResponseDto toPublicResponse(PromocionVersion version) {
        Promocion promocion = version.getPromocion();

        List<PromocionSkuDescuentoResponseDto> descuentos = promocionSkuDescuentoRepository
                .findByPromocionVersion_IdPromocionVersionAndEstadoTrueOrderByPrioridadAscIdPromocionSkuDescuentoVersionAsc(
                        version.getIdPromocionVersion()
                )
                .stream()
                .map(this::toDescuentoResponse)
                .toList();

        return PromocionPublicResponseDto.builder()
                .idPromocion(promocion == null ? null : promocion.getIdPromocion())
                .idPromocionVersion(version.getIdPromocionVersion())
                .codigo(promocion == null ? null : promocion.getCodigo())
                .nombre(promocion == null ? null : promocion.getNombre())
                .descripcion(promocion == null ? null : promocion.getDescripcion())
                .fechaInicio(version.getFechaInicio())
                .fechaFin(version.getFechaFin())
                .estadoPromocion(version.getEstadoPromocion())
                .descuentos(descuentos)
                .build();
    }

    private Map<String, Object> versionAuditMetadata(
            PromocionVersion version,
            AuthenticatedUserContext actor,
            Map<String, Object> extra
    ) {
        Map<String, Object> metadata = versionAuditSnapshot(version);
        metadata.put("actor", actor.actorLabel());
        metadata.put("idUsuarioMs1", actor.getIdUsuarioMs1());

        if (extra != null && !extra.isEmpty()) {
            metadata.putAll(extra);
        }

        return metadata;
    }

    private Map<String, Object> versionAuditSnapshot(PromocionVersion version) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        Promocion promocion = version.getPromocion();

        metadata.put("idPromocionVersion", version.getIdPromocionVersion());
        metadata.put("idPromocion", promocion == null ? null : promocion.getIdPromocion());
        metadata.put("codigoPromocion", promocion == null ? null : promocion.getCodigo());
        metadata.put("fechaInicio", version.getFechaInicio());
        metadata.put("fechaFin", version.getFechaFin());
        metadata.put("estadoPromocion", version.getEstadoPromocion() == null ? null : version.getEstadoPromocion().getCode());
        metadata.put("visiblePublico", version.getVisiblePublico());
        metadata.put("vigente", version.getVigente());
        metadata.put("estado", version.getEstado());

        return metadata;
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