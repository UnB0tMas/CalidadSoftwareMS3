// ruta: src/main/java/com/upsjb/ms3/service/impl/AlmacenServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.Almacen;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.EstadoReservaStock;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.dto.inventario.almacen.filter.AlmacenFilterDto;
import com.upsjb.ms3.dto.inventario.almacen.request.AlmacenCreateRequestDto;
import com.upsjb.ms3.dto.inventario.almacen.request.AlmacenEstadoRequestDto;
import com.upsjb.ms3.dto.inventario.almacen.request.AlmacenUpdateRequestDto;
import com.upsjb.ms3.dto.inventario.almacen.response.AlmacenDetailResponseDto;
import com.upsjb.ms3.dto.inventario.almacen.response.AlmacenResponseDto;
import com.upsjb.ms3.dto.reference.response.AlmacenOptionDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.mapper.AlmacenMapper;
import com.upsjb.ms3.policy.AlmacenPolicy;
import com.upsjb.ms3.repository.AlmacenRepository;
import com.upsjb.ms3.repository.MovimientoInventarioRepository;
import com.upsjb.ms3.repository.ReservaStockRepository;
import com.upsjb.ms3.repository.StockSkuRepository;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.security.principal.CurrentUserResolver;
import com.upsjb.ms3.service.contract.AlmacenService;
import com.upsjb.ms3.service.contract.AuditoriaFuncionalService;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.pagination.PaginationService;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.specification.AlmacenSpecifications;
import com.upsjb.ms3.util.StringNormalizer;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.upsjb.ms3.validator.AlmacenValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlmacenServiceImpl implements AlmacenService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "idAlmacen",
            "codigo",
            "nombre",
            "principal",
            "permiteVenta",
            "permiteCompra",
            "estado",
            "createdAt",
            "updatedAt"
    );

    private static final int DEFAULT_LOOKUP_LIMIT = 20;
    private static final int MAX_LOOKUP_LIMIT = 50;

    private final AlmacenRepository almacenRepository;
    private final StockSkuRepository stockSkuRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final ReservaStockRepository reservaStockRepository;
    private final AlmacenMapper almacenMapper;
    private final AlmacenValidator almacenValidator;
    private final AlmacenPolicy almacenPolicy;
    private final CurrentUserResolver currentUserResolver;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final PaginationService paginationService;
    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional
    public ApiResponseDto<AlmacenResponseDto> crear(AlmacenCreateRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        almacenPolicy.ensureCanCreate(actor);

        AlmacenCreateRequestDto normalized = normalizeCreate(request);

        almacenValidator.validateCreate(
                normalized.codigo(),
                normalized.nombre(),
                normalized.permiteVenta(),
                normalized.permiteCompra(),
                almacenRepository.existsByCodigoIgnoreCaseAndEstadoTrue(normalized.codigo()),
                almacenRepository.existsByNombreIgnoreCaseAndEstadoTrue(normalized.nombre()),
                almacenRepository.existsByPrincipalTrueAndEstadoTrue(),
                normalized.principal()
        );

        Almacen entity = almacenMapper.toEntity(normalized);
        Almacen saved = almacenRepository.save(entity);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.ALMACEN_CREADO,
                EntidadAuditada.ALMACEN,
                String.valueOf(saved.getIdAlmacen()),
                "CREAR_ALMACEN",
                "Almacén registrado correctamente.",
                auditMetadata(saved, actor, null)
        );

        log.info(
                "Almacén creado. idAlmacen={}, codigo={}, actor={}",
                saved.getIdAlmacen(),
                saved.getCodigo(),
                actor.actorLabel()
        );

        return apiResponseFactory.dtoCreated(
                "Almacén registrado correctamente.",
                almacenMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<AlmacenResponseDto> actualizar(Long idAlmacen, AlmacenUpdateRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        almacenPolicy.ensureCanUpdate(actor);

        Almacen entity = findRequired(idAlmacen);
        AlmacenUpdateRequestDto normalized = normalizeUpdate(request);

        boolean principalChangedToTrue = Boolean.TRUE.equals(normalized.principal())
                && !Boolean.TRUE.equals(entity.getPrincipal());

        almacenValidator.validateUpdate(
                entity,
                normalized.codigo(),
                normalized.nombre(),
                normalized.permiteVenta(),
                normalized.permiteCompra(),
                almacenRepository.existsByCodigoIgnoreCaseAndEstadoTrueAndIdAlmacenNot(
                        normalized.codigo(),
                        entity.getIdAlmacen()
                ),
                almacenRepository.existsByNombreIgnoreCaseAndEstadoTrueAndIdAlmacenNot(
                        normalized.nombre(),
                        entity.getIdAlmacen()
                ),
                principalChangedToTrue && almacenRepository.existsByPrincipalTrueAndEstadoTrueAndIdAlmacenNot(
                        entity.getIdAlmacen()
                ),
                normalized.principal()
        );

        Map<String, Object> before = Map.of(
                "codigo", safe(entity.getCodigo()),
                "nombre", safe(entity.getNombre()),
                "principal", Boolean.TRUE.equals(entity.getPrincipal()),
                "permiteVenta", Boolean.TRUE.equals(entity.getPermiteVenta()),
                "permiteCompra", Boolean.TRUE.equals(entity.getPermiteCompra())
        );

        almacenMapper.updateEntity(entity, normalized);
        Almacen saved = almacenRepository.save(entity);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.ALMACEN_ACTUALIZADO,
                EntidadAuditada.ALMACEN,
                String.valueOf(saved.getIdAlmacen()),
                "ACTUALIZAR_ALMACEN",
                "Almacén actualizado correctamente.",
                auditMetadata(saved, actor, before)
        );

        log.info(
                "Almacén actualizado. idAlmacen={}, codigo={}, actor={}",
                saved.getIdAlmacen(),
                saved.getCodigo(),
                actor.actorLabel()
        );

        return apiResponseFactory.dtoOk(
                "Almacén actualizado correctamente.",
                almacenMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<AlmacenResponseDto> cambiarEstado(Long idAlmacen, AlmacenEstadoRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        almacenPolicy.ensureCanChangeState(actor);

        if (request == null || request.estado() == null) {
            throw new ValidationException(
                    "ALMACEN_ESTADO_REQUERIDO",
                    "Debe indicar el estado solicitado."
            );
        }

        requireMotivo(request.motivo());

        Almacen entity = findRequired(idAlmacen);

        if (Objects.equals(entity.getEstado(), request.estado())) {
            return apiResponseFactory.dtoOk(
                    "Operación realizada correctamente.",
                    almacenMapper.toResponse(entity)
            );
        }

        if (Boolean.TRUE.equals(request.estado())) {
            almacenValidator.validateCanActivate(entity);
            entity.activar();
            Almacen saved = almacenRepository.save(entity);

            auditoriaFuncionalService.registrarExito(
                    TipoEventoAuditoria.ALMACEN_ACTIVADO,
                    EntidadAuditada.ALMACEN,
                    String.valueOf(saved.getIdAlmacen()),
                    "ACTIVAR_ALMACEN",
                    "Almacén activado correctamente.",
                    auditMetadata(saved, actor, Map.of("motivo", request.motivo()))
            );

            return apiResponseFactory.dtoOk(
                    "Almacén activado correctamente.",
                    almacenMapper.toResponse(saved)
            );
        }

        boolean hasStock = stockSkuRepository.existsByAlmacen_IdAlmacenAndEstadoTrue(entity.getIdAlmacen());
        boolean hasMovements = movimientoInventarioRepository.existsByAlmacen_IdAlmacenAndEstadoTrue(entity.getIdAlmacen());
        boolean hasPendingReservations = reservaStockRepository.existsByAlmacen_IdAlmacenAndEstadoReservaInAndEstadoTrue(
                entity.getIdAlmacen(),
                List.of(EstadoReservaStock.RESERVADA)
        );

        almacenValidator.validateCanDeactivate(entity, hasStock, hasMovements, hasPendingReservations);

        entity.inactivar();
        Almacen saved = almacenRepository.save(entity);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.ALMACEN_INACTIVADO,
                EntidadAuditada.ALMACEN,
                String.valueOf(saved.getIdAlmacen()),
                "INACTIVAR_ALMACEN",
                "Almacén inactivado correctamente.",
                auditMetadata(saved, actor, Map.of("motivo", request.motivo()))
        );

        return apiResponseFactory.dtoOk(
                "Almacén inactivado correctamente.",
                almacenMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<AlmacenResponseDto> obtenerPorId(Long idAlmacen) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        almacenPolicy.ensureCanViewAdmin(actor);

        Almacen entity = findRequired(idAlmacen);

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                almacenMapper.toResponse(entity)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<AlmacenDetailResponseDto> obtenerDetalle(Long idAlmacen) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        almacenPolicy.ensureCanViewAdmin(actor);

        Almacen entity = findRequired(idAlmacen);
        Long cantidadSkusConStock = stockSkuRepository.countDistinctSkuByAlmacen(entity.getIdAlmacen());
        Integer stockFisicoTotal = toInteger(stockSkuRepository.sumStockFisicoByAlmacen(entity.getIdAlmacen()));
        Integer stockReservadoTotal = toInteger(stockSkuRepository.sumStockReservadoByAlmacen(entity.getIdAlmacen()));
        Integer stockDisponibleTotal = toInteger(stockSkuRepository.sumStockDisponibleByAlmacen(entity.getIdAlmacen()));

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                almacenMapper.toDetailResponse(
                        entity,
                        cantidadSkusConStock,
                        stockFisicoTotal,
                        stockReservadoTotal,
                        stockDisponibleTotal
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<AlmacenResponseDto>> listar(
            AlmacenFilterDto filter,
            PageRequestDto pageRequest
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        almacenPolicy.ensureCanViewAdmin(actor);

        PageRequestDto safePage = safePageRequest(pageRequest, "createdAt");

        Pageable pageable = paginationService.pageable(
                safePage.page(),
                safePage.size(),
                safePage.sortBy(),
                safePage.sortDirection(),
                ALLOWED_SORT_FIELDS,
                "createdAt"
        );

        PageResponseDto<AlmacenResponseDto> response = paginationService.toPageResponseDto(
                almacenRepository.findAll(AlmacenSpecifications.fromFilter(filter), pageable),
                almacenMapper::toResponse
        );

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<AlmacenOptionDto>> lookup(String search, Integer limit) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        almacenPolicy.ensureCanViewAdmin(actor);

        int safeLimit = sanitizeLimit(limit);
        Pageable pageable = paginationService.pageable(
                0,
                safeLimit,
                "principal",
                "DESC",
                ALLOWED_SORT_FIELDS,
                "principal"
        );

        AlmacenFilterDto filter = AlmacenFilterDto.builder()
                .search(search)
                .estado(Boolean.TRUE)
                .build();

        List<AlmacenOptionDto> options = almacenRepository
                .findAll(AlmacenSpecifications.fromFilter(filter), pageable)
                .stream()
                .map(this::toOption)
                .toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", options);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<AlmacenResponseDto> obtenerPrincipal() {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        almacenPolicy.ensureCanViewAdmin(actor);

        Almacen principal = almacenRepository.findByPrincipalTrueAndEstadoTrue()
                .orElseThrow(() -> new NotFoundException(
                        "ALMACEN_PRINCIPAL_NO_ENCONTRADO",
                        "No se encontró un almacén principal activo."
                ));

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                almacenMapper.toResponse(principal)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<AlmacenResponseDto>> listarParaVenta() {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        almacenPolicy.ensureCanViewAdmin(actor);

        List<AlmacenResponseDto> response = almacenRepository
                .findByPermiteVentaTrueAndEstadoTrueOrderByPrincipalDescNombreAsc()
                .stream()
                .map(almacenMapper::toResponse)
                .toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<AlmacenResponseDto>> listarParaCompra() {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        almacenPolicy.ensureCanViewAdmin(actor);

        List<AlmacenResponseDto> response = almacenRepository
                .findByPermiteCompraTrueAndEstadoTrueOrderByPrincipalDescNombreAsc()
                .stream()
                .map(almacenMapper::toResponse)
                .toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", response);
    }

    private Almacen findRequired(Long idAlmacen) {
        if (idAlmacen == null) {
            throw new ValidationException(
                    "ALMACEN_ID_REQUERIDO",
                    "Debe indicar el almacén solicitado."
            );
        }

        return almacenRepository.findById(idAlmacen)
                .orElseThrow(() -> new NotFoundException(
                        "ALMACEN_NO_ENCONTRADO",
                        "No se encontró el registro solicitado."
                ));
    }

    private AlmacenCreateRequestDto normalizeCreate(AlmacenCreateRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "ALMACEN_REQUEST_REQUERIDO",
                    "Debe enviar los datos del almacén."
            );
        }

        return AlmacenCreateRequestDto.builder()
                .codigo(StringNormalizer.normalizeForCode(request.codigo()))
                .nombre(StringNormalizer.clean(request.nombre()))
                .direccion(StringNormalizer.cleanOrNull(request.direccion()))
                .principal(defaultBoolean(request.principal(), false))
                .permiteVenta(defaultBoolean(request.permiteVenta(), true))
                .permiteCompra(defaultBoolean(request.permiteCompra(), true))
                .observacion(StringNormalizer.cleanOrNull(request.observacion()))
                .build();
    }

    private AlmacenUpdateRequestDto normalizeUpdate(AlmacenUpdateRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "ALMACEN_REQUEST_REQUERIDO",
                    "Debe enviar los datos del almacén."
            );
        }

        return AlmacenUpdateRequestDto.builder()
                .codigo(StringNormalizer.normalizeForCode(request.codigo()))
                .nombre(StringNormalizer.clean(request.nombre()))
                .direccion(StringNormalizer.cleanOrNull(request.direccion()))
                .principal(defaultBoolean(request.principal(), false))
                .permiteVenta(defaultBoolean(request.permiteVenta(), true))
                .permiteCompra(defaultBoolean(request.permiteCompra(), true))
                .observacion(StringNormalizer.cleanOrNull(request.observacion()))
                .build();
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

    private AlmacenOptionDto toOption(Almacen almacen) {
        return AlmacenOptionDto.builder()
                .idAlmacen(almacen.getIdAlmacen())
                .codigo(almacen.getCodigo())
                .nombre(almacen.getNombre())
                .principal(almacen.getPrincipal())
                .permiteVenta(almacen.getPermiteVenta())
                .permiteCompra(almacen.getPermiteCompra())
                .estado(almacen.getEstado())
                .build();
    }

    private Map<String, Object> auditMetadata(
            Almacen almacen,
            AuthenticatedUserContext actor,
            Map<String, Object> extra
    ) {
        java.util.LinkedHashMap<String, Object> metadata = new java.util.LinkedHashMap<>();
        metadata.put("idAlmacen", almacen.getIdAlmacen());
        metadata.put("codigo", almacen.getCodigo());
        metadata.put("nombre", almacen.getNombre());
        metadata.put("principal", Boolean.TRUE.equals(almacen.getPrincipal()));
        metadata.put("permiteVenta", Boolean.TRUE.equals(almacen.getPermiteVenta()));
        metadata.put("permiteCompra", Boolean.TRUE.equals(almacen.getPermiteCompra()));
        metadata.put("actor", actor.actorLabel());
        metadata.put("idUsuarioMs1", actor.getIdUsuarioMs1());

        if (extra != null) {
            metadata.putAll(extra);
        }

        return metadata;
    }

    private int sanitizeLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LOOKUP_LIMIT;
        }

        if (limit < 1) {
            return DEFAULT_LOOKUP_LIMIT;
        }

        return Math.min(limit, MAX_LOOKUP_LIMIT);
    }

    private Boolean defaultBoolean(Boolean value, boolean defaultValue) {
        return value == null ? defaultValue : value;
    }

    private Integer toInteger(Long value) {
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

    private void requireMotivo(String motivo) {
        if (!StringNormalizer.hasText(motivo)) {
            throw new ValidationException(
                    "MOTIVO_REQUERIDO",
                    "Debe indicar el motivo de la operación."
            );
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}