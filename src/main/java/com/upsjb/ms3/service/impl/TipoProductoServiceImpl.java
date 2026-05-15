// ruta: src/main/java/com/upsjb/ms3/service/impl/TipoProductoServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.TipoProducto;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.dto.catalogo.atributo.response.TipoProductoAtributoResponseDto;
import com.upsjb.ms3.dto.catalogo.tipoproducto.filter.TipoProductoFilterDto;
import com.upsjb.ms3.dto.catalogo.tipoproducto.request.TipoProductoCreateRequestDto;
import com.upsjb.ms3.dto.catalogo.tipoproducto.request.TipoProductoUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.tipoproducto.response.TipoProductoDetailResponseDto;
import com.upsjb.ms3.dto.catalogo.tipoproducto.response.TipoProductoResponseDto;
import com.upsjb.ms3.dto.reference.response.TipoProductoOptionDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.mapper.ReferenceMapper;
import com.upsjb.ms3.mapper.TipoProductoAtributoMapper;
import com.upsjb.ms3.mapper.TipoProductoMapper;
import com.upsjb.ms3.policy.TipoProductoPolicy;
import com.upsjb.ms3.repository.ProductoRepository;
import com.upsjb.ms3.repository.TipoProductoAtributoRepository;
import com.upsjb.ms3.repository.TipoProductoRepository;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.security.principal.CurrentUserResolver;
import com.upsjb.ms3.service.contract.AuditoriaFuncionalService;
import com.upsjb.ms3.service.contract.EmpleadoInventarioPermisoService;
import com.upsjb.ms3.service.contract.TipoProductoService;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.outbox.ProductoSnapshotOutboxRegistrar;
import com.upsjb.ms3.shared.pagination.PaginationService;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.specification.TipoProductoSpecifications;
import com.upsjb.ms3.util.StringNormalizer;
import com.upsjb.ms3.validator.TipoProductoValidator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TipoProductoServiceImpl implements TipoProductoService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "idTipoProducto",
            "codigo",
            "nombre",
            "descripcion",
            "estado",
            "createdAt",
            "updatedAt"
    );

    private static final int DEFAULT_LOOKUP_LIMIT = 20;
    private static final int MAX_LOOKUP_LIMIT = 50;
    private static final int MAX_SEARCH_LENGTH = 250;
    private static final int MAX_CODIGO_LENGTH = 50;
    private static final int MAX_NOMBRE_LENGTH = 120;
    private static final int MAX_DESCRIPCION_LENGTH = 300;
    private static final int MAX_MOTIVO_LENGTH = 500;

    private final TipoProductoRepository tipoProductoRepository;
    private final TipoProductoAtributoRepository tipoProductoAtributoRepository;
    private final ProductoRepository productoRepository;
    private final TipoProductoMapper tipoProductoMapper;
    private final TipoProductoAtributoMapper tipoProductoAtributoMapper;
    private final ReferenceMapper referenceMapper;
    private final TipoProductoValidator tipoProductoValidator;
    private final TipoProductoPolicy tipoProductoPolicy;
    private final CurrentUserResolver currentUserResolver;
    private final EmpleadoInventarioPermisoService empleadoInventarioPermisoService;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final ProductoSnapshotOutboxRegistrar productoSnapshotOutboxRegistrar;
    private final PaginationService paginationService;
    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional
    public ApiResponseDto<TipoProductoResponseDto> crear(TipoProductoCreateRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        tipoProductoPolicy.ensureCanCreate(actor, employeeCanCreateProductBasic(actor));

        TipoProductoCreateRequestDto normalized = normalizeCreate(request);

        tipoProductoValidator.validateCreate(
                normalized.codigo(),
                normalized.nombre(),
                normalized.descripcion(),
                tipoProductoRepository.existsByCodigoIgnoreCaseAndEstadoTrue(normalized.codigo()),
                tipoProductoRepository.existsByNombreIgnoreCaseAndEstadoTrue(normalized.nombre())
        );

        TipoProducto entity = tipoProductoMapper.toEntity(normalized);
        entity.activar();

        TipoProducto saved = tipoProductoRepository.saveAndFlush(entity);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.TIPO_PRODUCTO_CREADO,
                EntidadAuditada.TIPO_PRODUCTO,
                String.valueOf(saved.getIdTipoProducto()),
                "CREAR_TIPO_PRODUCTO",
                "Tipo de producto creado correctamente.",
                auditMetadata(saved, actor, null)
        );

        log.info(
                "Tipo de producto creado. idTipoProducto={}, codigo={}, actor={}",
                saved.getIdTipoProducto(),
                saved.getCodigo(),
                actor.actorLabel()
        );

        return apiResponseFactory.dtoCreated(
                "Tipo de producto creado correctamente.",
                tipoProductoMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<TipoProductoResponseDto> actualizar(
            Long idTipoProducto,
            TipoProductoUpdateRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        tipoProductoPolicy.ensureCanUpdate(actor, employeeCanEditProductBasic(actor));

        TipoProducto entity = findAnyRequired(idTipoProducto);
        TipoProductoUpdateRequestDto normalized = normalizeUpdate(request);

        tipoProductoValidator.validateUpdate(
                entity,
                normalized.codigo(),
                normalized.nombre(),
                normalized.descripcion(),
                tipoProductoRepository.existsByCodigoIgnoreCaseAndEstadoTrueAndIdTipoProductoNot(
                        normalized.codigo(),
                        entity.getIdTipoProducto()
                ),
                tipoProductoRepository.existsByNombreIgnoreCaseAndEstadoTrueAndIdTipoProductoNot(
                        normalized.nombre(),
                        entity.getIdTipoProducto()
                )
        );

        Map<String, Object> before = auditSnapshot(entity);

        tipoProductoMapper.updateEntity(entity, normalized);
        TipoProducto saved = tipoProductoRepository.saveAndFlush(entity);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.TIPO_PRODUCTO_ACTUALIZADO,
                EntidadAuditada.TIPO_PRODUCTO,
                String.valueOf(saved.getIdTipoProducto()),
                "ACTUALIZAR_TIPO_PRODUCTO",
                "Tipo de producto actualizado correctamente.",
                auditMetadata(saved, actor, Map.of("before", before))
        );

        productoSnapshotOutboxRegistrar.registrarProductosDeTipoActualizados(
                saved.getIdTipoProducto(),
                "TipoProductoService",
                Map.of("accion", "ACTUALIZAR_TIPO_PRODUCTO")
        );

        log.info(
                "Tipo de producto actualizado. idTipoProducto={}, codigo={}, actor={}",
                saved.getIdTipoProducto(),
                saved.getCodigo(),
                actor.actorLabel()
        );

        return apiResponseFactory.dtoOk(
                "Tipo de producto actualizado correctamente.",
                tipoProductoMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<TipoProductoResponseDto> cambiarEstado(
            Long idTipoProducto,
            EstadoChangeRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        tipoProductoPolicy.ensureCanChangeState(actor);

        String motivo = normalizeEstadoRequestAndReturnMotivo(request);
        TipoProducto entity = findAnyRequired(idTipoProducto);

        if (Objects.equals(entity.getEstado(), request.estado())) {
            return apiResponseFactory.dtoOk(
                    "Operación realizada correctamente.",
                    tipoProductoMapper.toResponse(entity)
            );
        }

        if (Boolean.TRUE.equals(request.estado())) {
            tipoProductoValidator.validateCanActivate(entity);
            entity.activar();

            TipoProducto saved = tipoProductoRepository.saveAndFlush(entity);

            auditoriaFuncionalService.registrarExito(
                    TipoEventoAuditoria.TIPO_PRODUCTO_ACTIVADO,
                    EntidadAuditada.TIPO_PRODUCTO,
                    String.valueOf(saved.getIdTipoProducto()),
                    "ACTIVAR_TIPO_PRODUCTO",
                    "Tipo de producto activado correctamente.",
                    auditMetadata(saved, actor, Map.of("motivo", motivo))
            );

            productoSnapshotOutboxRegistrar.registrarProductosDeTipoActualizados(
                    saved.getIdTipoProducto(),
                    "TipoProductoService",
                    Map.of("accion", "ACTIVAR_TIPO_PRODUCTO", "motivo", motivo)
            );

            log.info(
                    "Tipo de producto activado. idTipoProducto={}, codigo={}, actor={}",
                    saved.getIdTipoProducto(),
                    saved.getCodigo(),
                    actor.actorLabel()
            );

            return apiResponseFactory.dtoOk(
                    "Tipo de producto activado correctamente.",
                    tipoProductoMapper.toResponse(saved)
            );
        }

        boolean hasActiveProducts = productoRepository.existsByTipoProducto_IdTipoProductoAndEstadoTrue(
                entity.getIdTipoProducto()
        );

        tipoProductoValidator.validateCanDeactivate(entity, hasActiveProducts);
        entity.inactivar();

        TipoProducto saved = tipoProductoRepository.saveAndFlush(entity);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.TIPO_PRODUCTO_INACTIVADO,
                EntidadAuditada.TIPO_PRODUCTO,
                String.valueOf(saved.getIdTipoProducto()),
                "INACTIVAR_TIPO_PRODUCTO",
                "Tipo de producto inactivado correctamente.",
                auditMetadata(saved, actor, Map.of("motivo", motivo))
        );

        productoSnapshotOutboxRegistrar.registrarProductosDeTipoActualizados(
                saved.getIdTipoProducto(),
                "TipoProductoService",
                Map.of("accion", "INACTIVAR_TIPO_PRODUCTO", "motivo", motivo)
        );

        log.info(
                "Tipo de producto inactivado. idTipoProducto={}, codigo={}, actor={}",
                saved.getIdTipoProducto(),
                saved.getCodigo(),
                actor.actorLabel()
        );

        return apiResponseFactory.dtoOk(
                "Tipo de producto inactivado correctamente.",
                tipoProductoMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<TipoProductoResponseDto> obtenerPorId(Long idTipoProducto) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        tipoProductoPolicy.ensureCanViewAdmin(actor);

        TipoProducto entity = findAnyRequired(idTipoProducto);

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                tipoProductoMapper.toResponse(entity)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<TipoProductoDetailResponseDto> obtenerDetalle(Long idTipoProducto) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        tipoProductoPolicy.ensureCanViewAdmin(actor);

        TipoProducto entity = findAnyRequired(idTipoProducto);

        Long cantidadAtributos = tipoProductoAtributoRepository.countByTipoProducto_IdTipoProductoAndEstadoTrue(
                entity.getIdTipoProducto()
        );

        Long cantidadProductos = productoRepository.countByTipoProducto_IdTipoProductoAndEstadoTrue(
                entity.getIdTipoProducto()
        );

        List<TipoProductoAtributoResponseDto> atributos = tipoProductoAtributoRepository
                .findByTipoProducto_IdTipoProductoAndEstadoTrueOrderByOrdenAscIdTipoProductoAtributoAsc(
                        entity.getIdTipoProducto()
                )
                .stream()
                .map(tipoProductoAtributoMapper::toResponse)
                .toList();

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                tipoProductoMapper.toDetailResponse(entity, cantidadAtributos, cantidadProductos, atributos)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<TipoProductoResponseDto>> listar(
            TipoProductoFilterDto filter,
            PageRequestDto pageRequest
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        tipoProductoPolicy.ensureCanViewAdmin(actor);

        PageRequestDto safePage = safePageRequest(pageRequest, "createdAt");

        Pageable pageable = paginationService.pageable(
                safePage.page(),
                safePage.size(),
                safePage.sortBy(),
                safePage.sortDirection(),
                ALLOWED_SORT_FIELDS,
                "createdAt"
        );

        TipoProductoFilterDto normalizedFilter = normalizeFilter(filter);

        PageResponseDto<TipoProductoResponseDto> response = paginationService.toPageResponseDto(
                tipoProductoRepository.findAll(TipoProductoSpecifications.fromFilter(normalizedFilter), pageable),
                tipoProductoMapper::toResponse
        );

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<TipoProductoOptionDto>> lookup(String search, Integer limit) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        tipoProductoPolicy.ensureCanViewAdmin(actor);

        Pageable pageable = paginationService.pageable(
                0,
                sanitizeLimit(limit),
                "nombre",
                "ASC",
                ALLOWED_SORT_FIELDS,
                "nombre"
        );

        TipoProductoFilterDto filter = TipoProductoFilterDto.builder()
                .search(StringNormalizer.truncateOrNull(search, MAX_SEARCH_LENGTH))
                .estado(Boolean.TRUE)
                .incluirTodosLosEstados(Boolean.FALSE)
                .build();

        List<TipoProductoOptionDto> response = tipoProductoRepository
                .findAll(TipoProductoSpecifications.fromFilter(filter), pageable)
                .stream()
                .map(referenceMapper::toTipoProductoOption)
                .toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", response);
    }

    private TipoProducto findAnyRequired(Long idTipoProducto) {
        if (idTipoProducto == null) {
            throw new ValidationException(
                    "TIPO_PRODUCTO_ID_REQUERIDO",
                    "Debe indicar el tipo de producto solicitado."
            );
        }

        return tipoProductoRepository.findById(idTipoProducto)
                .orElseThrow(() -> new NotFoundException(
                        "TIPO_PRODUCTO_NO_ENCONTRADO",
                        "No se encontró el registro solicitado."
                ));
    }

    private TipoProductoCreateRequestDto normalizeCreate(TipoProductoCreateRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "TIPO_PRODUCTO_REQUEST_REQUERIDO",
                    "Debe enviar los datos del tipo de producto."
            );
        }

        return TipoProductoCreateRequestDto.builder()
                .codigo(normalizeCodeOrNull(request.codigo()))
                .nombre(StringNormalizer.cleanOrNull(request.nombre()))
                .descripcion(StringNormalizer.cleanOrNull(request.descripcion()))
                .build();
    }

    private TipoProductoUpdateRequestDto normalizeUpdate(TipoProductoUpdateRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "TIPO_PRODUCTO_REQUEST_REQUERIDO",
                    "Debe enviar los datos del tipo de producto."
            );
        }

        return TipoProductoUpdateRequestDto.builder()
                .codigo(normalizeCodeOrNull(request.codigo()))
                .nombre(StringNormalizer.cleanOrNull(request.nombre()))
                .descripcion(StringNormalizer.cleanOrNull(request.descripcion()))
                .build();
    }

    private TipoProductoFilterDto normalizeFilter(TipoProductoFilterDto filter) {
        if (filter == null) {
            return null;
        }

        return TipoProductoFilterDto.builder()
                .search(StringNormalizer.truncateOrNull(filter.search(), MAX_SEARCH_LENGTH))
                .codigo(normalizeCodeOrNull(filter.codigo()))
                .nombre(StringNormalizer.truncateOrNull(filter.nombre(), MAX_NOMBRE_LENGTH))
                .estado(filter.estado())
                .incluirTodosLosEstados(Boolean.TRUE.equals(filter.incluirTodosLosEstados()))
                .fechaCreacion(filter.fechaCreacion())
                .fechaActualizacion(filter.fechaActualizacion())
                .build();
    }

    private String normalizeEstadoRequestAndReturnMotivo(EstadoChangeRequestDto request) {
        if (request == null || request.estado() == null) {
            throw new ValidationException(
                    "TIPO_PRODUCTO_ESTADO_REQUERIDO",
                    "Debe indicar el estado solicitado."
            );
        }

        String motivo = StringNormalizer.cleanOrNull(request.motivo());

        if (!StringNormalizer.hasText(motivo)) {
            throw new ValidationException(
                    "TIPO_PRODUCTO_MOTIVO_REQUERIDO",
                    "Debe indicar el motivo de la operación."
            );
        }

        if (motivo.length() > MAX_MOTIVO_LENGTH) {
            throw new ValidationException(
                    "TIPO_PRODUCTO_MOTIVO_INVALIDO",
                    "El motivo no debe superar 500 caracteres."
            );
        }

        return motivo;
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

    private int sanitizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_LOOKUP_LIMIT;
        }

        return Math.min(limit, MAX_LOOKUP_LIMIT);
    }

    private boolean employeeCanCreateProductBasic(AuthenticatedUserContext actor) {
        return actor != null
                && actor.isEmpleado()
                && actor.getIdUsuarioMs1() != null
                && empleadoInventarioPermisoService.puedeCrearProductoBasico(actor.getIdUsuarioMs1());
    }

    private boolean employeeCanEditProductBasic(AuthenticatedUserContext actor) {
        return actor != null
                && actor.isEmpleado()
                && actor.getIdUsuarioMs1() != null
                && empleadoInventarioPermisoService.puedeEditarProductoBasico(actor.getIdUsuarioMs1());
    }

    private String normalizeCodeOrNull(String value) {
        String normalized = StringNormalizer.normalizeForCode(value);

        if (!StringNormalizer.hasText(normalized)) {
            return null;
        }

        return normalized.length() > MAX_CODIGO_LENGTH
                ? normalized
                : normalized;
    }

    private Map<String, Object> auditMetadata(
            TipoProducto tipoProducto,
            AuthenticatedUserContext actor,
            Map<String, Object> extra
    ) {
        Map<String, Object> metadata = auditSnapshot(tipoProducto);
        metadata.put("actor", actor.actorLabel());
        metadata.put("idUsuarioMs1", actor.getIdUsuarioMs1());

        if (extra != null && !extra.isEmpty()) {
            metadata.putAll(extra);
        }

        return metadata;
    }

    private Map<String, Object> auditSnapshot(TipoProducto tipoProducto) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("idTipoProducto", tipoProducto.getIdTipoProducto());
        metadata.put("codigo", tipoProducto.getCodigo());
        metadata.put("nombre", tipoProducto.getNombre());
        metadata.put("descripcion", tipoProducto.getDescripcion());
        metadata.put("estado", tipoProducto.getEstado());
        return metadata;
    }
}