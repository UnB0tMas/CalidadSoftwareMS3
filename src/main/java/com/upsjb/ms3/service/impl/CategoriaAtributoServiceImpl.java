// ruta: src/main/java/com/upsjb/ms3/service/impl/CategoriaAtributoServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.Atributo;
import com.upsjb.ms3.domain.entity.Categoria;
import com.upsjb.ms3.domain.entity.CategoriaAtributo;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.dto.catalogo.atributo.filter.CategoriaAtributoFilterDto;
import com.upsjb.ms3.dto.catalogo.atributo.request.CategoriaAtributoAssignRequestDto;
import com.upsjb.ms3.dto.catalogo.atributo.request.CategoriaAtributoUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.atributo.response.CategoriaAtributoResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.mapper.CategoriaAtributoMapper;
import com.upsjb.ms3.policy.AtributoPolicy;
import com.upsjb.ms3.repository.ProductoAtributoValorRepository;
import com.upsjb.ms3.repository.SkuAtributoValorRepository;
import com.upsjb.ms3.repository.CategoriaAtributoRepository;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.security.principal.CurrentUserResolver;
import com.upsjb.ms3.service.contract.AuditoriaFuncionalService;
import com.upsjb.ms3.service.contract.EmpleadoInventarioPermisoService;
import com.upsjb.ms3.service.contract.CategoriaAtributoService;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.outbox.ProductoSnapshotOutboxRegistrar;
import com.upsjb.ms3.shared.pagination.PaginationService;
import com.upsjb.ms3.shared.reference.AtributoReferenceResolver;
import com.upsjb.ms3.shared.reference.CategoriaReferenceResolver;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.specification.CategoriaAtributoSpecifications;
import com.upsjb.ms3.util.StringNormalizer;
import com.upsjb.ms3.validator.CategoriaAtributoValidator;
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
public class CategoriaAtributoServiceImpl implements CategoriaAtributoService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "idCategoriaAtributo",
            "categoria.codigo",
            "categoria.nombre",
            "atributo.codigo",
            "atributo.nombre",
            "atributo.tipoDato",
            "requerido",
            "orden",
            "estado",
            "createdAt",
            "updatedAt"
    );

    private final CategoriaAtributoRepository categoriaAtributoRepository;
    private final ProductoAtributoValorRepository productoAtributoValorRepository;
    private final SkuAtributoValorRepository skuAtributoValorRepository;
    private final CategoriaReferenceResolver categoriaReferenceResolver;
    private final AtributoReferenceResolver atributoReferenceResolver;
    private final CategoriaAtributoMapper categoriaAtributoMapper;
    private final CategoriaAtributoValidator categoriaAtributoValidator;
    private final AtributoPolicy atributoPolicy;
    private final CurrentUserResolver currentUserResolver;
    private final EmpleadoInventarioPermisoService empleadoInventarioPermisoService;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final ProductoSnapshotOutboxRegistrar productoSnapshotOutboxRegistrar;
    private final PaginationService paginationService;
    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional
    public ApiResponseDto<CategoriaAtributoResponseDto> asignar(CategoriaAtributoAssignRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        atributoPolicy.ensureCanAssignToCategory(actor, employeeCanUpdateAttributes(actor));

        CategoriaAtributoAssignRequestDto normalized = normalizeAssign(request);
        Categoria categoria = resolveCategoria(normalized.categoria());
        Atributo atributo = resolveAtributo(normalized.atributo());

        boolean duplicatedActive = categoriaAtributoRepository
                .existsDirectActiveAssociation(
                        categoria.getIdCategoria(),
                        atributo.getIdAtributo()
                );

        categoriaAtributoValidator.validateAssign(
                categoria,
                atributo,
                normalized.orden(),
                duplicatedActive
        );

        CategoriaAtributo entity = categoriaAtributoRepository
                .findFirstByCategoria_IdCategoriaAndAtributo_IdAtributoOrderByIdCategoriaAtributoDesc(
                        categoria.getIdCategoria(),
                        atributo.getIdAtributo()
                )
                .orElseGet(() -> categoriaAtributoMapper.toEntity(normalized, categoria, atributo));

        entity.setCategoria(categoria);
        entity.setAtributo(atributo);
        categoriaAtributoMapper.updateEntity(entity, normalized.requerido(), normalized.orden());
        entity.activar();

        CategoriaAtributo saved = categoriaAtributoRepository.saveAndFlush(entity);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.CATEGORIA_ATRIBUTO_ASIGNADO,
                EntidadAuditada.CATEGORIA_ATRIBUTO,
                String.valueOf(saved.getIdCategoriaAtributo()),
                "ASIGNAR_ATRIBUTO_CATEGORIA",
                "Operación realizada correctamente.",
                auditMetadata(saved, actor, Map.of("motivo", safeMotivo(normalized.motivo())))
        );

        registrarOutboxCategoriaAfectado(saved, "ASIGNAR_ATRIBUTO_CATEGORIA");

        log.info(
                "Atributo asociado a categoría. idCategoriaAtributo={}, idCategoria={}, idAtributo={}, actor={}",
                saved.getIdCategoriaAtributo(),
                categoria.getIdCategoria(),
                atributo.getIdAtributo(),
                actor.actorLabel()
        );

        return apiResponseFactory.dtoCreated(
                "Operación realizada correctamente.",
                categoriaAtributoMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<CategoriaAtributoResponseDto> actualizar(
            Long idCategoriaAtributo,
            CategoriaAtributoUpdateRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        atributoPolicy.ensureCanAssignToCategory(actor, employeeCanUpdateAttributes(actor));

        CategoriaAtributo entity = findActiveRequired(idCategoriaAtributo);
        CategoriaAtributoUpdateRequestDto normalized = normalizeUpdate(request);

        categoriaAtributoValidator.validateUpdate(entity, normalized.orden());

        Map<String, Object> before = auditSnapshot(entity);

        categoriaAtributoMapper.updateEntity(entity, normalized.requerido(), normalized.orden());
        CategoriaAtributo saved = categoriaAtributoRepository.saveAndFlush(entity);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.CATEGORIA_ATRIBUTO_ACTUALIZADO,
                EntidadAuditada.CATEGORIA_ATRIBUTO,
                String.valueOf(saved.getIdCategoriaAtributo()),
                "ACTUALIZAR_ATRIBUTO_CATEGORIA",
                "Operación realizada correctamente.",
                auditMetadata(saved, actor, merge(before, "motivo", safeMotivo(normalized.motivo())))
        );

        registrarOutboxCategoriaAfectado(saved, "ACTUALIZAR_ATRIBUTO_CATEGORIA");

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                categoriaAtributoMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<CategoriaAtributoResponseDto> cambiarEstado(
            Long idCategoriaAtributo,
            EstadoChangeRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        atributoPolicy.ensureCanAssignToCategory(actor, employeeCanUpdateAttributes(actor));

        validateEstadoRequest(request);

        CategoriaAtributo entity = findAnyRequired(idCategoriaAtributo);

        if (Objects.equals(entity.getEstado(), request.estado())) {
            return apiResponseFactory.dtoOk(
                    "Operación realizada correctamente.",
                    categoriaAtributoMapper.toResponse(entity)
            );
        }

        if (Boolean.TRUE.equals(request.estado())) {
            boolean duplicatedActive = categoriaAtributoRepository
                    .existsByCategoria_IdCategoriaAndAtributo_IdAtributoAndEstadoTrueAndIdCategoriaAtributoNot(
                            entity.getCategoria().getIdCategoria(),
                            entity.getAtributo().getIdAtributo(),
                            entity.getIdCategoriaAtributo()
                    );

            categoriaAtributoValidator.validateCanActivate(entity, duplicatedActive);
            entity.activar();

            CategoriaAtributo saved = categoriaAtributoRepository.saveAndFlush(entity);

            auditoriaFuncionalService.registrarExito(
                    TipoEventoAuditoria.CATEGORIA_ATRIBUTO_ACTIVADO,
                    EntidadAuditada.CATEGORIA_ATRIBUTO,
                    String.valueOf(saved.getIdCategoriaAtributo()),
                    "ACTIVAR_ATRIBUTO_CATEGORIA",
                    "Operación realizada correctamente.",
                    auditMetadata(saved, actor, Map.of("motivo", request.motivo()))
            );

            registrarOutboxCategoriaAfectado(saved, "ACTIVAR_ATRIBUTO_CATEGORIA");

            return apiResponseFactory.dtoOk(
                    "Operación realizada correctamente.",
                    categoriaAtributoMapper.toResponse(saved)
            );
        }

        categoriaAtributoValidator.validateCanRemove(entity, hasProductOrSkuValues(entity));
        entity.inactivar();

        CategoriaAtributo saved = categoriaAtributoRepository.saveAndFlush(entity);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.CATEGORIA_ATRIBUTO_INACTIVADO,
                EntidadAuditada.CATEGORIA_ATRIBUTO,
                String.valueOf(saved.getIdCategoriaAtributo()),
                "INACTIVAR_ATRIBUTO_CATEGORIA",
                "Operación realizada correctamente.",
                auditMetadata(saved, actor, Map.of("motivo", request.motivo()))
        );

        registrarOutboxCategoriaAfectado(saved, "INACTIVAR_ATRIBUTO_CATEGORIA");

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                categoriaAtributoMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<CategoriaAtributoResponseDto> obtenerDetalle(Long idCategoriaAtributo) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        atributoPolicy.ensureCanViewAdmin(actor);

        CategoriaAtributo entity = findAnyRequired(idCategoriaAtributo);

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                categoriaAtributoMapper.toResponse(entity)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<CategoriaAtributoResponseDto>> listar(
            CategoriaAtributoFilterDto filter,
            PageRequestDto pageRequest
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        atributoPolicy.ensureCanViewAdmin(actor);

        PageRequestDto safePage = safePageRequest(pageRequest, "orden");

        Pageable pageable = paginationService.pageable(
                safePage.page(),
                safePage.size(),
                safePage.sortBy(),
                safePage.sortDirection(),
                ALLOWED_SORT_FIELDS,
                "orden"
        );

        PageResponseDto<CategoriaAtributoResponseDto> response = paginationService.toPageResponseDto(
                categoriaAtributoRepository.findAll(
                        CategoriaAtributoSpecifications.fromFilter(filter),
                        pageable
                ),
                categoriaAtributoMapper::toResponse
        );

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<CategoriaAtributoResponseDto>> listarPorCategoria(
            EntityReferenceDto categoria,
            PageRequestDto pageRequest
    ) {
        Categoria resolved = resolveCategoria(categoria);

        CategoriaAtributoFilterDto filter = CategoriaAtributoFilterDto.builder()
                .idCategoria(resolved.getIdCategoria())
                .estado(Boolean.TRUE)
                .build();

        return listar(filter, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<CategoriaAtributoResponseDto>> obtenerPlantillaActiva(EntityReferenceDto categoria) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        atributoPolicy.ensureCanViewAdmin(actor);

        Categoria resolved = resolveCategoria(categoria);

        List<CategoriaAtributoResponseDto> response = categoriaAtributoRepository
                .findByCategoria_IdCategoriaAndEstadoTrueOrderByOrdenAscIdCategoriaAtributoAsc(
                        resolved.getIdCategoria()
                )
                .stream()
                .map(categoriaAtributoMapper::toResponse)
                .toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", response);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeAsociacionActiva(Long idCategoria, Long idAtributo) {
        if (idCategoria == null || idAtributo == null) {
            return false;
        }

        return categoriaAtributoRepository
                .existsByCategoria_IdCategoriaAndAtributo_IdAtributoAndEstadoTrue(
                        idCategoria,
                        idAtributo
                );
    }

    private CategoriaAtributo findActiveRequired(Long idCategoriaAtributo) {
        if (idCategoriaAtributo == null) {
            throw new ValidationException(
                    "CATEGORIA_ATRIBUTO_ID_REQUERIDO",
                    "Debe indicar la asociación solicitada."
            );
        }

        return categoriaAtributoRepository
                .findByIdCategoriaAtributoAndEstadoTrue(idCategoriaAtributo)
                .orElseThrow(() -> new NotFoundException(
                        "CATEGORIA_ATRIBUTO_NO_ENCONTRADO",
                        "No se encontró el registro solicitado."
                ));
    }

    private CategoriaAtributo findAnyRequired(Long idCategoriaAtributo) {
        if (idCategoriaAtributo == null) {
            throw new ValidationException(
                    "CATEGORIA_ATRIBUTO_ID_REQUERIDO",
                    "Debe indicar la asociación solicitada."
            );
        }

        return categoriaAtributoRepository.findById(idCategoriaAtributo)
                .orElseThrow(() -> new NotFoundException(
                        "CATEGORIA_ATRIBUTO_NO_ENCONTRADO",
                        "No se encontró el registro solicitado."
                ));
    }

    private Categoria resolveCategoria(EntityReferenceDto reference) {
        if (reference == null) {
            throw new ValidationException(
                    "CATEGORIA_REFERENCIA_REQUERIDA",
                    "Debe indicar la categoría."
            );
        }

        return categoriaReferenceResolver.resolve(
                reference.id(),
                reference.codigo(),
                reference.slug(),
                reference.nombre()
        );
    }

    private Atributo resolveAtributo(EntityReferenceDto reference) {
        if (reference == null) {
            throw new ValidationException(
                    "ATRIBUTO_REFERENCIA_REQUERIDA",
                    "Debe indicar el atributo."
            );
        }

        return atributoReferenceResolver.resolve(
                reference.id(),
                reference.codigo(),
                reference.nombre()
        );
    }

    private CategoriaAtributoAssignRequestDto normalizeAssign(CategoriaAtributoAssignRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "CATEGORIA_ATRIBUTO_REQUEST_REQUERIDO",
                    "Debe enviar los datos de la asociación."
            );
        }

        return CategoriaAtributoAssignRequestDto.builder()
                .categoria(request.categoria())
                .atributo(request.atributo())
                .requerido(defaultBoolean(request.requerido(), false))
                .orden(defaultInteger(request.orden(), 0))
                .motivo(StringNormalizer.truncateOrNull(request.motivo(), 500))
                .build();
    }

    private CategoriaAtributoUpdateRequestDto normalizeUpdate(CategoriaAtributoUpdateRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "CATEGORIA_ATRIBUTO_REQUEST_REQUERIDO",
                    "Debe enviar los datos de la asociación."
            );
        }

        return CategoriaAtributoUpdateRequestDto.builder()
                .requerido(request.requerido())
                .orden(request.orden())
                .motivo(StringNormalizer.truncateOrNull(request.motivo(), 500))
                .build();
    }

    private void validateEstadoRequest(EstadoChangeRequestDto request) {
        if (request == null || request.estado() == null) {
            throw new ValidationException(
                    "CATEGORIA_ATRIBUTO_ESTADO_REQUERIDO",
                    "Debe indicar el estado solicitado."
            );
        }

        if (!StringNormalizer.hasText(request.motivo())) {
            throw new ValidationException(
                    "CATEGORIA_ATRIBUTO_MOTIVO_REQUERIDO",
                    "Debe indicar el motivo de la operación."
            );
        }
    }

    private boolean hasProductOrSkuValues(CategoriaAtributo relation) {
        Long idCategoria = relation.getCategoria().getIdCategoria();
        Long idAtributo = relation.getAtributo().getIdAtributo();

        return productoAtributoValorRepository.countByCategoriaAndAtributoActivos(idCategoria, idAtributo) > 0
                || skuAtributoValorRepository.countByCategoriaAndAtributoActivos(idCategoria, idAtributo) > 0;
    }

    private void registrarOutboxCategoriaAfectado(CategoriaAtributo relation, String accion) {
        if (relation == null || relation.getCategoria() == null) {
            return;
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("accion", accion);
        metadata.put("idCategoriaAtributo", relation.getIdCategoriaAtributo());

        if (relation.getAtributo() != null) {
            metadata.put("idAtributo", relation.getAtributo().getIdAtributo());
            metadata.put("codigoAtributo", relation.getAtributo().getCodigo());
        }

        productoSnapshotOutboxRegistrar.registrarProductosDeSubarbolCategoriaActualizados(
                relation.getCategoria().getIdCategoria(),
                "CategoriaAtributoService",
                metadata
        );
    }

    private boolean employeeCanUpdateAttributes(AuthenticatedUserContext actor) {
        return actor != null
                && actor.getIdUsuarioMs1() != null
                && actor.isEmpleado()
                && empleadoInventarioPermisoService.puedeActualizarAtributos(actor.getIdUsuarioMs1());
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

    private Map<String, Object> auditMetadata(
            CategoriaAtributo relation,
            AuthenticatedUserContext actor,
            Map<String, Object> extra
    ) {
        Map<String, Object> metadata = auditSnapshot(relation);
        metadata.put("actor", actor.actorLabel());
        metadata.put("idUsuarioMs1", actor.getIdUsuarioMs1());

        if (extra != null && !extra.isEmpty()) {
            metadata.putAll(extra);
        }

        return metadata;
    }

    private Map<String, Object> auditSnapshot(CategoriaAtributo relation) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("idCategoriaAtributo", relation.getIdCategoriaAtributo());

        if (relation.getCategoria() != null) {
            metadata.put("idCategoria", relation.getCategoria().getIdCategoria());
            metadata.put("codigoCategoria", relation.getCategoria().getCodigo());
            metadata.put("nombreCategoria", relation.getCategoria().getNombre());
        }

        if (relation.getAtributo() != null) {
            metadata.put("idAtributo", relation.getAtributo().getIdAtributo());
            metadata.put("codigoAtributo", relation.getAtributo().getCodigo());
            metadata.put("nombreAtributo", relation.getAtributo().getNombre());
            metadata.put("tipoDato", relation.getAtributo().getTipoDato() == null
                    ? null
                    : relation.getAtributo().getTipoDato().getCode());
        }

        metadata.put("requerido", relation.getRequerido());
        metadata.put("orden", relation.getOrden());
        metadata.put("estado", relation.getEstado());
        return metadata;
    }

    private Map<String, Object> merge(Map<String, Object> base, String key, Object value) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        if (base != null) {
            metadata.putAll(base);
        }
        metadata.put(key, value);
        return metadata;
    }

    private String safeMotivo(String motivo) {
        return StringNormalizer.hasText(motivo) ? motivo : "No especificado";
    }

    private Boolean defaultBoolean(Boolean value, boolean defaultValue) {
        return value == null ? defaultValue : value;
    }

    private Integer defaultInteger(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }
}
