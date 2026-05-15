// ruta: src/main/java/com/upsjb/ms3/service/impl/TipoProductoAtributoServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.Atributo;
import com.upsjb.ms3.domain.entity.TipoProducto;
import com.upsjb.ms3.domain.entity.TipoProductoAtributo;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.dto.catalogo.atributo.filter.TipoProductoAtributoFilterDto;
import com.upsjb.ms3.dto.catalogo.atributo.request.TipoProductoAtributoAssignRequestDto;
import com.upsjb.ms3.dto.catalogo.atributo.request.TipoProductoAtributoUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.atributo.response.TipoProductoAtributoResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.mapper.TipoProductoAtributoMapper;
import com.upsjb.ms3.policy.AtributoPolicy;
import com.upsjb.ms3.repository.ProductoAtributoValorRepository;
import com.upsjb.ms3.repository.SkuAtributoValorRepository;
import com.upsjb.ms3.repository.TipoProductoAtributoRepository;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.security.principal.CurrentUserResolver;
import com.upsjb.ms3.service.contract.AuditoriaFuncionalService;
import com.upsjb.ms3.service.contract.EmpleadoInventarioPermisoService;
import com.upsjb.ms3.service.contract.TipoProductoAtributoService;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.outbox.ProductoSnapshotOutboxRegistrar;
import com.upsjb.ms3.shared.pagination.PaginationService;
import com.upsjb.ms3.shared.reference.AtributoReferenceResolver;
import com.upsjb.ms3.shared.reference.TipoProductoReferenceResolver;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.specification.TipoProductoAtributoSpecifications;
import com.upsjb.ms3.util.StringNormalizer;
import com.upsjb.ms3.validator.TipoProductoAtributoValidator;
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
public class TipoProductoAtributoServiceImpl implements TipoProductoAtributoService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "idTipoProductoAtributo",
            "tipoProducto.codigo",
            "tipoProducto.nombre",
            "atributo.codigo",
            "atributo.nombre",
            "atributo.tipoDato",
            "requerido",
            "orden",
            "estado",
            "createdAt",
            "updatedAt"
    );

    private final TipoProductoAtributoRepository tipoProductoAtributoRepository;
    private final ProductoAtributoValorRepository productoAtributoValorRepository;
    private final SkuAtributoValorRepository skuAtributoValorRepository;
    private final TipoProductoReferenceResolver tipoProductoReferenceResolver;
    private final AtributoReferenceResolver atributoReferenceResolver;
    private final TipoProductoAtributoMapper tipoProductoAtributoMapper;
    private final TipoProductoAtributoValidator tipoProductoAtributoValidator;
    private final AtributoPolicy atributoPolicy;
    private final CurrentUserResolver currentUserResolver;
    private final EmpleadoInventarioPermisoService empleadoInventarioPermisoService;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final ProductoSnapshotOutboxRegistrar productoSnapshotOutboxRegistrar;
    private final PaginationService paginationService;
    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional
    public ApiResponseDto<TipoProductoAtributoResponseDto> asignar(TipoProductoAtributoAssignRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        atributoPolicy.ensureCanAssignToProductType(actor, employeeCanUpdateAttributes(actor));

        TipoProductoAtributoAssignRequestDto normalized = normalizeAssign(request);
        TipoProducto tipoProducto = resolveTipoProducto(normalized.tipoProducto());
        Atributo atributo = resolveAtributo(normalized.atributo());

        boolean duplicatedActive = tipoProductoAtributoRepository
                .existsByTipoProducto_IdTipoProductoAndAtributo_IdAtributoAndEstadoTrue(
                        tipoProducto.getIdTipoProducto(),
                        atributo.getIdAtributo()
                );

        tipoProductoAtributoValidator.validateAssign(
                tipoProducto,
                atributo,
                normalized.orden(),
                duplicatedActive
        );

        TipoProductoAtributo entity = tipoProductoAtributoRepository
                .findFirstByTipoProducto_IdTipoProductoAndAtributo_IdAtributoOrderByIdTipoProductoAtributoDesc(
                        tipoProducto.getIdTipoProducto(),
                        atributo.getIdAtributo()
                )
                .orElseGet(() -> tipoProductoAtributoMapper.toEntity(normalized, tipoProducto, atributo));

        entity.setTipoProducto(tipoProducto);
        entity.setAtributo(atributo);
        tipoProductoAtributoMapper.updateEntity(entity, normalized.requerido(), normalized.orden());
        entity.activar();

        TipoProductoAtributo saved = tipoProductoAtributoRepository.saveAndFlush(entity);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.TIPO_PRODUCTO_ATRIBUTO_ASIGNADO,
                EntidadAuditada.TIPO_PRODUCTO_ATRIBUTO,
                String.valueOf(saved.getIdTipoProductoAtributo()),
                "ASIGNAR_ATRIBUTO_TIPO_PRODUCTO",
                "Operación realizada correctamente.",
                auditMetadata(saved, actor, Map.of("motivo", safeMotivo(normalized.motivo())))
        );

        registrarOutboxTipoProductoAfectado(saved, "ASIGNAR_ATRIBUTO_TIPO_PRODUCTO");

        log.info(
                "Atributo asociado a tipo de producto. idTipoProductoAtributo={}, idTipoProducto={}, idAtributo={}, actor={}",
                saved.getIdTipoProductoAtributo(),
                tipoProducto.getIdTipoProducto(),
                atributo.getIdAtributo(),
                actor.actorLabel()
        );

        return apiResponseFactory.dtoCreated(
                "Operación realizada correctamente.",
                tipoProductoAtributoMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<TipoProductoAtributoResponseDto> actualizar(
            Long idTipoProductoAtributo,
            TipoProductoAtributoUpdateRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        atributoPolicy.ensureCanAssignToProductType(actor, employeeCanUpdateAttributes(actor));

        TipoProductoAtributo entity = findActiveRequired(idTipoProductoAtributo);
        TipoProductoAtributoUpdateRequestDto normalized = normalizeUpdate(request);

        tipoProductoAtributoValidator.validateUpdate(entity, normalized.orden());

        Map<String, Object> before = auditSnapshot(entity);

        tipoProductoAtributoMapper.updateEntity(entity, normalized.requerido(), normalized.orden());
        TipoProductoAtributo saved = tipoProductoAtributoRepository.saveAndFlush(entity);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.TIPO_PRODUCTO_ATRIBUTO_ACTUALIZADO,
                EntidadAuditada.TIPO_PRODUCTO_ATRIBUTO,
                String.valueOf(saved.getIdTipoProductoAtributo()),
                "ACTUALIZAR_ATRIBUTO_TIPO_PRODUCTO",
                "Operación realizada correctamente.",
                auditMetadata(saved, actor, merge(before, "motivo", safeMotivo(normalized.motivo())))
        );

        registrarOutboxTipoProductoAfectado(saved, "ACTUALIZAR_ATRIBUTO_TIPO_PRODUCTO");

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                tipoProductoAtributoMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<TipoProductoAtributoResponseDto> cambiarEstado(
            Long idTipoProductoAtributo,
            EstadoChangeRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        atributoPolicy.ensureCanAssignToProductType(actor, employeeCanUpdateAttributes(actor));

        validateEstadoRequest(request);

        TipoProductoAtributo entity = findAnyRequired(idTipoProductoAtributo);

        if (Objects.equals(entity.getEstado(), request.estado())) {
            return apiResponseFactory.dtoOk(
                    "Operación realizada correctamente.",
                    tipoProductoAtributoMapper.toResponse(entity)
            );
        }

        if (Boolean.TRUE.equals(request.estado())) {
            boolean duplicatedActive = tipoProductoAtributoRepository
                    .existsByTipoProducto_IdTipoProductoAndAtributo_IdAtributoAndEstadoTrueAndIdTipoProductoAtributoNot(
                            entity.getTipoProducto().getIdTipoProducto(),
                            entity.getAtributo().getIdAtributo(),
                            entity.getIdTipoProductoAtributo()
                    );

            tipoProductoAtributoValidator.validateCanActivate(entity, duplicatedActive);
            entity.activar();

            TipoProductoAtributo saved = tipoProductoAtributoRepository.saveAndFlush(entity);

            auditoriaFuncionalService.registrarExito(
                    TipoEventoAuditoria.TIPO_PRODUCTO_ATRIBUTO_ACTIVADO,
                    EntidadAuditada.TIPO_PRODUCTO_ATRIBUTO,
                    String.valueOf(saved.getIdTipoProductoAtributo()),
                    "ACTIVAR_ATRIBUTO_TIPO_PRODUCTO",
                    "Operación realizada correctamente.",
                    auditMetadata(saved, actor, Map.of("motivo", request.motivo()))
            );

            registrarOutboxTipoProductoAfectado(saved, "ACTIVAR_ATRIBUTO_TIPO_PRODUCTO");

            return apiResponseFactory.dtoOk(
                    "Operación realizada correctamente.",
                    tipoProductoAtributoMapper.toResponse(saved)
            );
        }

        tipoProductoAtributoValidator.validateCanRemove(entity, hasProductOrSkuValues(entity));
        entity.inactivar();

        TipoProductoAtributo saved = tipoProductoAtributoRepository.saveAndFlush(entity);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.TIPO_PRODUCTO_ATRIBUTO_INACTIVADO,
                EntidadAuditada.TIPO_PRODUCTO_ATRIBUTO,
                String.valueOf(saved.getIdTipoProductoAtributo()),
                "INACTIVAR_ATRIBUTO_TIPO_PRODUCTO",
                "Operación realizada correctamente.",
                auditMetadata(saved, actor, Map.of("motivo", request.motivo()))
        );

        registrarOutboxTipoProductoAfectado(saved, "INACTIVAR_ATRIBUTO_TIPO_PRODUCTO");

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                tipoProductoAtributoMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<TipoProductoAtributoResponseDto> obtenerDetalle(Long idTipoProductoAtributo) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        atributoPolicy.ensureCanViewAdmin(actor);

        TipoProductoAtributo entity = findAnyRequired(idTipoProductoAtributo);

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                tipoProductoAtributoMapper.toResponse(entity)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<TipoProductoAtributoResponseDto>> listar(
            TipoProductoAtributoFilterDto filter,
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

        PageResponseDto<TipoProductoAtributoResponseDto> response = paginationService.toPageResponseDto(
                tipoProductoAtributoRepository.findAll(
                        TipoProductoAtributoSpecifications.fromFilter(filter),
                        pageable
                ),
                tipoProductoAtributoMapper::toResponse
        );

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<TipoProductoAtributoResponseDto>> listarPorTipoProducto(
            EntityReferenceDto tipoProducto,
            PageRequestDto pageRequest
    ) {
        TipoProducto resolved = resolveTipoProducto(tipoProducto);

        TipoProductoAtributoFilterDto filter = TipoProductoAtributoFilterDto.builder()
                .idTipoProducto(resolved.getIdTipoProducto())
                .estado(Boolean.TRUE)
                .build();

        return listar(filter, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<List<TipoProductoAtributoResponseDto>> obtenerPlantillaActiva(EntityReferenceDto tipoProducto) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        atributoPolicy.ensureCanViewAdmin(actor);

        TipoProducto resolved = resolveTipoProducto(tipoProducto);

        List<TipoProductoAtributoResponseDto> response = tipoProductoAtributoRepository
                .findByTipoProducto_IdTipoProductoAndEstadoTrueOrderByOrdenAscIdTipoProductoAtributoAsc(
                        resolved.getIdTipoProducto()
                )
                .stream()
                .map(tipoProductoAtributoMapper::toResponse)
                .toList();

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", response);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeAsociacionActiva(Long idTipoProducto, Long idAtributo) {
        if (idTipoProducto == null || idAtributo == null) {
            return false;
        }

        return tipoProductoAtributoRepository
                .existsByTipoProducto_IdTipoProductoAndAtributo_IdAtributoAndEstadoTrue(
                        idTipoProducto,
                        idAtributo
                );
    }

    private TipoProductoAtributo findActiveRequired(Long idTipoProductoAtributo) {
        if (idTipoProductoAtributo == null) {
            throw new ValidationException(
                    "TIPO_PRODUCTO_ATRIBUTO_ID_REQUERIDO",
                    "Debe indicar la asociación solicitada."
            );
        }

        return tipoProductoAtributoRepository
                .findByIdTipoProductoAtributoAndEstadoTrue(idTipoProductoAtributo)
                .orElseThrow(() -> new NotFoundException(
                        "TIPO_PRODUCTO_ATRIBUTO_NO_ENCONTRADO",
                        "No se encontró el registro solicitado."
                ));
    }

    private TipoProductoAtributo findAnyRequired(Long idTipoProductoAtributo) {
        if (idTipoProductoAtributo == null) {
            throw new ValidationException(
                    "TIPO_PRODUCTO_ATRIBUTO_ID_REQUERIDO",
                    "Debe indicar la asociación solicitada."
            );
        }

        return tipoProductoAtributoRepository.findById(idTipoProductoAtributo)
                .orElseThrow(() -> new NotFoundException(
                        "TIPO_PRODUCTO_ATRIBUTO_NO_ENCONTRADO",
                        "No se encontró el registro solicitado."
                ));
    }

    private TipoProducto resolveTipoProducto(EntityReferenceDto reference) {
        if (reference == null) {
            throw new ValidationException(
                    "TIPO_PRODUCTO_REFERENCIA_REQUERIDA",
                    "Debe indicar el tipo de producto."
            );
        }

        return tipoProductoReferenceResolver.resolve(
                reference.id(),
                firstText(reference.codigo(), reference.codigoProducto()),
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

    private TipoProductoAtributoAssignRequestDto normalizeAssign(TipoProductoAtributoAssignRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "TIPO_PRODUCTO_ATRIBUTO_REQUEST_REQUERIDO",
                    "Debe enviar los datos de la asociación."
            );
        }

        return TipoProductoAtributoAssignRequestDto.builder()
                .tipoProducto(request.tipoProducto())
                .atributo(request.atributo())
                .requerido(defaultBoolean(request.requerido(), false))
                .orden(defaultInteger(request.orden(), 0))
                .motivo(StringNormalizer.truncateOrNull(request.motivo(), 500))
                .build();
    }

    private TipoProductoAtributoUpdateRequestDto normalizeUpdate(TipoProductoAtributoUpdateRequestDto request) {
        if (request == null) {
            throw new ValidationException(
                    "TIPO_PRODUCTO_ATRIBUTO_REQUEST_REQUERIDO",
                    "Debe enviar los datos de la asociación."
            );
        }

        return TipoProductoAtributoUpdateRequestDto.builder()
                .requerido(request.requerido())
                .orden(request.orden())
                .motivo(StringNormalizer.truncateOrNull(request.motivo(), 500))
                .build();
    }

    private void validateEstadoRequest(EstadoChangeRequestDto request) {
        if (request == null || request.estado() == null) {
            throw new ValidationException(
                    "TIPO_PRODUCTO_ATRIBUTO_ESTADO_REQUERIDO",
                    "Debe indicar el estado solicitado."
            );
        }

        if (!StringNormalizer.hasText(request.motivo())) {
            throw new ValidationException(
                    "TIPO_PRODUCTO_ATRIBUTO_MOTIVO_REQUERIDO",
                    "Debe indicar el motivo de la operación."
            );
        }
    }

    private boolean hasProductOrSkuValues(TipoProductoAtributo relation) {
        Long idTipoProducto = relation.getTipoProducto().getIdTipoProducto();
        Long idAtributo = relation.getAtributo().getIdAtributo();

        return productoAtributoValorRepository.countByTipoProductoAndAtributoActivos(idTipoProducto, idAtributo) > 0
                || skuAtributoValorRepository.countByTipoProductoAndAtributoActivos(idTipoProducto, idAtributo) > 0;
    }

    private void registrarOutboxTipoProductoAfectado(TipoProductoAtributo relation, String accion) {
        if (relation == null || relation.getTipoProducto() == null) {
            return;
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("accion", accion);
        metadata.put("idTipoProductoAtributo", relation.getIdTipoProductoAtributo());

        if (relation.getAtributo() != null) {
            metadata.put("idAtributo", relation.getAtributo().getIdAtributo());
            metadata.put("codigoAtributo", relation.getAtributo().getCodigo());
        }

        productoSnapshotOutboxRegistrar.registrarProductosDeTipoActualizados(
                relation.getTipoProducto().getIdTipoProducto(),
                "TipoProductoAtributoService",
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
            TipoProductoAtributo relation,
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

    private Map<String, Object> auditSnapshot(TipoProductoAtributo relation) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("idTipoProductoAtributo", relation.getIdTipoProductoAtributo());

        if (relation.getTipoProducto() != null) {
            metadata.put("idTipoProducto", relation.getTipoProducto().getIdTipoProducto());
            metadata.put("codigoTipoProducto", relation.getTipoProducto().getCodigo());
            metadata.put("nombreTipoProducto", relation.getTipoProducto().getNombre());
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

    private String firstText(String first, String second) {
        return StringNormalizer.hasText(first) ? first : second;
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