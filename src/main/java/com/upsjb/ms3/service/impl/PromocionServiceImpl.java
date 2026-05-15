// ruta: src/main/java/com/upsjb/ms3/service/impl/PromocionServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.PrecioSkuHistorial;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.domain.entity.Promocion;
import com.upsjb.ms3.domain.entity.PromocionSkuDescuentoVersion;
import com.upsjb.ms3.domain.entity.PromocionVersion;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.EstadoPromocion;
import com.upsjb.ms3.domain.enums.PromocionEventType;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.dto.promocion.filter.PromocionFilterDto;
import com.upsjb.ms3.dto.promocion.filter.PromocionVersionFilterDto;
import com.upsjb.ms3.dto.promocion.request.PromocionCreateRequestDto;
import com.upsjb.ms3.dto.promocion.request.PromocionSkuDescuentoCreateRequestDto;
import com.upsjb.ms3.dto.promocion.request.PromocionSkuDescuentoUpdateRequestDto;
import com.upsjb.ms3.dto.promocion.request.PromocionUpdateRequestDto;
import com.upsjb.ms3.dto.promocion.request.PromocionVersionCreateRequestDto;
import com.upsjb.ms3.dto.promocion.request.PromocionVersionEstadoRequestDto;
import com.upsjb.ms3.dto.promocion.response.PromocionDetailResponseDto;
import com.upsjb.ms3.dto.promocion.response.PromocionPublicResponseDto;
import com.upsjb.ms3.dto.promocion.response.PromocionResponseDto;
import com.upsjb.ms3.dto.promocion.response.PromocionSkuDescuentoResponseDto;
import com.upsjb.ms3.dto.promocion.response.PromocionVersionResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.MoneyResponseDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.mapper.PromocionMapper;
import com.upsjb.ms3.mapper.PromocionSkuDescuentoMapper;
import com.upsjb.ms3.mapper.PromocionVersionMapper;
import com.upsjb.ms3.policy.PromocionPolicy;
import com.upsjb.ms3.repository.PromocionRepository;
import com.upsjb.ms3.repository.PromocionSkuDescuentoVersionRepository;
import com.upsjb.ms3.repository.PromocionVersionRepository;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.security.principal.CurrentUserResolver;
import com.upsjb.ms3.service.contract.AuditoriaFuncionalService;
import com.upsjb.ms3.service.contract.CodigoGeneradorService;
import com.upsjb.ms3.service.contract.PromocionService;
import com.upsjb.ms3.service.support.PromocionPricingSupport;
import com.upsjb.ms3.service.support.PromocionSnapshotOutboxSupport;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.pagination.PaginationService;
import com.upsjb.ms3.shared.reference.ProductoSkuReferenceResolver;
import com.upsjb.ms3.shared.reference.PromocionReferenceResolver;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.specification.PromocionSpecifications;
import com.upsjb.ms3.specification.PromocionVersionSpecifications;
import com.upsjb.ms3.util.DateTimeUtil;
import com.upsjb.ms3.util.MoneyUtil;
import com.upsjb.ms3.util.StringNormalizer;
import com.upsjb.ms3.validator.PromocionSkuDescuentoValidator;
import com.upsjb.ms3.validator.PromocionValidator;
import com.upsjb.ms3.validator.PromocionVersionValidator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
public class PromocionServiceImpl implements PromocionService {

    private static final Set<String> ALLOWED_PROMOCION_SORT_FIELDS = Set.of(
            "idPromocion",
            "codigo",
            "nombre",
            "creadoPorIdUsuarioMs1",
            "estado",
            "createdAt",
            "updatedAt"
    );

