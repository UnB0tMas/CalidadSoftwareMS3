// ruta: src/main/java/com/upsjb/ms3/controller/EmpleadoInventarioPermisoController.java
package com.upsjb.ms3.controller;

import com.upsjb.ms3.dto.empleado.filter.EmpleadoInventarioPermisoFilterDto;
import com.upsjb.ms3.dto.empleado.filter.EmpleadoSnapshotMs2FilterDto;
import com.upsjb.ms3.dto.empleado.request.EmpleadoInventarioPermisoRevokeRequestDto;
import com.upsjb.ms3.dto.empleado.request.EmpleadoInventarioPermisoUpdateRequestDto;
import com.upsjb.ms3.dto.empleado.request.EmpleadoSnapshotMs2UpsertRequestDto;
import com.upsjb.ms3.dto.empleado.response.EmpleadoInventarioPermisoResponseDto;
import com.upsjb.ms3.dto.empleado.response.EmpleadoSnapshotMs2ResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.service.contract.EmpleadoInventarioPermisoService;
import com.upsjb.ms3.service.contract.EmpleadoSnapshotMs2Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ms3/admin/empleados-inventario")
@Tag(
        name = "MS3 - Admin - Empleados de inventario",
        description = "Endpoints administrativos para snapshots de empleados MS2 y permisos funcionales de inventario."
)
public class EmpleadoInventarioPermisoController {

    private final EmpleadoSnapshotMs2Service empleadoSnapshotMs2Service;
    private final EmpleadoInventarioPermisoService empleadoInventarioPermisoService;

    @PutMapping("/snapshots")
    @Operation(
            summary = "Registrar o actualizar snapshot de empleado MS2",
            description = "Crea o actualiza el snapshot mínimo de un empleado proveniente de MS2. No crea empleados en MS2 ni usuarios en MS1."
    )
    public ResponseEntity<ApiResponseDto<EmpleadoSnapshotMs2ResponseDto>> upsertSnapshot(
            @Valid @RequestBody EmpleadoSnapshotMs2UpsertRequestDto request
    ) {
        return ResponseEntity.ok(empleadoSnapshotMs2Service.upsert(request));
    }

    @PostMapping("/snapshots/sincronizar/usuario-ms1/{idUsuarioMs1}")
    @Operation(
            summary = "Sincronizar snapshot por usuario MS1",
            description = "Solicita la sincronización del snapshot del empleado asociado a un usuario MS1. La integración con MS2 se ejecuta en el service."
    )
    public ResponseEntity<ApiResponseDto<EmpleadoSnapshotMs2ResponseDto>> sincronizarDesdeMs2PorUsuarioMs1(
            @Parameter(description = "ID lógico del usuario en MS1.", required = true)
            @Positive(message = "El ID del usuario MS1 debe ser positivo.")
            @PathVariable Long idUsuarioMs1
    ) {
        return ResponseEntity.ok(empleadoSnapshotMs2Service.sincronizarDesdeMs2PorUsuarioMs1(idUsuarioMs1));
    }

    @PostMapping("/snapshots/sincronizar/empleado-ms2/{idEmpleadoMs2}")
    @Operation(
            summary = "Sincronizar snapshot por empleado MS2",
            description = "Solicita la sincronización del snapshot del empleado por su ID en MS2. No modifica la ficha laboral oficial."
    )
    public ResponseEntity<ApiResponseDto<EmpleadoSnapshotMs2ResponseDto>> sincronizarDesdeMs2PorEmpleadoMs2(
            @Parameter(description = "ID lógico del empleado en MS2.", required = true)
            @Positive(message = "El ID del empleado MS2 debe ser positivo.")
            @PathVariable Long idEmpleadoMs2
    ) {
        return ResponseEntity.ok(empleadoSnapshotMs2Service.sincronizarDesdeMs2PorEmpleadoMs2(idEmpleadoMs2));
    }

    @GetMapping("/snapshots/{idEmpleadoSnapshot}")
    @Operation(
            summary = "Obtener snapshot por ID",
            description = "Obtiene el snapshot mínimo de empleado registrado en MS3."
    )
    public ResponseEntity<ApiResponseDto<EmpleadoSnapshotMs2ResponseDto>> obtenerSnapshotPorId(
            @Parameter(description = "ID técnico del snapshot en MS3.", required = true)
            @Positive(message = "El ID del snapshot debe ser positivo.")
            @PathVariable Long idEmpleadoSnapshot
    ) {
        return ResponseEntity.ok(empleadoSnapshotMs2Service.obtenerPorId(idEmpleadoSnapshot));
    }

    @GetMapping("/snapshots/por-usuario-ms1/{idUsuarioMs1}")
    @Operation(
            summary = "Obtener snapshot por usuario MS1",
            description = "Obtiene el snapshot activo asociado al usuario MS1 indicado."
    )
    public ResponseEntity<ApiResponseDto<EmpleadoSnapshotMs2ResponseDto>> obtenerSnapshotPorUsuarioMs1(
            @Parameter(description = "ID lógico del usuario en MS1.", required = true)
            @Positive(message = "El ID del usuario MS1 debe ser positivo.")
            @PathVariable Long idUsuarioMs1
    ) {
        return ResponseEntity.ok(empleadoSnapshotMs2Service.obtenerPorIdUsuarioMs1(idUsuarioMs1));
    }

