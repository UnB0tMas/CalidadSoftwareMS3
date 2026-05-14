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
import com.upsjb.ms3.shared.audit.AuditResult;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.pagination.PaginationService;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import com.upsjb.ms3.specification.AuditoriaFuncionalSpecifications;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        AuditoriaFuncional auditoria = auditEventFactory.create(
                tipoEvento,
                entidad,
                idRegistroAfectado,
                accion,
                AuditResult.SUCCESS,
                descripcion,
                safeMetadata(metadata)
        );
        auditoriaFuncionalRepository.save(auditoria);
    }

    @Override
    @Transactional
    public void registrarFallo(
            TipoEventoAuditoria tipoEvento,
            EntidadAuditada entidad,
            String idRegistroAfectado,
            String accion,
            String descripcion,
            Map<String, Object> metadata
    ) {
        AuditoriaFuncional auditoria = auditEventFactory.create(
                tipoEvento,
                entidad,
                idRegistroAfectado,
                accion,
                AuditResult.FAILURE,
                descripcion,
                safeMetadata(metadata)
        );
        auditoriaFuncionalRepository.save(auditoria);
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

    private Map<String, Object> safeMetadata(Map<String, Object> metadata) {
        return metadata == null ? Map.of() : metadata;
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