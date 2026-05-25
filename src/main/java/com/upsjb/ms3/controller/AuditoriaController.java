// ruta: src/main/java/com/upsjb/ms3/controller/AuditoriaController.java
package com.upsjb.ms3.controller;

import com.upsjb.ms3.dto.auditoria.filter.AuditoriaFuncionalFilterDto;
import com.upsjb.ms3.dto.auditoria.response.AuditoriaFuncionalResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.service.contract.AuditoriaFuncionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ms3/auditoria")
@Tag(
        name = "MS3 - Auditoría funcional",
        description = "Endpoints administrativos para consultar auditoría funcional del MS3."
)
public class AuditoriaController {

    private final AuditoriaFuncionalService auditoriaFuncionalService;

    @GetMapping
    @Operation(
            summary = "Listar auditoría funcional",
            description = "Lista eventos de auditoría funcional con filtros y paginación. Solo usuarios ADMIN pueden consultar esta información."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<AuditoriaFuncionalResponseDto>>> listar(
            @Valid @ParameterObject @ModelAttribute AuditoriaFuncionalFilterDto filter,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(auditoriaFuncionalService.listar(filter, pageRequest));
    }

    @GetMapping("/{idAuditoria}")
    @Operation(
            summary = "Obtener detalle de auditoría funcional",
            description = "Obtiene el detalle de un evento de auditoría funcional por ID técnico."
    )
    public ResponseEntity<ApiResponseDto<AuditoriaFuncionalResponseDto>> obtenerDetalle(
            @Parameter(description = "ID técnico del evento de auditoría.", required = true)
            @Positive(message = "El ID de auditoría debe ser positivo.")
            @PathVariable Long idAuditoria
    ) {
        return ResponseEntity.ok(auditoriaFuncionalService.obtenerDetalle(idAuditoria));
    }
}