// ruta: src/main/java/com/upsjb/ms3/service/impl/AuditoriaFuncionalServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.domain.entity.AuditoriaFuncional;
import com.upsjb.ms3.domain.enums.EntidadAuditada;
import com.upsjb.ms3.domain.enums.TipoEventoAuditoria;
import com.upsjb.ms3.dto.auditoria.filter.AuditoriaFuncionalFilterDto;
import com.upsjb.ms3.dto.auditoria.response.AuditoriaFuncionalResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.mapper.AuditoriaFuncionalMapper;
import com.upsjb.ms3.policy.AuditoriaPolicy;
import com.upsjb.ms3.repository.AuditoriaFuncionalRepository;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.security.principal.CurrentUserResolver;
import com.upsjb.ms3.service.contract.AuditoriaFuncionalService;
import com.upsjb.ms3.shared.audit.AuditEventFactory;
import com.upsjb.ms3.shared.audit.AuditMetadataBuilder;
import com.upsjb.ms3.shared.audit.AuditResult;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.pagination.PaginationService;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.specification.AuditoriaFuncionalSpecifications;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditoriaFuncionalServiceImpl implements AuditoriaFuncionalService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "idAuditoria",
            "idUsuarioActorMs1",
            "idEmpleadoActorMs2",
            "rolActor",
            "tipoEvento",
            "entidad",
            "resultado",
            "eventAt",
            "requestId",
            "correlationId"
    );

    private final AuditoriaFuncionalRepository auditoriaFuncionalRepository;
    private final AuditoriaFuncionalMapper auditoriaFuncionalMapper;
    private final AuditoriaPolicy auditoriaPolicy;
    private final CurrentUserResolver currentUserResolver;
    private final AuditEventFactory auditEventFactory;
    private final PaginationService paginationService;
    private final ApiResponseFactory apiResponseFactory;

    @Override
    @Transactional
    public void registrarExito(
            TipoEventoAuditoria tipoEvento,
            EntidadAuditada entidad,
            String idRegistroAfectado,
            String accion,
            String descripcion,
            Map<String, Object> metadata
    ) {
        registrar(
                tipoEvento,
                entidad,
                idRegistroAfectado,
                accion,
                descripcion,
                metadata,
                AuditResult.SUCCESS
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarFallo(
            TipoEventoAuditoria tipoEvento,
            EntidadAuditada entidad,
            String idRegistroAfectado,
            String accion,
            String descripcion,
            Map<String, Object> metadata
    ) {
        registrar(
                tipoEvento,
                entidad,
                idRegistroAfectado,
                accion,
                descripcion,
                metadata,
                AuditResult.FAILURE
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarAccesoDenegado(
            EntidadAuditada entidad,
            String idRegistroAfectado,
            String accion,
            String descripcion,
            Map<String, Object> metadata
    ) {
        registrar(
                TipoEventoAuditoria.ACCESO_DENEGADO,
                entidad,
                idRegistroAfectado,
                accion,
                descripcion,
                metadata,
                AuditResult.DENIED
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarValidacionFallida(
            EntidadAuditada entidad,
            String idRegistroAfectado,
            String accion,
            String descripcion,
            Map<String, Object> metadata
    ) {
        registrar(
                TipoEventoAuditoria.VALIDACION_FALLIDA,
                entidad,
                idRegistroAfectado,
                accion,
                descripcion,
                metadata,
                AuditResult.FAILURE
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarErrorSistema(
            EntidadAuditada entidad,
            String idRegistroAfectado,
            String accion,
            String descripcion,
            Map<String, Object> metadata
    ) {
        registrar(
                TipoEventoAuditoria.ERROR_SISTEMA,
                entidad,
                idRegistroAfectado,
                accion,
                descripcion,
                metadata,
                AuditResult.FAILURE
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<PageResponseDto<AuditoriaFuncionalResponseDto>> listar(
            AuditoriaFuncionalFilterDto filter,
            PageRequestDto pageRequest
    ) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        auditoriaPolicy.ensureCanViewAudit(actor);

        PageRequestDto safePage = safePageRequest(pageRequest, "eventAt");

        Pageable pageable = paginationService.pageable(
                safePage.page(),
                safePage.size(),
                safePage.sortBy(),
                safePage.sortDirection(),
                ALLOWED_SORT_FIELDS,
                "eventAt"
        );

        PageResponseDto<AuditoriaFuncionalResponseDto> response = paginationService.toPageResponseDto(
                auditoriaFuncionalRepository.findAll(
                        AuditoriaFuncionalSpecifications.fromFilter(filter),
                        pageable
                ),
                auditoriaFuncionalMapper::toResponse
        );

        return apiResponseFactory.dtoOk("Lista obtenida correctamente.", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDto<AuditoriaFuncionalResponseDto> obtenerDetalle(Long idAuditoria) {
        AuthenticatedUserContext actor = currentUserResolver.resolveRequired();
        auditoriaPolicy.ensureCanViewAuditDetail(actor);

        if (idAuditoria == null) {
            throw new ValidationException(
                    "AUDITORIA_ID_REQUERIDO",
                    "Debe indicar la auditoría solicitada."
            );
        }

        AuditoriaFuncional auditoria = auditoriaFuncionalRepository.findById(idAuditoria)
                .orElseThrow(() -> new NotFoundException(
                        "AUDITORIA_NO_ENCONTRADA",
                        "No se encontró el registro solicitado."
                ));

        return apiResponseFactory.dtoOk(
                "Detalle obtenido correctamente.",
                auditoriaFuncionalMapper.toResponse(auditoria)
        );
    }

    private void registrar(
            TipoEventoAuditoria tipoEvento,
            EntidadAuditada entidad,
            String idRegistroAfectado,
            String accion,
            String descripcion,
            Map<String, Object> metadata,
            AuditResult result
    ) {
        validarEntrada(tipoEvento, entidad, accion);

        AuditoriaFuncional auditoria = auditEventFactory.create(
                tipoEvento,
                entidad,
                idRegistroAfectado,
                accion,
                result,
                descripcion,
                safeMetadata(metadata)
        );

        AuditoriaFuncional saved = auditoriaFuncionalRepository.save(auditoria);

        log.debug(
                "Auditoría funcional registrada. idAuditoria={}, tipoEvento={}, entidad={}, resultado={}, requestId={}, correlationId={}",
                saved.getIdAuditoria(),
                saved.getTipoEvento(),
                saved.getEntidad(),
                saved.getResultado(),
                saved.getRequestId(),
                saved.getCorrelationId()
        );
    }

    private void validarEntrada(TipoEventoAuditoria tipoEvento, EntidadAuditada entidad, String accion) {
        if (tipoEvento == null) {
            throw new ValidationException(
                    "AUDITORIA_TIPO_EVENTO_REQUERIDO",
                    "Debe indicar el tipo de evento de auditoría."
            );
        }

        if (entidad == null) {
            throw new ValidationException(
                    "AUDITORIA_ENTIDAD_REQUERIDA",
                    "Debe indicar la entidad auditada."
            );
        }

        if (!StringUtils.hasText(accion)) {
            throw new ValidationException(
                    "AUDITORIA_ACCION_REQUERIDA",
                    "Debe indicar la acción de auditoría."
            );
        }
    }

    private Map<String, Object> safeMetadata(Map<String, Object> metadata) {
        AuditMetadataBuilder builder = AuditMetadataBuilder.create();

        if (metadata != null) {
            metadata.forEach(builder::put);
        }

        return builder.build();
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