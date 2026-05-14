// ruta: src/main/java/com/upsjb/ms3/service/impl/EmpleadoSnapshotMs2ServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.EmpleadoSnapshotMs2;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.dto.empleado.filter.EmpleadoSnapshotMs2FilterDto;
import com.upsjb.ms3.dto.empleado.request.EmpleadoSnapshotMs2UpsertRequestDto;
import com.upsjb.ms3.dto.empleado.response.EmpleadoSnapshotMs2ResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.integration.ms2.Ms2EmpleadoSnapshotClient;
import com.upsjb.ms3.mapper.EmpleadoSnapshotMs2Mapper;
import com.upsjb.ms3.policy.EmpleadoInventarioPermisoPolicy;
import com.upsjb.ms3.repository.EmpleadoSnapshotMs2Repository;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.security.principal.CurrentUserResolver;
import com.upsjb.ms3.service.contract.AuditoriaFuncionalService;
import com.upsjb.ms3.service.contract.EmpleadoSnapshotMs2Service;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.pagination.PaginationService;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.specification.EmpleadoSnapshotMs2Specifications;
import com.upsjb.ms3.util.StringNormalizer;
import com.upsjb.ms3.validator.EmpleadoSnapshotMs2Validator;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
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
public class EmpleadoSnapshotMs2ServiceImpl implements EmpleadoSnapshotMs2Service {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "idEmpleadoSnapshot",
            "idEmpleadoMs2",
            "idUsuarioMs1",
            "codigoEmpleado",
            "nombresCompletos",
            "areaCodigo",
            "areaNombre",
            "empleadoActivo",
            "snapshotVersion",
            "snapshotAt",
            "estado",
            "createdAt",
            "updatedAt"
    );

    private final EmpleadoSnapshotMs2Repository empleadoRepository;
    private final EmpleadoSnapshotMs2Mapper empleadoMapper;
    private final EmpleadoSnapshotMs2Validator empleadoValidator;
    private final EmpleadoInventarioPermisoPolicy empleadoInventarioPermisoPolicy;
    private final Ms2EmpleadoSnapshotClient ms2EmpleadoSnapshotClient;
    private final CurrentUserResolver currentUserResolver;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final PaginationService paginationService;
    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional
    public ApiResponseDto<EmpleadoSnapshotMs2ResponseDto> upsert(EmpleadoSnapshotMs2UpsertRequestDto request) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        empleadoInventarioPermisoPolicy.ensureCanUpsertEmployeeSnapshot(actor);

        EmpleadoSnapshotMs2 saved = upsertLocal(request, false);

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                empleadoMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<EmpleadoSnapshotMs2ResponseDto> sincronizarDesdeMs2PorUsuarioMs1(Long idUsuarioMs1) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        empleadoInventarioPermisoPolicy.ensureCanUpsertEmployeeSnapshot(actor);

        Ms2EmpleadoSnapshotClient.EmpleadoSnapshotResponse response = ms2EmpleadoSnapshotClient
                .findByIdUsuarioMs1(idUsuarioMs1)
                .orElseThrow(() -> new NotFoundException(
                        "EMPLEADO_MS2_NO_ENCONTRADO",
                        "No se encontró el empleado solicitado en MS2."
                ));

        EmpleadoSnapshotMs2 saved = upsertFromMs2Response(response);

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                empleadoMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<EmpleadoSnapshotMs2ResponseDto> sincronizarDesdeMs2PorEmpleadoMs2(Long idEmpleadoMs2) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        empleadoInventarioPermisoPolicy.ensureCanUpsertEmployeeSnapshot(actor);

        Ms2EmpleadoSnapshotClient.EmpleadoSnapshotResponse response = ms2EmpleadoSnapshotClient
                .findByIdEmpleadoMs2(idEmpleadoMs2)
                .orElseThrow(() -> new NotFoundException(
                        "EMPLEADO_MS2_NO_ENCONTRADO",
                        "No se encontró el empleado solicitado en MS2."
                ));

        EmpleadoSnapshotMs2 saved = upsertFromMs2Response(response);

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                empleadoMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<EmpleadoSnapshotMs2ResponseDto> obtenerPorId(Long idEmpleadoSnapshot) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        empleadoInventarioPermisoPolicy.ensureCanViewPermissions(actor);

        EmpleadoSnapshotMs2 empleado = empleadoRepository.findByIdEmpleadoSnapshotAndEstadoTrue(idEmpleadoSnapshot)
                .orElseThrow(() -> new NotFoundException(
                        "EMPLEADO_SNAPSHOT_NO_ENCONTRADO",
                        "No se encontró el registro solicitado."
                ));

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                empleadoMapper.toResponse(empleado)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<EmpleadoSnapshotMs2ResponseDto> obtenerPorIdUsuarioMs1(Long idUsuarioMs1) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        empleadoInventarioPermisoPolicy.ensureCanViewPermissions(actor);

        EmpleadoSnapshotMs2 empleado = empleadoRepository.findByIdUsuarioMs1AndEstadoTrue(idUsuarioMs1)
                .orElseThrow(() -> new NotFoundException(
                        "EMPLEADO_SNAPSHOT_NO_ENCONTRADO",
                        "No se encontró el registro solicitado."
                ));

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                empleadoMapper.toResponse(empleado)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<EmpleadoSnapshotMs2ResponseDto> obtenerPorIdEmpleadoMs2(Long idEmpleadoMs2) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        empleadoInventarioPermisoPolicy.ensureCanViewPermissions(actor);

        EmpleadoSnapshotMs2 empleado = empleadoRepository.findByIdEmpleadoMs2AndEstadoTrue(idEmpleadoMs2)
                .orElseThrow(() -> new NotFoundException(
                        "EMPLEADO_SNAPSHOT_NO_ENCONTRADO",
                        "No se encontró el registro solicitado."
                ));

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                empleadoMapper.toResponse(empleado)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<EmpleadoSnapshotMs2ResponseDto> obtenerPorCodigoEmpleado(String codigoEmpleado) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        empleadoInventarioPermisoPolicy.ensureCanViewPermissions(actor);

        EmpleadoSnapshotMs2 empleado = empleadoRepository
                .findByCodigoEmpleadoIgnoreCaseAndEstadoTrue(StringNormalizer.clean(codigoEmpleado))
                .orElseThrow(() -> new NotFoundException(
                        "EMPLEADO_SNAPSHOT_NO_ENCONTRADO",
                        "No se encontró el registro solicitado."
                ));

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                empleadoMapper.toResponse(empleado)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<EmpleadoSnapshotMs2ResponseDto> validarEmpleadoActivoPorUsuarioMs1(Long idUsuarioMs1) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        empleadoInventarioPermisoPolicy.ensureCanViewPermissions(actor);

        EmpleadoSnapshotMs2 empleado = empleadoRepository.findByIdUsuarioMs1AndEstadoTrue(idUsuarioMs1)
                .orElseThrow(() -> new NotFoundException(
                        "EMPLEADO_SNAPSHOT_NO_ENCONTRADO",
                        "No se encontró el registro solicitado."
                ));

        empleadoValidator.requireActiveEmployee(empleado);

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                empleadoMapper.toResponse(empleado)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<EmpleadoSnapshotMs2ResponseDto>> listar(
            EmpleadoSnapshotMs2FilterDto filter,
            PageRequestDto pageRequest
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        empleadoInventarioPermisoPolicy.ensureCanViewPermissions(actor);

        PageRequestDto safePage = safePageRequest(pageRequest, "createdAt");

        Pageable pageable = paginationService.pageable(
                safePage.page(),
                safePage.size(),
                safePage.sortBy(),
                safePage.sortDirection(),
                ALLOWED_SORT_FIELDS,
                "createdAt"
        );

        PageResponseDto<EmpleadoSnapshotMs2ResponseDto> response = paginationService.toPageResponseDto(
                empleadoRepository.findAll(EmpleadoSnapshotMs2Specifications.fromFilter(filter), pageable),
                empleadoMapper::toResponse
        );

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", response);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeEmpleadoActivoPorUsuarioMs1(Long idUsuarioMs1) {
        if (idUsuarioMs1 == null) {
            return false;
        }

        return empleadoRepository.findByIdUsuarioMs1AndEstadoTrue(idUsuarioMs1)
                .filter(EmpleadoSnapshotMs2::isActivo)
                .filter(empleado -> Boolean.TRUE.equals(empleado.getEmpleadoActivo()))
                .isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeEmpleadoActivoPorEmpleadoMs2(Long idEmpleadoMs2) {
        if (idEmpleadoMs2 == null) {
            return false;
        }

        return empleadoRepository.findByIdEmpleadoMs2AndEstadoTrue(idEmpleadoMs2)
                .filter(EmpleadoSnapshotMs2::isActivo)
                .filter(empleado -> Boolean.TRUE.equals(empleado.getEmpleadoActivo()))
                .isPresent();
    }

    private EmpleadoSnapshotMs2 upsertFromMs2Response(Ms2EmpleadoSnapshotClient.EmpleadoSnapshotResponse response) {
        EmpleadoSnapshotMs2UpsertRequestDto request = EmpleadoSnapshotMs2UpsertRequestDto.builder()
                .idEmpleadoMs2(response.idEmpleadoMs2())
                .idUsuarioMs1(response.idUsuarioMs1())
                .codigoEmpleado(response.codigoEmpleado())
                .nombresCompletos(response.nombresCompletos())
                .areaCodigo(response.areaCodigo())
                .areaNombre(response.areaNombre())
                .empleadoActivo(response.empleadoActivo())
                .snapshotVersion(response.snapshotVersion())
                .snapshotAt(response.snapshotAt() == null ? LocalDateTime.now() : response.snapshotAt())
                .estado(Boolean.TRUE)
                .build();

        return upsertLocal(request, true);
    }

    private EmpleadoSnapshotMs2 upsertLocal(
            EmpleadoSnapshotMs2UpsertRequestDto request,
            boolean sincronizadoDesdeMs2
    ) {
        LocalDateTime snapshotAt = request.snapshotAt() == null ? LocalDateTime.now() : request.snapshotAt();

        Optional<EmpleadoSnapshotMs2> existing = findExisting(request);
        Long currentId = existing.map(EmpleadoSnapshotMs2::getIdEmpleadoSnapshot).orElse(null);

        boolean duplicatedByEmpleado = currentId == null
                ? empleadoRepository.existsByIdEmpleadoMs2AndEstadoTrue(request.idEmpleadoMs2())
                : empleadoRepository.existsByIdEmpleadoMs2AndEstadoTrueAndIdEmpleadoSnapshotNot(
                request.idEmpleadoMs2(),
                currentId
        );

        boolean duplicatedByUsuario = currentId == null
                ? empleadoRepository.existsByIdUsuarioMs1AndEstadoTrue(request.idUsuarioMs1())
                : empleadoRepository.existsByIdUsuarioMs1AndEstadoTrueAndIdEmpleadoSnapshotNot(
                request.idUsuarioMs1(),
                currentId
        );

        boolean duplicatedByCodigo = currentId == null
                ? empleadoRepository.existsByCodigoEmpleadoIgnoreCaseAndEstadoTrue(request.codigoEmpleado())
                : empleadoRepository.existsByCodigoEmpleadoIgnoreCaseAndEstadoTrueAndIdEmpleadoSnapshotNot(
                request.codigoEmpleado(),
                currentId
        );

        boolean staleVersion = existing
                .map(current -> isStaleVersion(current.getSnapshotVersion(), request.snapshotVersion()))
                .orElse(false);

        empleadoValidator.validateUpsert(
                request.idEmpleadoMs2(),
                request.idUsuarioMs1(),
                request.codigoEmpleado(),
                request.nombresCompletos(),
                request.empleadoActivo(),
                snapshotAt,
                duplicatedByEmpleado,
                duplicatedByUsuario,
                duplicatedByCodigo,
                staleVersion
        );

        boolean isCreate = existing.isEmpty();

        EmpleadoSnapshotMs2 entity = existing.orElseGet(() -> empleadoMapper.toEntity(request));
        if (existing.isPresent()) {
            empleadoMapper.updateEntity(entity, request);
        }

        entity.setCodigoEmpleado(StringNormalizer.clean(request.codigoEmpleado()));
        entity.setNombresCompletos(StringNormalizer.clean(request.nombresCompletos()));
        entity.setAreaCodigo(StringNormalizer.cleanOrNull(request.areaCodigo()));
        entity.setAreaNombre(StringNormalizer.cleanOrNull(request.areaNombre()));
        entity.setSnapshotAt(snapshotAt);

        if (request.estado() == null || Boolean.TRUE.equals(request.estado())) {
            entity.activar();
        } else {
            entity.inactivar();
        }

        EmpleadoSnapshotMs2 saved = empleadoRepository.saveAndFlush(entity);

        auditoriaFuncionalService.registrarExito(
                sincronizadoDesdeMs2
                        ? TipoEventoAuditoria.EMPLEADO_SNAPSHOT_MS2_SINCRONIZADO
                        : isCreate
                        ? TipoEventoAuditoria.EMPLEADO_SNAPSHOT_MS2_REGISTRADO
                        : TipoEventoAuditoria.EMPLEADO_SNAPSHOT_MS2_ACTUALIZADO,
                EntidadAuditada.EMPLEADO_SNAPSHOT_MS2,
                String.valueOf(saved.getIdEmpleadoSnapshot()),
                sincronizadoDesdeMs2 ? "SINCRONIZAR_EMPLEADO_SNAPSHOT_MS2" : "UPSERT_EMPLEADO_SNAPSHOT_MS2",
                sincronizadoDesdeMs2
                        ? "Snapshot de empleado MS2 sincronizado correctamente."
                        : "Snapshot de empleado MS2 registrado correctamente.",
                metadata(saved)
        );

        return saved;
    }

    private Optional<EmpleadoSnapshotMs2> findExisting(EmpleadoSnapshotMs2UpsertRequestDto request) {
        if (request == null) {
            return Optional.empty();
        }

        if (request.idEmpleadoMs2() != null) {
            Optional<EmpleadoSnapshotMs2> byEmpleado = empleadoRepository
                    .findByIdEmpleadoMs2AndEstadoTrue(request.idEmpleadoMs2());
            if (byEmpleado.isPresent()) {
                return byEmpleado;
            }
        }

        if (request.idUsuarioMs1() != null) {
            Optional<EmpleadoSnapshotMs2> byUsuario = empleadoRepository
                    .findByIdUsuarioMs1AndEstadoTrue(request.idUsuarioMs1());
            if (byUsuario.isPresent()) {
                return byUsuario;
            }
        }

        if (StringNormalizer.hasText(request.codigoEmpleado())) {
            return empleadoRepository.findByCodigoEmpleadoIgnoreCaseAndEstadoTrue(request.codigoEmpleado());
        }

        return Optional.empty();
    }

    private boolean isStaleVersion(Long currentVersion, Long newVersion) {
        return currentVersion != null && newVersion != null && newVersion < currentVersion;
    }

    private Map<String, Object> metadata(EmpleadoSnapshotMs2 empleado) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("idEmpleadoSnapshot", empleado.getIdEmpleadoSnapshot());
        metadata.put("idEmpleadoMs2", empleado.getIdEmpleadoMs2());
        metadata.put("idUsuarioMs1", empleado.getIdUsuarioMs1());
        metadata.put("codigoEmpleado", empleado.getCodigoEmpleado());
        metadata.put("nombresCompletos", empleado.getNombresCompletos());
        metadata.put("areaCodigo", empleado.getAreaCodigo());
        metadata.put("areaNombre", empleado.getAreaNombre());
        metadata.put("empleadoActivo", empleado.getEmpleadoActivo());
        metadata.put("snapshotVersion", empleado.getSnapshotVersion());
        metadata.put("snapshotAt", empleado.getSnapshotAt());
        metadata.put("estado", empleado.getEstado());
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
}