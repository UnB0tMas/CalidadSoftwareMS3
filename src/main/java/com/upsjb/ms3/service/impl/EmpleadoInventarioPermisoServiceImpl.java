// ruta: src/main/java/com/upsjb/ms3/service/impl/EmpleadoInventarioPermisoServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.EmpleadoInventarioPermisoHistorial;
import com.upsjb.ms3.domain.entity.EmpleadoSnapshotMs2;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.dto.empleado.filter.EmpleadoInventarioPermisoFilterDto;
import com.upsjb.ms3.dto.empleado.request.EmpleadoInventarioPermisoRevokeRequestDto;
import com.upsjb.ms3.dto.empleado.request.EmpleadoInventarioPermisoUpdateRequestDto;
import com.upsjb.ms3.dto.empleado.response.EmpleadoInventarioPermisoResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.mapper.EmpleadoInventarioPermisoMapper;
import com.upsjb.ms3.policy.EmpleadoInventarioPermisoPolicy;
import com.upsjb.ms3.repository.EmpleadoInventarioPermisoHistorialRepository;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.security.principal.CurrentUserResolver;
import com.upsjb.ms3.service.contract.AuditoriaFuncionalService;
import com.upsjb.ms3.service.contract.EmpleadoInventarioPermisoService;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.pagination.PaginationService;
import com.upsjb.ms3.shared.reference.EmpleadoInventarioReferenceResolver;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.specification.EmpleadoInventarioPermisoSpecifications;
import com.upsjb.ms3.util.StringNormalizer;
import com.upsjb.ms3.validator.EmpleadoInventarioPermisoValidator;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmpleadoInventarioPermisoServiceImpl implements EmpleadoInventarioPermisoService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "idPermisoHistorial",
            "empleadoSnapshot.codigoEmpleado",
            "empleadoSnapshot.nombresCompletos",
            "empleadoSnapshot.idUsuarioMs1",
            "empleadoSnapshot.idEmpleadoMs2",
            "fechaInicio",
            "fechaFin",
            "vigente",
            "estado",
            "createdAt",
            "updatedAt"
    );

    private final EmpleadoInventarioPermisoHistorialRepository permisoRepository;
    private final EmpleadoInventarioReferenceResolver empleadoReferenceResolver;
    private final EmpleadoInventarioPermisoMapper permisoMapper;
    private final EmpleadoInventarioPermisoValidator permisoValidator;
    private final EmpleadoInventarioPermisoPolicy permisoPolicy;
    private final CurrentUserResolver currentUserResolver;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final PaginationService paginationService;
    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional
    public ApiResponseDto<EmpleadoInventarioPermisoResponseDto> otorgarOActualizar(
            EmpleadoInventarioPermisoUpdateRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        permisoPolicy.ensureCanGrantPermissions(actor);

        EmpleadoSnapshotMs2 empleado = resolveEmpleado(request == null ? null : request.empleado());
        permisoPolicy.ensureNotSelfGrant(actor, empleado.getIdUsuarioMs1());

        LocalDateTime fechaInicio = request.fechaInicio() == null ? LocalDateTime.now() : request.fechaInicio();

        permisoValidator.validateGrantOrReplace(
                empleado,
                fechaInicio,
                request.fechaFin(),
                request.motivo(),
                actor.getIdUsuarioMs1(),
                actor.getIdUsuarioMs1() != null && actor.getIdUsuarioMs1().equals(empleado.getIdUsuarioMs1())
        );

        Optional<EmpleadoInventarioPermisoHistorial> vigenteActual = permisoRepository
                .findFirstByEmpleadoSnapshot_IdEmpleadoSnapshotAndVigenteTrueAndEstadoTrueOrderByFechaInicioDescIdPermisoHistorialDesc(
                        empleado.getIdEmpleadoSnapshot()
                );

        vigenteActual.ifPresent(current -> {
            permisoMapper.closeVigencia(
                    current,
                    actor.getIdUsuarioMs1(),
                    LocalDateTime.now(),
                    "Reemplazo de permisos: " + request.motivo()
            );
            permisoRepository.save(current);
        });

        EmpleadoInventarioPermisoHistorial nuevo = permisoMapper.toEntity(request, empleado, actor.getIdUsuarioMs1());
        nuevo.setFechaInicio(fechaInicio);
        nuevo.setVigente(request.fechaFin() == null || request.fechaFin().isAfter(LocalDateTime.now()));
        nuevo.activar();

        permisoValidator.validatePermissionPayload(nuevo);

        EmpleadoInventarioPermisoHistorial saved = permisoRepository.saveAndFlush(nuevo);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.PERMISO_INVENTARIO_OTORGADO,
                EntidadAuditada.EMPLEADO_INVENTARIO_PERMISO,
                String.valueOf(saved.getIdPermisoHistorial()),
                "OTORGAR_O_ACTUALIZAR_PERMISO_INVENTARIO",
                "Permiso de inventario otorgado correctamente.",
                metadataPermiso(saved)
        );

        return apiResponseFactory.dtoCreated(
                "Operación realizada correctamente.",
                permisoMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional
    public ApiResponseDto<EmpleadoInventarioPermisoResponseDto> revocar(
            Long idPermisoHistorial,
            EmpleadoInventarioPermisoRevokeRequestDto request
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        permisoPolicy.ensureCanRevokePermissions(actor);

        EmpleadoInventarioPermisoHistorial permiso = permisoRepository
                .findByIdPermisoHistorialAndEstadoTrue(idPermisoHistorial)
                .orElseThrow(() -> new NotFoundException(
                        "PERMISO_INVENTARIO_NO_ENCONTRADO",
                        "No se encontró el registro solicitado."
                ));

        permisoValidator.validateRevoke(
                permiso,
                request == null ? null : request.motivo(),
                actor.getIdUsuarioMs1()
        );

        permisoMapper.closeVigencia(permiso, actor.getIdUsuarioMs1(), request);

        EmpleadoInventarioPermisoHistorial saved = permisoRepository.saveAndFlush(permiso);

        auditoriaFuncionalService.registrarExito(
                TipoEventoAuditoria.PERMISO_INVENTARIO_REVOCADO,
                EntidadAuditada.EMPLEADO_INVENTARIO_PERMISO,
                String.valueOf(saved.getIdPermisoHistorial()),
                "REVOCAR_PERMISO_INVENTARIO",
                "Permiso de inventario revocado correctamente.",
                metadataPermiso(saved)
        );

        return apiResponseFactory.dtoOk(
                "Operación realizada correctamente.",
                permisoMapper.toResponse(saved)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<EmpleadoInventarioPermisoResponseDto> obtenerDetalle(Long idPermisoHistorial) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        permisoPolicy.ensureCanViewPermissions(actor);

        EmpleadoInventarioPermisoHistorial permiso = permisoRepository
                .findByIdPermisoHistorialAndEstadoTrue(idPermisoHistorial)
                .orElseThrow(() -> new NotFoundException(
                        "PERMISO_INVENTARIO_NO_ENCONTRADO",
                        "No se encontró el registro solicitado."
                ));

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                permisoMapper.toResponse(permiso)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<EmpleadoInventarioPermisoResponseDto> obtenerVigentePorUsuarioMs1(Long idUsuarioMs1) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        permisoPolicy.ensureCanViewPermissions(actor);

        EmpleadoInventarioPermisoHistorial permiso = findVigenteByUsuario(idUsuarioMs1)
                .orElseThrow(() -> new NotFoundException(
                        "PERMISO_INVENTARIO_NO_ENCONTRADO",
                        "No se encontró el registro solicitado."
                ));

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                permisoMapper.toResponse(permiso)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<EmpleadoInventarioPermisoResponseDto>> listar(
            EmpleadoInventarioPermisoFilterDto filter,
            PageRequestDto pageRequest
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        permisoPolicy.ensureCanViewPermissions(actor);

        PageRequestDto safePage = safePageRequest(pageRequest, "createdAt");
        Pageable pageable = paginationService.pageable(
                safePage.page(),
                safePage.size(),
                safePage.sortBy(),
                safePage.sortDirection(),
                ALLOWED_SORT_FIELDS,
                "createdAt"
        );

        PageResponseDto<EmpleadoInventarioPermisoResponseDto> response = paginationService.toPageResponseDto(
                permisoRepository.findAll(
                        EmpleadoInventarioPermisoSpecifications.fromFilter(filter),
                        pageable
                ),
                permisoMapper::toResponse
        );

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", response);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tienePermisoVigente(Long idUsuarioMs1) {
        return findVigenteByUsuario(idUsuarioMs1).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean puedeCrearProductoBasico(Long idUsuarioMs1) {
        return hasPermission(idUsuarioMs1, EmpleadoInventarioPermisoHistorial::getPuedeCrearProductoBasico);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean puedeEditarProductoBasico(Long idUsuarioMs1) {
        return hasPermission(idUsuarioMs1, EmpleadoInventarioPermisoHistorial::getPuedeEditarProductoBasico);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean puedeRegistrarEntrada(Long idUsuarioMs1) {
        return hasPermission(idUsuarioMs1, EmpleadoInventarioPermisoHistorial::getPuedeRegistrarEntrada);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean puedeRegistrarSalida(Long idUsuarioMs1) {
        return hasPermission(idUsuarioMs1, EmpleadoInventarioPermisoHistorial::getPuedeRegistrarSalida);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean puedeRegistrarAjuste(Long idUsuarioMs1) {
        return hasPermission(idUsuarioMs1, EmpleadoInventarioPermisoHistorial::getPuedeRegistrarAjuste);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean puedeConsultarKardex(Long idUsuarioMs1) {
        return hasPermission(idUsuarioMs1, EmpleadoInventarioPermisoHistorial::getPuedeConsultarKardex);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean puedeGestionarImagenes(Long idUsuarioMs1) {
        return hasPermission(idUsuarioMs1, EmpleadoInventarioPermisoHistorial::getPuedeGestionarImagenes);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean puedeActualizarAtributos(Long idUsuarioMs1) {
        return hasPermission(idUsuarioMs1, EmpleadoInventarioPermisoHistorial::getPuedeActualizarAtributos);
    }

    private EmpleadoSnapshotMs2 resolveEmpleado(EntityReferenceDto reference) {
        if (reference == null) {
            throw new ValidationException(
                    "EMPLEADO_REFERENCIA_OBLIGATORIA",
                    "Debe indicar el empleado al que se asignarán permisos."
            );
        }

        return empleadoReferenceResolver.resolve(
                firstNonNull(reference.idEmpleadoSnapshot(), reference.id()),
                reference.idEmpleadoMs2(),
                reference.idUsuarioMs1(),
                firstText(reference.codigoEmpleado(), reference.codigo())
        );
    }

    private Optional<EmpleadoInventarioPermisoHistorial> findVigenteByUsuario(Long idUsuarioMs1) {
        if (idUsuarioMs1 == null) {
            return Optional.empty();
        }

        return permisoRepository
                .findFirstByEmpleadoSnapshot_IdUsuarioMs1AndVigenteTrueAndEstadoTrueOrderByFechaInicioDescIdPermisoHistorialDesc(
                        idUsuarioMs1
                )
                .filter(this::isPermissionOperational);
    }

    private boolean hasPermission(
            Long idUsuarioMs1,
            Function<EmpleadoInventarioPermisoHistorial, Boolean> permissionGetter
    ) {
        return findVigenteByUsuario(idUsuarioMs1)
                .map(permissionGetter)
                .map(Boolean.TRUE::equals)
                .orElse(false);
    }

    private boolean isPermissionOperational(EmpleadoInventarioPermisoHistorial permiso) {
        if (permiso == null || !permiso.isActivo() || !Boolean.TRUE.equals(permiso.getVigente())) {
            return false;
        }

        EmpleadoSnapshotMs2 empleado = permiso.getEmpleadoSnapshot();

        return empleado != null
                && empleado.isActivo()
                && Boolean.TRUE.equals(empleado.getEmpleadoActivo())
                && (permiso.getFechaFin() == null || permiso.getFechaFin().isAfter(LocalDateTime.now()));
    }

    private Map<String, Object> metadataPermiso(EmpleadoInventarioPermisoHistorial permiso) {
        EmpleadoSnapshotMs2 empleado = permiso.getEmpleadoSnapshot();

        return Map.of(
                "idPermisoHistorial", permiso.getIdPermisoHistorial(),
                "idEmpleadoSnapshot", empleado == null ? null : empleado.getIdEmpleadoSnapshot(),
                "idEmpleadoMs2", empleado == null ? null : empleado.getIdEmpleadoMs2(),
                "idUsuarioMs1", empleado == null ? null : empleado.getIdUsuarioMs1(),
                "codigoEmpleado", empleado == null ? null : empleado.getCodigoEmpleado(),
                "vigente", permiso.getVigente()
        );
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

    private Long firstNonNull(Long first, Long second) {
        return first != null ? first : second;
    }

    private String firstText(String first, String second) {
        if (StringNormalizer.hasText(first)) {
            return first;
        }

        return second;
    }
}