    @GetMapping("/snapshots/por-empleado-ms2/{idEmpleadoMs2}")
    @Operation(
            summary = "Obtener snapshot por empleado MS2",
            description = "Obtiene el snapshot activo asociado al empleado MS2 indicado."
    )
    public ResponseEntity<ApiResponseDto<EmpleadoSnapshotMs2ResponseDto>> obtenerSnapshotPorEmpleadoMs2(
            @Parameter(description = "ID lógico del empleado en MS2.", required = true)
            @Positive(message = "El ID del empleado MS2 debe ser positivo.")
            @PathVariable Long idEmpleadoMs2
    ) {
        return ResponseEntity.ok(empleadoSnapshotMs2Service.obtenerPorIdEmpleadoMs2(idEmpleadoMs2));
    }

    @GetMapping("/snapshots/por-codigo/{codigoEmpleado}")
    @Operation(
            summary = "Obtener snapshot por código de empleado",
            description = "Obtiene el snapshot activo usando el código funcional del empleado."
    )
    public ResponseEntity<ApiResponseDto<EmpleadoSnapshotMs2ResponseDto>> obtenerSnapshotPorCodigoEmpleado(
            @Parameter(description = "Código funcional del empleado.", required = true)
            @NotBlank(message = "El código del empleado es obligatorio.")
            @Size(max = 50, message = "El código del empleado no debe superar 50 caracteres.")
            @PathVariable String codigoEmpleado
    ) {
        return ResponseEntity.ok(empleadoSnapshotMs2Service.obtenerPorCodigoEmpleado(codigoEmpleado));
    }

    @GetMapping("/snapshots/validar-activo/usuario-ms1/{idUsuarioMs1}")
    @Operation(
            summary = "Validar empleado activo por usuario MS1",
            description = "Valida si existe un empleado activo asociado al usuario MS1 indicado."
    )
    public ResponseEntity<ApiResponseDto<EmpleadoSnapshotMs2ResponseDto>> validarEmpleadoActivoPorUsuarioMs1(
            @Parameter(description = "ID lógico del usuario en MS1.", required = true)
            @Positive(message = "El ID del usuario MS1 debe ser positivo.")
            @PathVariable Long idUsuarioMs1
    ) {
        return ResponseEntity.ok(empleadoSnapshotMs2Service.validarEmpleadoActivoPorUsuarioMs1(idUsuarioMs1));
    }

    @GetMapping("/snapshots")
    @Operation(
            summary = "Listar snapshots de empleados MS2",
            description = "Lista snapshots de empleados con filtros y paginación."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<EmpleadoSnapshotMs2ResponseDto>>> listarSnapshots(
            @Valid @ParameterObject @ModelAttribute EmpleadoSnapshotMs2FilterDto filter,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(empleadoSnapshotMs2Service.listar(filter, pageRequest));
    }

    @PostMapping("/permisos")
    @Operation(
            summary = "Otorgar o actualizar permisos de inventario",
            description = "Crea una nueva versión de permisos funcionales para un empleado. Si existe permiso vigente, el service lo cierra y crea uno nuevo."
    )
    public ResponseEntity<ApiResponseDto<EmpleadoInventarioPermisoResponseDto>> otorgarOActualizarPermiso(
            @Valid @RequestBody EmpleadoInventarioPermisoUpdateRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(empleadoInventarioPermisoService.otorgarOActualizar(request));
    }

    @PatchMapping("/permisos/{idPermisoHistorial}/revocar")
    @Operation(
            summary = "Revocar permiso de inventario",
            description = "Revoca lógicamente un permiso funcional vigente. No elimina historial físico."
    )
    public ResponseEntity<ApiResponseDto<EmpleadoInventarioPermisoResponseDto>> revocarPermiso(
            @Parameter(description = "ID técnico del historial de permiso.", required = true)
            @Positive(message = "El ID del permiso debe ser positivo.")
            @PathVariable Long idPermisoHistorial,
            @Valid @RequestBody EmpleadoInventarioPermisoRevokeRequestDto request
    ) {
        return ResponseEntity.ok(empleadoInventarioPermisoService.revocar(idPermisoHistorial, request));
    }

    @GetMapping("/permisos/{idPermisoHistorial}")
    @Operation(
            summary = "Obtener detalle de permiso de inventario",
            description = "Obtiene el detalle de un registro histórico de permisos funcionales de inventario."
    )
    public ResponseEntity<ApiResponseDto<EmpleadoInventarioPermisoResponseDto>> obtenerDetallePermiso(
            @Parameter(description = "ID técnico del historial de permiso.", required = true)
            @Positive(message = "El ID del permiso debe ser positivo.")
            @PathVariable Long idPermisoHistorial
    ) {
        return ResponseEntity.ok(empleadoInventarioPermisoService.obtenerDetalle(idPermisoHistorial));
    }

    @GetMapping("/permisos/vigente/usuario-ms1/{idUsuarioMs1}")
    @Operation(
            summary = "Obtener permiso vigente por usuario MS1",
            description = "Obtiene el permiso funcional vigente del empleado asociado al usuario MS1 indicado."
    )
    public ResponseEntity<ApiResponseDto<EmpleadoInventarioPermisoResponseDto>> obtenerPermisoVigentePorUsuarioMs1(
            @Parameter(description = "ID lógico del usuario en MS1.", required = true)
            @Positive(message = "El ID del usuario MS1 debe ser positivo.")
            @PathVariable Long idUsuarioMs1
    ) {
        return ResponseEntity.ok(empleadoInventarioPermisoService.obtenerVigentePorUsuarioMs1(idUsuarioMs1));
    }

    @GetMapping("/permisos")
    @Operation(
            summary = "Listar permisos de inventario",
            description = "Lista historial de permisos funcionales de inventario con filtros y paginación."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<EmpleadoInventarioPermisoResponseDto>>> listarPermisos(
            @Valid @ParameterObject @ModelAttribute EmpleadoInventarioPermisoFilterDto filter,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(empleadoInventarioPermisoService.listar(filter, pageRequest));
    }
}