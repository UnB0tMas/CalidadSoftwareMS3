// ruta: src/main/java/com/upsjb/ms3/controller/AlmacenController.java
package com.upsjb.ms3.controller;

import com.upsjb.ms3.dto.inventario.almacen.filter.AlmacenFilterDto;
import com.upsjb.ms3.dto.inventario.almacen.request.AlmacenCreateRequestDto;
import com.upsjb.ms3.dto.inventario.almacen.request.AlmacenEstadoRequestDto;
import com.upsjb.ms3.dto.inventario.almacen.request.AlmacenUpdateRequestDto;
import com.upsjb.ms3.dto.inventario.almacen.response.AlmacenDetailResponseDto;
import com.upsjb.ms3.dto.inventario.almacen.response.AlmacenResponseDto;
import com.upsjb.ms3.dto.reference.response.AlmacenOptionDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.service.contract.AlmacenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@RequestMapping("/api/ms3/inventario/almacenes")
@Tag(
        name = "MS3 - Inventario - Almacenes",
        description = "Endpoints administrativos para gestionar almacenes de inventario."
)
public class AlmacenController {

    private final AlmacenService almacenService;

    @PostMapping
    @Operation(
            summary = "Crear almacén",
            description = "Registra un nuevo almacén. La validación de permisos, unicidad, almacén principal y reglas funcionales se ejecuta en el service."
    )
    public ResponseEntity<ApiResponseDto<AlmacenResponseDto>> crear(
            @Valid @RequestBody AlmacenCreateRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(almacenService.crear(request));
    }

    @PutMapping("/{idAlmacen}")
    @Operation(
            summary = "Actualizar almacén",
            description = "Actualiza los datos administrativos de un almacén sin modificar stock ni movimientos."
    )
    public ResponseEntity<ApiResponseDto<AlmacenResponseDto>> actualizar(
            @Parameter(description = "ID técnico del almacén.", required = true)
            @Positive(message = "El ID del almacén debe ser positivo.")
            @PathVariable Long idAlmacen,
            @Valid @RequestBody AlmacenUpdateRequestDto request
    ) {
        return ResponseEntity.ok(almacenService.actualizar(idAlmacen, request));
    }

    @PatchMapping("/{idAlmacen}/estado")
    @Operation(
            summary = "Cambiar estado de almacén",
            description = "Activa o inactiva lógicamente un almacén. El service valida si existen relaciones funcionales que impidan la operación."
    )
    public ResponseEntity<ApiResponseDto<AlmacenResponseDto>> cambiarEstado(
            @Parameter(description = "ID técnico del almacén.", required = true)
            @Positive(message = "El ID del almacén debe ser positivo.")
            @PathVariable Long idAlmacen,
            @Valid @RequestBody AlmacenEstadoRequestDto request
    ) {
        return ResponseEntity.ok(almacenService.cambiarEstado(idAlmacen, request));
    }

    @GetMapping("/{idAlmacen}")
    @Operation(
            summary = "Obtener almacén por ID",
            description = "Obtiene la respuesta resumida de un almacén activo."
    )
    public ResponseEntity<ApiResponseDto<AlmacenResponseDto>> obtenerPorId(
            @Parameter(description = "ID técnico del almacén.", required = true)
            @Positive(message = "El ID del almacén debe ser positivo.")
            @PathVariable Long idAlmacen
    ) {
        return ResponseEntity.ok(almacenService.obtenerPorId(idAlmacen));
    }

    @GetMapping("/{idAlmacen}/detalle")
    @Operation(
            summary = "Obtener detalle de almacén",
            description = "Obtiene el detalle administrativo del almacén, incluyendo resumen de stock cuando el service lo expone."
    )
    public ResponseEntity<ApiResponseDto<AlmacenDetailResponseDto>> obtenerDetalle(
            @Parameter(description = "ID técnico del almacén.", required = true)
            @Positive(message = "El ID del almacén debe ser positivo.")
            @PathVariable Long idAlmacen
    ) {
        return ResponseEntity.ok(almacenService.obtenerDetalle(idAlmacen));
    }

    @GetMapping
    @Operation(
            summary = "Listar almacenes",
            description = "Lista almacenes con filtros y paginación. Por defecto el service aplica estado activo cuando corresponde."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<AlmacenResponseDto>>> listar(
            @Valid @ParameterObject @ModelAttribute AlmacenFilterDto filter,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(almacenService.listar(filter, pageRequest));
    }

    @GetMapping("/lookup")
    @Operation(
            summary = "Buscar almacenes para selección",
            description = "Devuelve opciones livianas de almacenes activos para autocompletados o selects del frontend."
    )
    public ResponseEntity<ApiResponseDto<List<AlmacenOptionDto>>> lookup(
            @Parameter(description = "Texto de búsqueda por código o nombre.")
            @Size(max = 120, message = "La búsqueda no debe superar 120 caracteres.")
            @RequestParam(required = false) String search,
            @Parameter(description = "Cantidad máxima de resultados.")
            @Min(value = 1, message = "El límite mínimo es 1.")
            @Max(value = 50, message = "El límite máximo es 50.")
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(almacenService.lookup(search, limit));
    }

    @GetMapping("/principal")
    @Operation(
            summary = "Obtener almacén principal",
            description = "Obtiene el almacén marcado como principal y activo."
    )
    public ResponseEntity<ApiResponseDto<AlmacenResponseDto>> obtenerPrincipal() {
        return ResponseEntity.ok(almacenService.obtenerPrincipal());
    }

    @GetMapping("/para-venta")
    @Operation(
            summary = "Listar almacenes habilitados para venta",
            description = "Lista almacenes activos que permiten venta. No modifica stock."
    )
    public ResponseEntity<ApiResponseDto<List<AlmacenResponseDto>>> listarParaVenta() {
        return ResponseEntity.ok(almacenService.listarParaVenta());
    }

    @GetMapping("/para-compra")
    @Operation(
            summary = "Listar almacenes habilitados para compra",
            description = "Lista almacenes activos que permiten compra. No registra compras ni movimientos."
    )
    public ResponseEntity<ApiResponseDto<List<AlmacenResponseDto>>> listarParaCompra() {
        return ResponseEntity.ok(almacenService.listarParaCompra());
    }
}