    private static final Set<String> ALLOWED_VERSION_SORT_FIELDS = Set.of(
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

    private final PromocionRepository promocionRepository;
    private final PromocionVersionRepository promocionVersionRepository;
    private final PromocionSkuDescuentoVersionRepository promocionSkuDescuentoRepository;
    private final PromocionReferenceResolver promocionReferenceResolver;
    private final ProductoSkuReferenceResolver productoSkuReferenceResolver;

    private final PromocionMapper promocionMapper;
    private final PromocionVersionMapper promocionVersionMapper;
    private final PromocionSkuDescuentoMapper promocionSkuDescuentoMapper;

    private final PromocionValidator promocionValidator;
    private final PromocionVersionValidator promocionVersionValidator;
    private final PromocionSkuDescuentoValidator promocionSkuDescuentoValidator;
    private final PromocionPolicy promocionPolicy;

    private final CodigoGeneradorService codigoGeneradorService;
    private final CurrentUserResolver currentUserResolver;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final PromocionPricingSupport promocionPricingSupport;
    private final PromocionSnapshotOutboxSupport promocionSnapshotOutboxSupport;
    private final PaginationService paginationService;
    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional
    public ApiResponseDto<PromocionResponseDto> crear(PromocionCreateRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        promocionPolicy.ensureCanCreate(actor);

        PromocionCreateRequestDto normalized = normalizeCreateRequest(request);
        String codigo = codigoGeneradorService.generarCodigoPromocion();

        promocionValidator.validateCreate(
                codigo,
                normalized.nombre(),
                actor.getIdUsuarioMs1(),
                promocionRepository.existsByCodigoIgnoreCaseAndEstadoTrue(codigo),
                promocionRepository.existsByNombreIgnoreCaseAndEstadoTrue(normalized.nombre())
        );

        Promocion promocion = promocionMapper.toEntity(
                normalized,
                codigo,
                actor.getIdUsuarioMs1()
        );
        promocion.activar();

        Promocion saved = promocionRepository.saveAndFlush(promocion);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.PROMOCION_CREADA,
                EntidadAuditada.PROMOCION,
                String.valueOf(saved.getIdPromocion()),
                "CREAR_PROMOCION",
                "Promoción creada correctamente.",
                auditMetadata(saved, actor, Map.of())
        );

        registrarPromocionSnapshot(saved, null, PromocionEventType.PROMOCION_SNAPSHOT_ACTUALIZADA);

        log.info(
                "Promoción creada. idPromocion={}, codigo={}, actor={}",
                saved.getIdPromocion(),
                saved.getCodigo(),
                actor.actorLabel()
        );

        return apiResponseFactory.dtoCreated(
                "Promoción creada correctamente.",
                promocionMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<PromocionResponseDto> actualizar(Long idPromocion, PromocionUpdateRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        promocionPolicy.ensureCanUpdate(actor);

        Promocion promocion = findPromocionRequired(idPromocion);
        PromocionUpdateRequestDto normalized = normalizeUpdateRequest(request);

        promocionValidator.validateUpdate(
                promocion,
                normalized.nombre(),
                promocionRepository.existsByNombreIgnoreCaseAndEstadoTrueAndIdPromocionNot(
                        normalized.nombre(),
                        promocion.getIdPromocion()
                )
        );

        Map<String, Object> before = auditSnapshot(promocion);

        promocionMapper.updateEntity(promocion, normalized);

        Promocion saved = promocionRepository.saveAndFlush(promocion);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.PROMOCION_ACTUALIZADA,
                EntidadAuditada.PROMOCION,
                String.valueOf(saved.getIdPromocion()),
                "ACTUALIZAR_PROMOCION",
                "Promoción actualizada correctamente.",
                auditMetadata(saved, actor, Map.of("before", before))
        );

        registrarPromocionSnapshot(
                saved,
                findCurrentVersionOrNull(saved),
                PromocionEventType.PROMOCION_SNAPSHOT_ACTUALIZADA
        );

        return apiResponseFactory.dtoOk(
                "Promoción actualizada correctamente.",
                promocionMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<PromocionResponseDto> inactivar(Long idPromocion, EstadoChangeRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        promocionPolicy.ensureCanCancel(actor);

        Promocion promocion = findPromocionRequired(idPromocion);
        requireEstadoFalse(request);

        promocionValidator.validateCanDeactivate(
                promocion,
                promocionVersionRepository.existsByPromocion_IdPromocionAndVigenteTrueAndEstadoTrue(
                        promocion.getIdPromocion()
                )
        );

        promocion.inactivar();

        Promocion saved = promocionRepository.saveAndFlush(promocion);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.PROMOCION_CANCELADA,
                EntidadAuditada.PROMOCION,
                String.valueOf(saved.getIdPromocion()),
                "INACTIVAR_PROMOCION",
                "Operación realizada correctamente.",
                auditMetadata(saved, actor, Map.of("motivo", request.motivo()))
        );

        registrarPromocionSnapshot(saved, null, PromocionEventType.PROMOCION_SNAPSHOT_CANCELADA);

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                promocionMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PromocionResponseDto> obtenerPorId(Long idPromocion) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        promocionPolicy.ensureCanViewAdmin(actor);

        Promocion promocion = findPromocionRequired(idPromocion);

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                promocionMapper.toResponse(promocion)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PromocionDetailResponseDto> obtenerDetalle(Long idPromocion) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        promocionPolicy.ensureCanViewAdmin(actor);

        Promocion promocion = findPromocionRequired(idPromocion);

        List<PromocionVersionResponseDto> versiones = promocionVersionRepository
                .findByPromocion_IdPromocionAndEstadoTrueOrderByFechaInicioDescIdPromocionVersionDesc(
                        promocion.getIdPromocion()
                )
                .stream()
                .map(this::toVersionResponse)
                .toList();

        PromocionDetailResponseDto response = PromocionDetailResponseDto.builder()
                .idPromocion(promocion.getIdPromocion())
                .codigo(promocion.getCodigo())
                .codigoGenerado(promocion.getCodigoGenerado())
                .nombre(promocion.getNombre())
                .descripcion(promocion.getDescripcion())
                .estado(promocion.getEstado())
                .versiones(versiones)
                .createdAt(promocion.getCreatedAt())
                .updatedAt(promocion.getUpdatedAt())
                .build();

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                response
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<PromocionResponseDto>> listar(
            PromocionFilterDto filter,
            PageRequestDto pageRequest
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        promocionPolicy.ensureCanViewAdmin(actor);

        PageRequestDto safePage = safePageRequest(pageRequest, "updatedAt");

        Pageable pageable = paginationService.pageable(
                safePage.page(),
                safePage.size(),
                safePage.sortBy(),
                safePage.sortDirection(),
                ALLOWED_PROMOCION_SORT_FIELDS,
                "updatedAt"
        );

        PageResponseDto<PromocionResponseDto> response = paginationService.toPageResponseDto(
                promocionRepository.findAll(PromocionSpecifications.fromFilter(filter), pageable),
                promocionMapper::toResponse
        );

        return apiResponseFactory.dtoOk(
                "Lista obtenida correctamente.",
                response
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<PromocionVersionResponseDto> crearVersion(PromocionVersionCreateRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        promocionPolicy.ensureCanCreateVersion(actor);

        PromocionVersionCreateRequestDto normalized = normalizeVersionCreateRequest(request);
        Promocion promocion = resolvePromocion(normalized.promocion());

        boolean hasOverlappingVersion = !promocionVersionRepository
                .findVersionesSolapadas(
                        promocion.getIdPromocion(),
                        normalized.fechaInicio(),
                        normalized.fechaFin(),
                        null
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

        PromocionVersion current = findCurrentVersionOrNull(promocion);
        if (current != null) {
            promocionVersionMapper.closeVersion(
                    current,
                    "Reemplazada por nueva versión: " + normalized.motivo()
            );
            promocionVersionRepository.save(current);
        }

        PromocionVersion version = promocionVersionMapper.toEntity(
                normalized,
                promocion,
                actor.getIdUsuarioMs1()
        );
        version.activar();

        PromocionVersion saved = promocionVersionRepository.saveAndFlush(version);

        if (normalized.descuentos() != null) {
            for (PromocionSkuDescuentoCreateRequestDto descuento : normalized.descuentos()) {
                crearDescuentoInterno(saved, descuento, false);
            }
        }

        if (saved.getEstadoPromocion() == EstadoPromocion.ACTIVA
                || saved.getEstadoPromocion() == EstadoPromocion.PROGRAMADA) {
            promocionVersionValidator.validateCanActivate(
                    saved,
                    saved.getEstadoPromocion(),
                    hasDiscounts(saved),
                    DateTimeUtil.nowUtc()
            );
        }

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.PROMOCION_VERSIONADA,
                EntidadAuditada.PROMOCION_VERSION,
                String.valueOf(saved.getIdPromocionVersion()),
                "CREAR_VERSION_PROMOCION",
                "Promoción versionada correctamente.",
                versionAuditMetadata(saved, actor, Map.of())
        );

        registrarPromocionSnapshot(promocion, saved, PromocionEventType.PROMOCION_SNAPSHOT_ACTUALIZADA);

        return apiResponseFactory.dtoCreated(
                "Promoción versionada correctamente.",
                toVersionResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<PromocionVersionResponseDto> cambiarEstadoVersion(
            Long idPromocionVersion,
            PromocionVersionEstadoRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        promocionPolicy.ensureCanChangeVersionState(actor);

        PromocionVersion version = findVersionRequired(idPromocionVersion);
        PromocionVersionEstadoRequestDto normalized = normalizeVersionEstadoRequest(request);

        if (normalized.estadoPromocion() == EstadoPromocion.CANCELADA) {
            return cancelarVersion(
                    idPromocionVersion,
                    EstadoChangeRequestDto.builder()
                            .estado(Boolean.FALSE)
                            .motivo(normalized.motivo())
                            .build()
            );
        }

        boolean hasDiscounts = hasDiscounts(version);
        promocionVersionValidator.validateCanChangeState(
                version,
                normalized.estadoPromocion(),
                normalized.motivo(),
                hasDiscounts,
                DateTimeUtil.nowUtc()
        );

        Map<String, Object> before = versionAuditSnapshot(version);

        promocionVersionMapper.applyEstado(version, normalized);
        applyVersionLifecycleFlags(version, normalized.estadoPromocion(), normalized.visiblePublico());

        PromocionVersion saved = promocionVersionRepository.saveAndFlush(version);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.PROMOCION_VERSIONADA,
                EntidadAuditada.PROMOCION_VERSION,
                String.valueOf(saved.getIdPromocionVersion()),
                "CAMBIAR_ESTADO_PROMOCION_VERSION",
                "Operación realizada correctamente.",
                versionAuditMetadata(saved, actor, Map.of("before", before))
        );

        PromocionEventType eventType = normalized.estadoPromocion() == EstadoPromocion.FINALIZADA
                ? PromocionEventType.PROMOCION_SNAPSHOT_FINALIZADA
                : PromocionEventType.PROMOCION_SNAPSHOT_ACTUALIZADA;

        registrarPromocionSnapshot(saved.getPromocion(), saved, eventType);

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                toVersionResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<PromocionVersionResponseDto> cancelarVersion(
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
        version.setMotivo(request.motivo());

        PromocionVersion saved = promocionVersionRepository.saveAndFlush(version);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.PROMOCION_CANCELADA,
                EntidadAuditada.PROMOCION_VERSION,
                String.valueOf(saved.getIdPromocionVersion()),
                "CANCELAR_PROMOCION_VERSION",
                "Operación realizada correctamente.",
                versionAuditMetadata(saved, actor, Map.of("motivo", request.motivo()))
        );

        registrarPromocionSnapshot(saved.getPromocion(), saved, PromocionEventType.PROMOCION_SNAPSHOT_CANCELADA);

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                toVersionResponse(saved)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PromocionVersionResponseDto> obtenerVersionDetalle(Long idPromocionVersion) {
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
    public ApiResponseDto<PageResponseDto<PromocionVersionResponseDto>> listarVersiones(
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
                ALLOWED_VERSION_SORT_FIELDS,
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
    @Transactional
    public ApiResponseDto<PromocionSkuDescuentoResponseDto> agregarDescuentoSku(
            Long idPromocionVersion,
            PromocionSkuDescuentoCreateRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        promocionPolicy.ensureCanManageSkuDiscounts(actor);

        PromocionVersion version = findVersionRequired(idPromocionVersion);
        PromocionSkuDescuentoVersion descuento = crearDescuentoInterno(version, request, true);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.PROMOCION_VERSIONADA,
                EntidadAuditada.PROMOCION_SKU_DESCUENTO_VERSION,
                String.valueOf(descuento.getIdPromocionSkuDescuentoVersion()),
                "AGREGAR_DESCUENTO_SKU_PROMOCION",
                "Operación realizada correctamente.",
                descuentoAuditMetadata(descuento, actor, Map.of())
        );

        registrarPromocionSnapshot(version.getPromocion(), version, PromocionEventType.PROMOCION_SNAPSHOT_ACTUALIZADA);

        return apiResponseFactory.dtoCreated(
                "Operación realizada correctamente.",
                toDescuentoResponse(descuento)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<PromocionSkuDescuentoResponseDto> actualizarDescuentoSku(
            Long idPromocionSkuDescuentoVersion,
            PromocionSkuDescuentoUpdateRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        promocionPolicy.ensureCanManageSkuDiscounts(actor);

        PromocionSkuDescuentoVersion descuento = findDescuentoRequired(idPromocionSkuDescuentoVersion);
        PromocionSkuDescuentoUpdateRequestDto normalized = normalizeDescuentoUpdateRequest(request);

        PromocionVersion version = descuento.getPromocionVersion();
        ProductoSku sku = descuento.getSku();
        PrecioSkuHistorial precio = promocionPricingSupport.currentPriceRequired(sku);
        BigDecimal costoEstimado = promocionPricingSupport.costoPromedioEstimado(sku);
        BigDecimal precioFinal = promocionPricingSupport.resolvePrecioFinal(
                normalized.tipoDescuento(),
                normalized.valorDescuento(),
                precio.getPrecioVenta()
        );

        promocionSkuDescuentoValidator.validateUpdate(
                version,
                normalized.tipoDescuento(),
                normalized.valorDescuento(),
                precio.getPrecioVenta(),
                costoEstimado,
                normalized.limiteUnidades(),
                normalized.prioridad()
        );

        Map<String, Object> before = descuentoAuditSnapshot(descuento);

        PromocionSkuDescuentoUpdateRequestDto normalizedWithCalculatedValues = PromocionSkuDescuentoUpdateRequestDto.builder()
                .tipoDescuento(normalized.tipoDescuento())
                .valorDescuento(normalized.valorDescuento())
                .precioFinalEstimado(precioFinal)
                .margenEstimado(promocionPricingSupport.resolveMargen(precioFinal, costoEstimado))
                .limiteUnidades(normalized.limiteUnidades())
                .prioridad(normalized.prioridad())
                .build();

        promocionSkuDescuentoMapper.updateEntity(descuento, normalizedWithCalculatedValues);

        PromocionSkuDescuentoVersion saved = promocionSkuDescuentoRepository.saveAndFlush(descuento);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.PROMOCION_VERSIONADA,
                EntidadAuditada.PROMOCION_SKU_DESCUENTO_VERSION,
                String.valueOf(saved.getIdPromocionSkuDescuentoVersion()),
                "ACTUALIZAR_DESCUENTO_SKU_PROMOCION",
                "Operación realizada correctamente.",
                descuentoAuditMetadata(saved, actor, Map.of("before", before))
        );

        registrarPromocionSnapshot(version.getPromocion(), version, PromocionEventType.PROMOCION_SNAPSHOT_ACTUALIZADA);

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                toDescuentoResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<PromocionSkuDescuentoResponseDto> inactivarDescuentoSku(
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
                descuentoAuditMetadata(saved, actor, Map.of("motivo", request.motivo()))
        );

        registrarPromocionSnapshot(version.getPromocion(), version, PromocionEventType.PROMOCION_SNAPSHOT_ACTUALIZADA);

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                toDescuentoResponse(saved)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<PromocionPublicResponseDto>> listarPublicasVigentes() {
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

    private PromocionCreateRequestDto normalizeCreateRequest(PromocionCreateRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "PROMOCION_REQUEST_REQUERIDO",
                    "Debe enviar los datos de la promoción."
            );
        }

        return PromocionCreateRequestDto.builder()
                .nombre(StringNormalizer.truncateOrNull(request.nombre(), 180))
                .descripcion(StringNormalizer.truncateOrNull(request.descripcion(), 500))
                .build();
    }

    private PromocionUpdateRequestDto normalizeUpdateRequest(PromocionUpdateRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "PROMOCION_REQUEST_REQUERIDO",
                    "Debe enviar los datos de la promoción."
            );
        }

        return PromocionUpdateRequestDto.builder()
                .nombre(StringNormalizer.truncateOrNull(request.nombre(), 180))
                .descripcion(StringNormalizer.truncateOrNull(request.descripcion(), 500))
                .build();
    }

    private PromocionVersionCreateRequestDto normalizeVersionCreateRequest(PromocionVersionCreateRequestDto request) {
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

    private PromocionVersionEstadoRequestDto normalizeVersionEstadoRequest(PromocionVersionEstadoRequestDto request) {
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

    private PromocionSkuDescuentoCreateRequestDto normalizeDescuentoCreateRequest(
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

    private PromocionSkuDescuentoUpdateRequestDto normalizeDescuentoUpdateRequest(
            PromocionSkuDescuentoUpdateRequestDto request
    ) {
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

    private Promocion findPromocionRequired(Long idPromocion) {
        if (idPromocion == null) {
            throw new ValidationException(
                    "PROMOCION_ID_REQUERIDO",
                    "Debe indicar la promoción solicitada."
            );
        }

        Promocion promocion = promocionRepository.findByIdPromocionAndEstadoTrue(idPromocion)
                .orElseThrow(() -> new NotFoundException(
                        "PROMOCION_NO_ENCONTRADA",
                        "No se encontró el registro solicitado."
                ));

        promocionValidator.requireActive(promocion);
        return promocion;
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

    private PromocionVersion findCurrentVersionOrNull(Promocion promocion) {
        if (promocion == null || promocion.getIdPromocion() == null) {
            return null;
        }

        return promocionVersionRepository
                .findFirstByPromocion_IdPromocionAndVigenteTrueAndEstadoTrueOrderByFechaInicioDescIdPromocionVersionDesc(
                        promocion.getIdPromocion()
                )
                .orElse(null);
    }

    private PromocionSkuDescuentoVersion crearDescuentoInterno(
            PromocionVersion version,
            PromocionSkuDescuentoCreateRequestDto request,
            boolean flush
    ) {
        PromocionSkuDescuentoCreateRequestDto normalized = normalizeDescuentoCreateRequest(request);
        ProductoSku sku = resolveSku(normalized.sku());

        PrecioSkuHistorial precio = promocionPricingSupport.currentPriceRequired(sku);
        BigDecimal costoEstimado = promocionPricingSupport.costoPromedioEstimado(sku);
        BigDecimal precioFinal = promocionPricingSupport.resolvePrecioFinal(
                normalized.tipoDescuento(),
                normalized.valorDescuento(),
                precio.getPrecioVenta()
        );

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

        PromocionSkuDescuentoCreateRequestDto normalizedWithCalculatedValues = PromocionSkuDescuentoCreateRequestDto.builder()
                .sku(normalized.sku())
                .tipoDescuento(normalized.tipoDescuento())
                .valorDescuento(normalized.valorDescuento())
                .precioFinalEstimado(precioFinal)
                .margenEstimado(promocionPricingSupport.resolveMargen(precioFinal, costoEstimado))
                .limiteUnidades(normalized.limiteUnidades())
                .prioridad(normalized.prioridad())
                .build();

        PromocionSkuDescuentoVersion entity = promocionSkuDescuentoMapper.toEntity(
                normalizedWithCalculatedValues,
                version,
                sku
        );
        entity.activar();

        return flush
                ? promocionSkuDescuentoRepository.saveAndFlush(entity)
                : promocionSkuDescuentoRepository.save(entity);
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
        PrecioSkuHistorial precio = promocionPricingSupport.currentPriceOptional(descuento.getSku())
                .orElse(null);

        MoneyResponseDto precioBase = precio == null
                ? null
                : promocionPricingSupport.toMoney(precio.getPrecioVenta(), precio.getMoneda());

        return promocionSkuDescuentoMapper.toResponse(
                descuento,
                precioBase,
                precio == null ? null : precio.getMoneda()
        );
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

    private void registrarPromocionSnapshot(
            Promocion promocion,
            PromocionVersion version,
            PromocionEventType eventType
    ) {
        promocionSnapshotOutboxSupport.registrarSnapshot(
                promocion,
                version,
                eventType,
                "PromocionService"
        );
    }

    private void applyVersionLifecycleFlags(
            PromocionVersion version,
            EstadoPromocion estadoPromocion,
            Boolean visiblePublico
    ) {
        if (estadoPromocion == EstadoPromocion.ACTIVA || estadoPromocion == EstadoPromocion.PROGRAMADA) {
            version.setVigente(Boolean.TRUE);
            version.setVisiblePublico(visiblePublico == null ? Boolean.TRUE : visiblePublico);
            return;
        }

        if (estadoPromocion == EstadoPromocion.FINALIZADA || estadoPromocion == EstadoPromocion.BORRADOR) {
            version.setVigente(Boolean.FALSE);
        }

        if (estadoPromocion == EstadoPromocion.FINALIZADA) {
            version.setVisiblePublico(Boolean.FALSE);
        }
    }

    private Map<String, Object> auditMetadata(
            Promocion promocion,
            AuthenticatedUserContext actor,
            Map<String, Object> extra
    ) {
        Map<String, Object> metadata = auditSnapshot(promocion);
        metadata.put("actor", actor.actorLabel());
        metadata.put("idUsuarioMs1", actor.getIdUsuarioMs1());

        if (extra != null && !extra.isEmpty()) {
            metadata.putAll(extra);
        }

        return metadata;
    }

    private Map<String, Object> auditSnapshot(Promocion promocion) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("idPromocion", promocion.getIdPromocion());
        metadata.put("codigo", promocion.getCodigo());
        metadata.put("nombre", promocion.getNombre());
        metadata.put("estado", promocion.getEstado());
        return metadata;
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
        metadata.put(
                "estadoPromocion",
                version.getEstadoPromocion() == null ? null : version.getEstadoPromocion().getCode()
        );
        metadata.put("visiblePublico", version.getVisiblePublico());
        metadata.put("vigente", version.getVigente());
        metadata.put("estado", version.getEstado());

        return metadata;
    }

    private Map<String, Object> descuentoAuditMetadata(
            PromocionSkuDescuentoVersion descuento,
            AuthenticatedUserContext actor,
            Map<String, Object> extra
    ) {
        Map<String, Object> metadata = descuentoAuditSnapshot(descuento);
        metadata.put("actor", actor.actorLabel());
        metadata.put("idUsuarioMs1", actor.getIdUsuarioMs1());

        if (extra != null && !extra.isEmpty()) {
            metadata.putAll(extra);
        }

        return metadata;
    }

    private Map<String, Object> descuentoAuditSnapshot(PromocionSkuDescuentoVersion descuento) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        ProductoSku sku = descuento.getSku();
        PromocionVersion version = descuento.getPromocionVersion();

        metadata.put("idPromocionSkuDescuentoVersion", descuento.getIdPromocionSkuDescuentoVersion());
        metadata.put("idPromocionVersion", version == null ? null : version.getIdPromocionVersion());
        metadata.put("idSku", sku == null ? null : sku.getIdSku());
        metadata.put("codigoSku", sku == null ? null : sku.getCodigoSku());
        metadata.put(
                "tipoDescuento",
                descuento.getTipoDescuento() == null ? null : descuento.getTipoDescuento().getCode()
        );
        metadata.put("valorDescuento", descuento.getValorDescuento());
        metadata.put("precioFinalEstimado", descuento.getPrecioFinalEstimado());
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
                    "Para inactivar debe enviar estado=false."
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