// ruta: src/main/java/com/upsjb/ms3/controller/ProveedorController.java
package com.upsjb.ms3.controller;

import com.upsjb.ms3.domain.enums.TipoDocumentoProveedor;
import com.upsjb.ms3.dto.proveedor.filter.ProveedorFilterDto;
import com.upsjb.ms3.dto.proveedor.request.ProveedorCreateRequestDto;
import com.upsjb.ms3.dto.proveedor.request.ProveedorEstadoRequestDto;
import com.upsjb.ms3.dto.proveedor.request.ProveedorUpdateRequestDto;
import com.upsjb.ms3.dto.proveedor.response.ProveedorDetailResponseDto;
import com.upsjb.ms3.dto.proveedor.response.ProveedorResponseDto;
import com.upsjb.ms3.dto.reference.response.ProveedorOptionDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.service.contract.ProveedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ms3/inventario/proveedores")
@Tag(
        name = "MS3 - Inventario - Proveedores",
        description = "Endpoints protegidos para gestionar proveedores de inventario. No registran compras ni modifican stock."
)
public class ProveedorController {

    private final ProveedorService proveedorService;

    @PostMapping
    @Operation(
            summary = "Crear proveedor",
            description = "Registra un proveedor de inventario. El service valida tipo, documento, RUC, duplicados, permisos, auditoría y reglas funcionales."
    )
    public ResponseEntity<ApiResponseDto<ProveedorResponseDto>> crear(
            @Valid @RequestBody ProveedorCreateRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(proveedorService.crear(request));
    }

    @PutMapping("/{idProveedor}")
    @Operation(
            summary = "Actualizar proveedor",
            description = "Actualiza datos administrativos del proveedor. No registra compras, no modifica stock y no consulta MS2."
    )
    public ResponseEntity<ApiResponseDto<ProveedorResponseDto>> actualizar(
            @Parameter(description = "ID técnico del proveedor.", required = true)
            @Positive(message = "El ID del proveedor debe ser positivo.")
            @PathVariable Long idProveedor,
            @Valid @RequestBody ProveedorUpdateRequestDto request
    ) {
        return ResponseEntity.ok(proveedorService.actualizar(idProveedor, request));
    }

    @PatchMapping("/{idProveedor}/estado")
    @Operation(
            summary = "Activar o inactivar proveedor",
            description = "Cambia el estado lógico del proveedor. El service valida si existe historial funcional que restrinja la operación."
    )
    public ResponseEntity<ApiResponseDto<ProveedorResponseDto>> cambiarEstado(
            @Parameter(description = "ID técnico del proveedor.", required = true)
            @Positive(message = "El ID del proveedor debe ser positivo.")
            @PathVariable Long idProveedor,
            @Valid @RequestBody ProveedorEstadoRequestDto request
    ) {
        return ResponseEntity.ok(proveedorService.cambiarEstado(idProveedor, request));
    }

    @GetMapping("/{idProveedor}")
    @Operation(
            summary = "Obtener proveedor por ID",
            description = "Obtiene la respuesta resumida de un proveedor por su ID técnico."
    )
    public ResponseEntity<ApiResponseDto<ProveedorResponseDto>> obtenerPorId(
            @Parameter(description = "ID técnico del proveedor.", required = true)
            @Positive(message = "El ID del proveedor debe ser positivo.")
            @PathVariable Long idProveedor
    ) {
        return ResponseEntity.ok(proveedorService.obtenerPorId(idProveedor));
    }

    @GetMapping("/{idProveedor}/detalle")
    @Operation(
            summary = "Obtener detalle de proveedor",
            description = "Obtiene el detalle administrativo del proveedor. No expone información pública de catálogo."
    )
    public ResponseEntity<ApiResponseDto<ProveedorDetailResponseDto>> obtenerDetalle(
            @Parameter(description = "ID técnico del proveedor.", required = true)
            @Positive(message = "El ID del proveedor debe ser positivo.")
            @PathVariable Long idProveedor
    ) {
        return ResponseEntity.ok(proveedorService.obtenerDetalle(idProveedor));
    }

    @GetMapping
    @Operation(
            summary = "Listar proveedores",
            description = "Lista proveedores con filtros y paginación. Los criterios de búsqueda se resuelven mediante service/specification."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<ProveedorResponseDto>>> listar(
            @Valid @ParameterObject @ModelAttribute ProveedorFilterDto filter,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(proveedorService.listar(filter, pageRequest));
    }

    @GetMapping("/lookup")
    @Operation(
            summary = "Buscar proveedores para selección",
            description = "Devuelve opciones livianas de proveedores para compras y formularios internos. No es endpoint público."
    )
    public ResponseEntity<ApiResponseDto<List<ProveedorOptionDto>>> lookup(
            @Parameter(description = "Texto de búsqueda por documento, RUC, razón social, nombre comercial, nombres o apellidos.")
            @Size(max = 120, message = "La búsqueda no debe superar 120 caracteres.")
            @RequestParam(required = false) String search,
            @Parameter(description = "Cantidad máxima de resultados.")
            @Min(value = 1, message = "El límite mínimo es 1.")
            @Max(value = 50, message = "El límite máximo es 50.")
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(proveedorService.lookup(search, limit));
    }

    @GetMapping("/ruc/{ruc}")
    @Operation(
            summary = "Obtener proveedor por RUC",
            description = "Obtiene un proveedor empresa usando su RUC. La validación final corresponde al service."
    )
    public ResponseEntity<ApiResponseDto<ProveedorResponseDto>> obtenerPorRuc(
            @Parameter(description = "RUC del proveedor.", required = true)
            @NotBlank(message = "El RUC es obligatorio.")
            @Size(max = 20, message = "El RUC no debe superar 20 caracteres.")
            @PathVariable String ruc
    ) {
        return ResponseEntity.ok(proveedorService.obtenerPorRuc(ruc));
    }

    @GetMapping("/documentos/{numeroDocumento}")
    @Operation(
            summary = "Obtener proveedor por número de documento",
            description = "Obtiene un proveedor usando su número de documento sin forzar el tipo desde el endpoint."
    )
    public ResponseEntity<ApiResponseDto<ProveedorResponseDto>> obtenerPorDocumento(
            @Parameter(description = "Número de documento del proveedor.", required = true)
            @NotBlank(message = "El número de documento es obligatorio.")
            @Size(max = 30, message = "El número de documento no debe superar 30 caracteres.")
            @PathVariable String numeroDocumento
    ) {
        return ResponseEntity.ok(proveedorService.obtenerPorDocumento(numeroDocumento));
    }

    @GetMapping("/documentos/{tipoDocumento}/{numeroDocumento}")
    @Operation(
            summary = "Obtener proveedor por tipo y número de documento",
            description = "Obtiene un proveedor usando tipo de documento y número de documento."
    )
    public ResponseEntity<ApiResponseDto<ProveedorResponseDto>> obtenerPorTipoYNumeroDocumento(
            @Parameter(description = "Tipo de documento del proveedor.", required = true)
            @PathVariable TipoDocumentoProveedor tipoDocumento,
            @Parameter(description = "Número de documento del proveedor.", required = true)
            @NotBlank(message = "El número de documento es obligatorio.")
            @Size(max = 30, message = "El número de documento no debe superar 30 caracteres.")
            @PathVariable String numeroDocumento
    ) {
        return ResponseEntity.ok(proveedorService.obtenerPorDocumento(tipoDocumento, numeroDocumento));
    }
}