// ruta: src/main/java/com/upsjb/ms3/controller/MovimientoInventarioController.java
package com.upsjb.ms3.controller;

import com.upsjb.ms3.dto.inventario.movimiento.filter.MovimientoInventarioFilterDto;
import com.upsjb.ms3.dto.inventario.movimiento.request.AjusteInventarioRequestDto;
import com.upsjb.ms3.dto.inventario.movimiento.request.EntradaInventarioRequestDto;
import com.upsjb.ms3.dto.inventario.movimiento.request.MovimientoCompensatorioRequestDto;
import com.upsjb.ms3.dto.inventario.movimiento.request.SalidaInventarioRequestDto;
import com.upsjb.ms3.dto.inventario.movimiento.response.MovimientoInventarioResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.service.contract.MovimientoInventarioService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ms3/inventario/movimientos")
@Tag(
        name = "MS3 - Inventario - Movimientos",
        description = "Endpoints protegidos para registrar y consultar movimientos operativos de inventario."
)
public class MovimientoInventarioController {

    private final MovimientoInventarioService movimientoInventarioService;

    @PostMapping("/entradas")
    @Operation(
            summary = "Registrar entrada de inventario",
            description = "Registra una entrada operativa de inventario. El service valida permisos, SKU, almacén, stock, kardex, auditoría y Outbox."
    )
    public ResponseEntity<ApiResponseDto<MovimientoInventarioResponseDto>> registrarEntrada(
            @Valid @RequestBody EntradaInventarioRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(movimientoInventarioService.registrarEntrada(request));
    }

    @PostMapping("/salidas")
    @Operation(
            summary = "Registrar salida de inventario",
            description = "Registra una salida operativa de inventario. El service valida disponibilidad, permisos funcionales, kardex, auditoría y Outbox."
    )
    public ResponseEntity<ApiResponseDto<MovimientoInventarioResponseDto>> registrarSalida(
            @Valid @RequestBody SalidaInventarioRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(movimientoInventarioService.registrarSalida(request));
    }

    @PostMapping("/ajustes")
    @Operation(
            summary = "Registrar ajuste de inventario",
            description = "Registra un ajuste manual de inventario. Todo ajuste exige motivo y el service valida reglas de stock, kardex, auditoría y Outbox."
    )
    public ResponseEntity<ApiResponseDto<MovimientoInventarioResponseDto>> registrarAjuste(
            @Valid @RequestBody AjusteInventarioRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(movimientoInventarioService.registrarAjuste(request));
    }

    @PostMapping("/compensaciones")
    @Operation(
            summary = "Registrar movimiento compensatorio",
            description = "Registra un movimiento compensatorio contra un movimiento original. No elimina ni modifica el kardex histórico."
    )
    public ResponseEntity<ApiResponseDto<MovimientoInventarioResponseDto>> registrarCompensacion(
            @Valid @RequestBody MovimientoCompensatorioRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(movimientoInventarioService.registrarCompensacion(request));
    }

    @GetMapping("/{idMovimiento}")
    @Operation(
            summary = "Obtener movimiento por ID",
            description = "Obtiene el detalle de un movimiento de inventario por ID técnico. La visibilidad de costos se valida en el service."
    )
    public ResponseEntity<ApiResponseDto<MovimientoInventarioResponseDto>> obtenerDetalle(
            @Parameter(description = "ID técnico del movimiento.", required = true)
            @Positive(message = "El ID del movimiento debe ser positivo.")
            @PathVariable Long idMovimiento,
            @Parameter(description = "Indica si se solicitan costos. Solo un actor autorizado puede ver costos.")
            @RequestParam(defaultValue = "false") Boolean incluirCostos
    ) {
        return ResponseEntity.ok(movimientoInventarioService.obtenerDetalle(idMovimiento, incluirCostos));
    }

    @GetMapping("/codigo/{codigoMovimiento}")
    @Operation(
            summary = "Obtener movimiento por código",
            description = "Obtiene el detalle de un movimiento usando su código funcional."
    )
    public ResponseEntity<ApiResponseDto<MovimientoInventarioResponseDto>> obtenerPorCodigo(
            @Parameter(description = "Código funcional del movimiento.", required = true)
            @NotBlank(message = "El código del movimiento es obligatorio.")
            @Size(max = 100, message = "El código del movimiento no debe superar 100 caracteres.")
            @PathVariable String codigoMovimiento,
            @Parameter(description = "Indica si se solicitan costos. Solo un actor autorizado puede ver costos.")
            @RequestParam(defaultValue = "false") Boolean incluirCostos
    ) {
        return ResponseEntity.ok(movimientoInventarioService.obtenerPorCodigo(codigoMovimiento, incluirCostos));
    }

    @GetMapping
    @Operation(
            summary = "Listar movimientos de inventario",
            description = "Lista movimientos de inventario con filtros y paginación. La visibilidad de costos se valida en el service."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<MovimientoInventarioResponseDto>>> listar(
            @Valid @ParameterObject @ModelAttribute MovimientoInventarioFilterDto filter,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest,
            @Parameter(description = "Indica si se solicitan costos. Solo un actor autorizado puede ver costos.")
            @RequestParam(defaultValue = "false") Boolean incluirCostos
    ) {
        return ResponseEntity.ok(movimientoInventarioService.listar(filter, pageRequest, incluirCostos));
    }

    @GetMapping("/referencias")
    @Operation(
            summary = "Listar movimientos por referencia externa",
            description = "Lista movimientos asociados a una referencia externa, como venta MS4, reserva, compra, ajuste o compensación."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<MovimientoInventarioResponseDto>>> listarPorReferencia(
            @Parameter(description = "Tipo de referencia externa.", required = true)
            @NotBlank(message = "El tipo de referencia es obligatorio.")
            @Size(max = 50, message = "El tipo de referencia no debe superar 50 caracteres.")
            @RequestParam String referenciaTipo,
            @Parameter(description = "Identificador externo de referencia.", required = true)
            @NotBlank(message = "La referencia externa es obligatoria.")
            @Size(max = 100, message = "La referencia externa no debe superar 100 caracteres.")
            @RequestParam String referenciaIdExterno,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest,
            @Parameter(description = "Indica si se solicitan costos. Solo un actor autorizado puede ver costos.")
            @RequestParam(defaultValue = "false") Boolean incluirCostos
    ) {
        return ResponseEntity.ok(
                movimientoInventarioService.listarPorReferencia(
                        referenciaTipo,
                        referenciaIdExterno,
                        pageRequest,
                        incluirCostos
                )
        );
    }
}