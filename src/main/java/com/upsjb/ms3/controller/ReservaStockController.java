// ruta: src/main/java/com/upsjb/ms3/controller/ReservaStockController.java
package com.upsjb.ms3.controller;

import com.upsjb.ms3.domain.enums.TipoReferenciaStock;
import com.upsjb.ms3.dto.inventario.reserva.filter.ReservaStockFilterDto;
import com.upsjb.ms3.dto.inventario.reserva.request.ReservaStockConfirmRequestDto;
import com.upsjb.ms3.dto.inventario.reserva.request.ReservaStockCreateRequestDto;
import com.upsjb.ms3.dto.inventario.reserva.request.ReservaStockLiberarRequestDto;
import com.upsjb.ms3.dto.inventario.reserva.response.ReservaStockResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.service.contract.ReservaStockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ms3/inventario/reservas-stock")
@Tag(
        name = "MS3 - Inventario - Reservas de stock",
        description = "Endpoints protegidos para crear, consultar, confirmar, liberar y vencer reservas de stock. No crean ventas, no facturan y no actualizan stock directamente desde el controller."
)
public class ReservaStockController {

    private final ReservaStockService reservaStockService;

    @PostMapping
    @Operation(
            summary = "Crear reserva de stock",
            description = "Crea una reserva de stock para un SKU y almacén. La resolución de referencias, validación de disponibilidad, actualización de reservado, auditoría y outbox se ejecutan en el service."
    )
    public ResponseEntity<ApiResponseDto<ReservaStockResponseDto>> crear(
            @Valid @RequestBody ReservaStockCreateRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reservaStockService.crear(request));
    }

    @PatchMapping("/{idReservaStock}/confirmar")
    @Operation(
            summary = "Confirmar reserva por ID",
            description = "Confirma una reserva existente por ID técnico. El service valida estado, vencimiento, stock reservado y registra los movimientos/eventos correspondientes."
    )
    public ResponseEntity<ApiResponseDto<ReservaStockResponseDto>> confirmar(
            @Parameter(description = "ID técnico de la reserva de stock.", required = true)
            @Positive(message = "El ID de la reserva debe ser positivo.")
            @PathVariable Long idReservaStock,
            @Valid @RequestBody ReservaStockConfirmRequestDto request
    ) {
        return ResponseEntity.ok(reservaStockService.confirmar(idReservaStock, request));
    }

    @PatchMapping("/codigo/{codigoReserva}/confirmar")
    @Operation(
            summary = "Confirmar reserva por código",
            description = "Confirma una reserva usando el código funcional generado por el backend."
    )
    public ResponseEntity<ApiResponseDto<ReservaStockResponseDto>> confirmarPorCodigo(
            @Parameter(description = "Código funcional de la reserva.", required = true)
            @NotBlank(message = "El código de reserva es obligatorio.")
            @Size(max = 80, message = "El código de reserva no debe superar 80 caracteres.")
            @PathVariable String codigoReserva,
            @Valid @RequestBody ReservaStockConfirmRequestDto request
    ) {
        return ResponseEntity.ok(reservaStockService.confirmarPorCodigo(codigoReserva, request));
    }

    @PatchMapping("/referencia/confirmar")
    @Operation(
            summary = "Confirmar reserva por referencia externa",
            description = "Confirma una reserva usando la referencia externa de venta/carrito y el par SKU/almacén. La validación funcional se ejecuta en el service."
    )
    public ResponseEntity<ApiResponseDto<ReservaStockResponseDto>> confirmarPorReferencia(
            @Parameter(description = "Tipo de referencia de stock.", required = true)
            @NotNull(message = "El tipo de referencia es obligatorio.")
            @RequestParam TipoReferenciaStock referenciaTipo,
            @Parameter(description = "ID externo de referencia asociado a MS4 o carrito.", required = true)
            @NotBlank(message = "La referencia externa es obligatoria.")
            @Size(max = 100, message = "La referencia externa no debe superar 100 caracteres.")
            @RequestParam String referenciaIdExterno,
            @Parameter(description = "ID técnico del SKU.", required = true)
            @Positive(message = "El ID del SKU debe ser positivo.")
            @RequestParam Long idSku,
            @Parameter(description = "ID técnico del almacén.", required = true)
            @Positive(message = "El ID del almacén debe ser positivo.")
            @RequestParam Long idAlmacen,
            @Valid @RequestBody ReservaStockConfirmRequestDto request
    ) {
        return ResponseEntity.ok(reservaStockService.confirmarPorReferencia(
                referenciaTipo,
                referenciaIdExterno,
                idSku,
                idAlmacen,
                request
        ));
    }

    @PatchMapping("/{idReservaStock}/liberar")
    @Operation(
            summary = "Liberar reserva por ID",
            description = "Libera una reserva existente por ID técnico. El service devuelve el stock reservado a disponibilidad y registra auditoría/eventos si corresponde."
    )
    public ResponseEntity<ApiResponseDto<ReservaStockResponseDto>> liberar(
            @Parameter(description = "ID técnico de la reserva de stock.", required = true)
            @Positive(message = "El ID de la reserva debe ser positivo.")
            @PathVariable Long idReservaStock,
            @Valid @RequestBody ReservaStockLiberarRequestDto request
    ) {
        return ResponseEntity.ok(reservaStockService.liberar(idReservaStock, request));
    }

    @PatchMapping("/codigo/{codigoReserva}/liberar")
    @Operation(
            summary = "Liberar reserva por código",
            description = "Libera una reserva usando el código funcional generado por el backend."
    )
    public ResponseEntity<ApiResponseDto<ReservaStockResponseDto>> liberarPorCodigo(
            @Parameter(description = "Código funcional de la reserva.", required = true)
            @NotBlank(message = "El código de reserva es obligatorio.")
            @Size(max = 80, message = "El código de reserva no debe superar 80 caracteres.")
            @PathVariable String codigoReserva,
            @Valid @RequestBody ReservaStockLiberarRequestDto request
    ) {
        return ResponseEntity.ok(reservaStockService.liberarPorCodigo(codigoReserva, request));
    }

    @PatchMapping("/referencia/liberar")
    @Operation(
            summary = "Liberar reserva por referencia externa",
            description = "Libera una reserva usando la referencia externa de venta/carrito y el par SKU/almacén. La validación funcional se ejecuta en el service."
    )
    public ResponseEntity<ApiResponseDto<ReservaStockResponseDto>> liberarPorReferencia(
            @Parameter(description = "Tipo de referencia de stock.", required = true)
            @NotNull(message = "El tipo de referencia es obligatorio.")
            @RequestParam TipoReferenciaStock referenciaTipo,
            @Parameter(description = "ID externo de referencia asociado a MS4 o carrito.", required = true)
            @NotBlank(message = "La referencia externa es obligatoria.")
            @Size(max = 100, message = "La referencia externa no debe superar 100 caracteres.")
            @RequestParam String referenciaIdExterno,
            @Parameter(description = "ID técnico del SKU.", required = true)
            @Positive(message = "El ID del SKU debe ser positivo.")
            @RequestParam Long idSku,
            @Parameter(description = "ID técnico del almacén.", required = true)
            @Positive(message = "El ID del almacén debe ser positivo.")
            @RequestParam Long idAlmacen,
            @Valid @RequestBody ReservaStockLiberarRequestDto request
    ) {
        return ResponseEntity.ok(reservaStockService.liberarPorReferencia(
                referenciaTipo,
                referenciaIdExterno,
                idSku,
                idAlmacen,
                request
        ));
    }

    @PatchMapping("/{idReservaStock}/vencer")
    @Operation(
            summary = "Vencer reserva por ID",
            description = "Marca una reserva como vencida cuando corresponde. El service valida estado, vencimiento y reversión del reservado."
    )
    public ResponseEntity<ApiResponseDto<ReservaStockResponseDto>> vencer(
            @Parameter(description = "ID técnico de la reserva de stock.", required = true)
            @Positive(message = "El ID de la reserva debe ser positivo.")
            @PathVariable Long idReservaStock
    ) {
        return ResponseEntity.ok(reservaStockService.vencer(idReservaStock));
    }

    @GetMapping("/{idReservaStock}")
    @Operation(
            summary = "Obtener reserva por ID",
            description = "Obtiene una reserva de stock por su ID técnico."
    )
    public ResponseEntity<ApiResponseDto<ReservaStockResponseDto>> obtenerPorId(
            @Parameter(description = "ID técnico de la reserva de stock.", required = true)
            @Positive(message = "El ID de la reserva debe ser positivo.")
            @PathVariable Long idReservaStock
    ) {
        return ResponseEntity.ok(reservaStockService.obtenerPorId(idReservaStock));
    }

    @GetMapping("/codigo/{codigoReserva}")
    @Operation(
            summary = "Obtener reserva por código",
            description = "Obtiene una reserva usando su código funcional."
    )
    public ResponseEntity<ApiResponseDto<ReservaStockResponseDto>> obtenerPorCodigo(
            @Parameter(description = "Código funcional de la reserva.", required = true)
            @NotBlank(message = "El código de reserva es obligatorio.")
            @Size(max = 80, message = "El código de reserva no debe superar 80 caracteres.")
            @PathVariable String codigoReserva
    ) {
        return ResponseEntity.ok(reservaStockService.obtenerPorCodigo(codigoReserva));
    }

    @GetMapping("/referencia")
    @Operation(
            summary = "Obtener reserva por referencia externa",
            description = "Obtiene una reserva usando tipo de referencia, referencia externa, SKU y almacén."
    )
    public ResponseEntity<ApiResponseDto<ReservaStockResponseDto>> obtenerPorReferencia(
            @Parameter(description = "Tipo de referencia de stock.", required = true)
            @NotNull(message = "El tipo de referencia es obligatorio.")
            @RequestParam TipoReferenciaStock referenciaTipo,
            @Parameter(description = "ID externo de referencia asociado a MS4 o carrito.", required = true)
            @NotBlank(message = "La referencia externa es obligatoria.")
            @Size(max = 100, message = "La referencia externa no debe superar 100 caracteres.")
            @RequestParam String referenciaIdExterno,
            @Parameter(description = "ID técnico del SKU.", required = true)
            @Positive(message = "El ID del SKU debe ser positivo.")
            @RequestParam Long idSku,
            @Parameter(description = "ID técnico del almacén.", required = true)
            @Positive(message = "El ID del almacén debe ser positivo.")
            @RequestParam Long idAlmacen
    ) {
        return ResponseEntity.ok(reservaStockService.obtenerPorReferencia(
                referenciaTipo,
                referenciaIdExterno,
                idSku,
                idAlmacen
        ));
    }

    @GetMapping
    @Operation(
            summary = "Listar reservas de stock",
            description = "Lista reservas de stock con filtros y paginación. Permite filtrar por código, SKU, producto, almacén, referencia externa, estado, expiración, actor y rangos de fechas."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<ReservaStockResponseDto>>> listar(
            @Valid @ParameterObject @ModelAttribute ReservaStockFilterDto filter,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(reservaStockService.listar(filter, pageRequest));
    }
}