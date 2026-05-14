// ruta: src/main/java/com/upsjb/ms3/service/impl/AtributoServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.Atributo;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.TipoDatoAtributo;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.dto.catalogo.atributo.filter.AtributoFilterDto;
import com.upsjb.ms3.dto.catalogo.atributo.request.AtributoCreateRequestDto;
import com.upsjb.ms3.dto.catalogo.atributo.request.AtributoUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.atributo.response.AtributoDetailResponseDto;
import com.upsjb.ms3.dto.catalogo.atributo.response.AtributoResponseDto;
import com.upsjb.ms3.dto.catalogo.atributo.response.TipoProductoAtributoResponseDto;
import com.upsjb.ms3.dto.reference.response.AtributoOptionDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.mapper.AtributoMapper;
import com.upsjb.ms3.mapper.TipoProductoAtributoMapper;
import com.upsjb.ms3.policy.AtributoPolicy;
import com.upsjb.ms3.repository.AtributoRepository;
import com.upsjb.ms3.repository.EmpleadoInventarioPermisoHistorialRepository;
import com.upsjb.ms3.repository.ProductoAtributoValorRepository;
import com.upsjb.ms3.repository.SkuAtributoValorRepository;
import com.upsjb.ms3.repository.TipoProductoAtributoRepository;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.security.principal.CurrentUserResolver;
import com.upsjb.ms3.service.contract.AtributoService;
import com.upsjb.ms3.service.contract.AuditoriaFuncionalService;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.pagination.PaginationService;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.specification.AtributoSpecifications;
import com.upsjb.ms3.util.StringNormalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.upsjb.ms3.validator.AtributoValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AtributoServiceImpl implements AtributoService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "idAtributo",
            "codigo",
            "nombre",
            "tipoDato",
            "unidadMedida",
            "requerido",
            "filtrable",
            "visiblePublico",
            "estado",
            "createdAt",
            "updatedAt"
    );

    private static final int DEFAULT_LOOKUP_LIMIT = 20;
    private static final int MAX_LOOKUP_LIMIT = 50;

    private final AtributoRepository atributoRepository;
    private final ProductoAtributoValorRepository productoAtributoValorRepository;
    private final SkuAtributoValorRepository skuAtributoValorRepository;
    private final TipoProductoAtributoRepository tipoProductoAtributoRepository;
    private final EmpleadoInventarioPermisoHistorialRepository empleadoInventarioPermisoHistorialRepository;
    private final AtributoMapper atributoMapper;
    private final TipoProductoAtributoMapper tipoProductoAtributoMapper;
    private final AtributoValidator atributoValidator;
    private final AtributoPolicy atributoPolicy;
    private final CurrentUserResolver currentUserResolver;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final PaginationService paginationService;
    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional
    public ApiResponseDto<AtributoResponseDto> crear(AtributoCreateRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        atributoPolicy.ensureCanCreate(actor);

        AtributoCreateRequestDto normalized = normalizeCreate(request);

        atributoValidator.validateCreate(
                normalized.codigo(),
                normalized.nombre(),
                normalized.tipoDato(),
                normalized.unidadMedida(),
                atributoRepository.existsByCodigoIgnoreCaseAndEstadoTrue(normalized.codigo()),
                atributoRepository.existsByNombreIgnoreCaseAndEstadoTrue(normalized.nombre())
        );

        Atributo entity = atributoMapper.toEntity(normalized);
        Atributo saved = atributoRepository.save(entity);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.ATRIBUTO_CREADO,
                EntidadAuditada.ATRIBUTO,
                String.valueOf(saved.getIdAtributo()),
                "CREAR_ATRIBUTO",
                "Atributo creado correctamente.",
                auditMetadata(saved, actor, null)
        );

        log.info(
                "Atributo creado. idAtributo={}, codigo={}, actor={}",
                saved.getIdAtributo(),
                saved.getCodigo(),
                actor.actorLabel()
        );

        return apiResponseFactory.dtoCreated(
                "Atributo creado correctamente.",
                atributoMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<AtributoResponseDto> actualizar(Long idAtributo, AtributoUpdateRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        boolean employeeCanUpdateAttributes = employeeCanUpdateAttributes(actor);
        atributoPolicy.ensureCanUpdate(actor, employeeCanUpdateAttributes);

        Atributo entity = findRequired(idAtributo);
        AtributoUpdateRequestDto normalized = normalizeUpdate(request);

        boolean hasExistingValues = hasExistingValues(entity.getIdAtributo());

        atributoValidator.validateUpdate(
                entity,
                normalized.codigo(),
                normalized.nombre(),
                normalized.tipoDato(),
                normalized.unidadMedida(),
                atributoRepository.existsByCodigoIgnoreCaseAndEstadoTrueAndIdAtributoNot(
                        normalized.codigo(),
                        entity.getIdAtributo()
                ),
                atributoRepository.existsByNombreIgnoreCaseAndEstadoTrueAndIdAtributoNot(
                        normalized.nombre(),
                        entity.getIdAtributo()
                ),
                hasExistingValues
        );

        Map<String, Object> before = Map.of(
                "codigo", safe(entity.getCodigo()),
                "nombre", safe(entity.getNombre()),
                "tipoDato", entity.getTipoDato() == null ? "" : entity.getTipoDato().getCode(),
                "filtrable", Boolean.TRUE.equals(entity.getFiltrable()),
                "visiblePublico", Boolean.TRUE.equals(entity.getVisiblePublico())
        );

        atributoMapper.updateEntity(entity, normalized);
        Atributo saved = atributoRepository.save(entity);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.ATRIBUTO_ACTUALIZADO,
                EntidadAuditada.ATRIBUTO,
                String.valueOf(saved.getIdAtributo()),
                "ACTUALIZAR_ATRIBUTO",
                "Atributo actualizado correctamente.",
                auditMetadata(saved, actor, before)
        );

        log.info(
                "Atributo actualizado. idAtributo={}, codigo={}, actor={}",
                saved.getIdAtributo(),
                saved.getCodigo(),
                actor.actorLabel()
        );

        return apiResponseFactory.dtoOk(
                "Atributo actualizado correctamente.",
                atributoMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<AtributoResponseDto> cambiarEstado(Long idAtributo, EstadoChangeRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        atributoPolicy.ensureCanChangeState(actor);

        if (request == null || request.estado() == null) {
            throw new ValidationException(
                    "ATRIBUTO_ESTADO_REQUERIDO",
                    "Debe indicar el estado solicitado."
            );
        }

        requireMotivo(request.motivo());

        Atributo entity = findRequired(idAtributo);

        if (Objects.equals(entity.getEstado(), request.estado())) {
            return apiResponseFactory.dtoOk(
                    "Operación realizada correctamente.",
                    atributoMapper.toResponse(entity)
            );
        }

        if (Boolean.TRUE.equals(request.estado())) {
            atributoValidator.validateCanActivate(entity);
            entity.activar();
            Atributo saved = atributoRepository.save(entity);

            auditoriaFuncionalService.registrarExito(
                    TipoEventoAuditoria.ATRIBUTO_ACTIVADO,
                    EntidadAuditada.ATRIBUTO,
                    String.valueOf(saved.getIdAtributo()),
                    "ACTIVAR_ATRIBUTO",
                    "Atributo activado correctamente.",
                    auditMetadata(saved, actor, Map.of("motivo", request.motivo()))
            );

            return apiResponseFactory.dtoOk(
                    "Atributo activado correctamente.",
                    atributoMapper.toResponse(saved)
            );
        }

        boolean hasAssociations = tipoProductoAtributoRepository.countByAtributo_IdAtributoAndEstadoTrue(
                entity.getIdAtributo()
        ) > 0;

        atributoValidator.validateCanDeactivate(
                entity,
                hasAssociations,
                hasExistingValues(entity.getIdAtributo())
        );

        entity.inactivar();
        Atributo saved = atributoRepository.save(entity);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.ATRIBUTO_INACTIVADO,
                EntidadAuditada.ATRIBUTO,
                String.valueOf(saved.getIdAtributo()),
                "INACTIVAR_ATRIBUTO",
                "Atributo inactivado correctamente.",
                auditMetadata(saved, actor, Map.of("motivo", request.motivo()))
        );

        return apiResponseFactory.dtoOk(
                "Atributo inactivado correctamente.",
                atributoMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<AtributoResponseDto> obtenerPorId(Long idAtributo) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        atributoPolicy.ensureCanViewAdmin(actor);

        Atributo entity = findRequired(idAtributo);

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                atributoMapper.toResponse(entity)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<AtributoDetailResponseDto> obtenerDetalle(Long idAtributo) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        atributoPolicy.ensureCanViewAdmin(actor);

        Atributo entity = findRequired(idAtributo);

        Long cantidadValoresProducto = productoAtributoValorRepository.countByAtributo_IdAtributoAndEstadoTrue(
                entity.getIdAtributo()
        );

        Long cantidadValoresSku = skuAtributoValorRepository.countByAtributo_IdAtributoAndEstadoTrue(
                entity.getIdAtributo()
        );

        List<TipoProductoAtributoResponseDto> asociaciones = tipoProductoAtributoRepository
                .findByAtributo_IdAtributoAndEstadoTrueOrderByIdTipoProductoAtributoAsc(entity.getIdAtributo())
                .stream()
                .map(tipoProductoAtributoMapper::toResponse)
                .toList();

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                atributoMapper.toDetailResponse(
                        entity,
                        cantidadValoresProducto,
                        cantidadValoresSku,
                        asociaciones
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<AtributoResponseDto>> listar(
            AtributoFilterDto filter,
            PageRequestDto pageRequest
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        atributoPolicy.ensureCanViewAdmin(actor);

        PageRequestDto safePage = safePageRequest(pageRequest, "createdAt");

        Pageable pageable = paginationService.pageable(
                safePage.page(),
                safePage.size(),
                safePage.sortBy(),
                safePage.sortDirection(),
                ALLOWED_SORT_FIELDS,
                "createdAt"
        );

        PageResponseDto<AtributoResponseDto> response = paginationService.toPageResponseDto(
                atributoRepository.findAll(AtributoSpecifications.fromFilter(filter), pageable),
                atributoMapper::toResponse
        );

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<AtributoOptionDto>> lookup(String search, Integer limit) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        atributoPolicy.ensureCanViewAdmin(actor);

        int safeLimit = sanitizeLimit(limit);

        Pageable pageable = paginationService.pageable(
                0,
                safeLimit,
                "nombre",
                "ASC",
                ALLOWED_SORT_FIELDS,
                "nombre"
        );

        AtributoFilterDto filter = AtributoFilterDto.builder()
                .search(search)
                .estado(Boolean.TRUE)
                .build();

        List<AtributoOptionDto> options = atributoRepository
                .findAll(AtributoSpecifications.fromFilter(filter), pageable)
                .stream()
                .map(this::toOption)
                .toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", options);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<AtributoResponseDto>> listarFiltrables() {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        atributoPolicy.ensureCanViewAdmin(actor);

        List<AtributoResponseDto> response = atributoRepository
                .findByFiltrableTrueAndEstadoTrueOrderByNombreAsc()
                .stream()
                .map(atributoMapper::toResponse)
                .toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<AtributoResponseDto>> listarVisiblesPublico() {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        atributoPolicy.ensureCanViewAdmin(actor);

        List<AtributoResponseDto> response = atributoRepository
                .findByVisiblePublicoTrueAndEstadoTrueOrderByNombreAsc()
                .stream()
                .map(atributoMapper::toResponse)
                .toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<AtributoResponseDto>> listarPorTipoDato(TipoDatoAtributo tipoDato) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        atributoPolicy.ensureCanViewAdmin(actor);

        if (tipoDato == null) {
            throw new ValidationException(
                    "TIPO_DATO_ATRIBUTO_REQUERIDO",
                    "Debe indicar el tipo de dato del atributo."
            );
        }

        List<AtributoResponseDto> response = atributoRepository
                .findByTipoDatoAndEstadoTrueOrderByNombreAsc(tipoDato)
                .stream()
                .map(atributoMapper::toResponse)
                .toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", response);
    }

    private Atributo findRequired(Long idAtributo) {
        if (idAtributo == null) {
            throw new ValidationException(
                    "ATRIBUTO_ID_REQUERIDO",
                    "Debe indicar el atributo solicitado."
            );
        }

        return atributoRepository.findById(idAtributo)
                .orElseThrow(() -> new NotFoundException(
                        "ATRIBUTO_NO_ENCONTRADO",
                        "No se encontró el registro solicitado."
                ));
    }

    private AtributoCreateRequestDto normalizeCreate(AtributoCreateRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "ATRIBUTO_REQUEST_REQUERIDO",
                    "Debe enviar los datos del atributo."
            );
        }

        return AtributoCreateRequestDto.builder()
                .codigo(StringNormalizer.normalizeForCode(request.codigo()))
                .nombre(StringNormalizer.clean(request.nombre()))
                .tipoDato(request.tipoDato())
                .unidadMedida(StringNormalizer.cleanOrNull(request.unidadMedida()))
                .requerido(defaultBoolean(request.requerido(), false))
                .filtrable(defaultBoolean(request.filtrable(), false))
                .visiblePublico(defaultBoolean(request.visiblePublico(), true))
                .build();
    }

    private AtributoUpdateRequestDto normalizeUpdate(AtributoUpdateRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "ATRIBUTO_REQUEST_REQUERIDO",
                    "Debe enviar los datos del atributo."
            );
        }

        return AtributoUpdateRequestDto.builder()
                .codigo(StringNormalizer.normalizeForCode(request.codigo()))
                .nombre(StringNormalizer.clean(request.nombre()))
                .tipoDato(request.tipoDato())
                .unidadMedida(StringNormalizer.cleanOrNull(request.unidadMedida()))
                .requerido(defaultBoolean(request.requerido(), false))
                .filtrable(defaultBoolean(request.filtrable(), false))
                .visiblePublico(defaultBoolean(request.visiblePublico(), true))
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

    private boolean hasExistingValues(Long idAtributo) {
        return productoAtributoValorRepository.countByAtributo_IdAtributoAndEstadoTrue(idAtributo) > 0
                || skuAtributoValorRepository.countByAtributo_IdAtributoAndEstadoTrue(idAtributo) > 0;
    }

    private boolean employeeCanUpdateAttributes(AuthenticatedUserContext actor) {
        if (actor == null || actor.getIdUsuarioMs1() == null || !actor.isEmpleado()) {
            return false;
        }

        return empleadoInventarioPermisoHistorialRepository
                .existsByEmpleadoSnapshot_IdUsuarioMs1AndVigenteTrueAndEstadoTrueAndPuedeActualizarAtributosTrue(
                        actor.getIdUsuarioMs1()
                );
    }

    private AtributoOptionDto toOption(Atributo atributo) {
        return AtributoOptionDto.builder()
                .idAtributo(atributo.getIdAtributo())
                .codigo(atributo.getCodigo())
                .nombre(atributo.getNombre())
                .tipoDato(atributo.getTipoDato())
                .tipoDatoLabel(atributo.getTipoDato() == null ? null : atributo.getTipoDato().getLabel())
                .unidadMedida(atributo.getUnidadMedida())
                .requerido(atributo.getRequerido())
                .filtrable(atributo.getFiltrable())
                .visiblePublico(atributo.getVisiblePublico())
                .estado(atributo.getEstado())
                .build();
    }

    private Map<String, Object> auditMetadata(
            Atributo atributo,
            AuthenticatedUserContext actor,
            Map<String, Object> extra
    ) {
        LinkedHashMap<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("idAtributo", atributo.getIdAtributo());
        metadata.put("codigo", atributo.getCodigo());
        metadata.put("nombre", atributo.getNombre());
        metadata.put("tipoDato", atributo.getTipoDato() == null ? null : atributo.getTipoDato().getCode());
        metadata.put("unidadMedida", atributo.getUnidadMedida());
        metadata.put("requerido", Boolean.TRUE.equals(atributo.getRequerido()));
        metadata.put("filtrable", Boolean.TRUE.equals(atributo.getFiltrable()));
        metadata.put("visiblePublico", Boolean.TRUE.equals(atributo.getVisiblePublico()));
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