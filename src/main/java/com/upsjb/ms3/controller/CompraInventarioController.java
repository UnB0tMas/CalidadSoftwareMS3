// ruta: src/main/java/com/upsjb/ms3/controller/CompraInventarioController.java
package com.upsjb.ms3.controller;

import com.upsjb.ms3.dto.inventario.compra.filter.CompraInventarioFilterDto;
import com.upsjb.ms3.dto.inventario.compra.request.CompraInventarioAnularRequestDto;
import com.upsjb.ms3.dto.inventario.compra.request.CompraInventarioConfirmRequestDto;
import com.upsjb.ms3.dto.inventario.compra.request.CompraInventarioCreateRequestDto;
import com.upsjb.ms3.dto.inventario.compra.request.CompraInventarioUpdateRequestDto;
import com.upsjb.ms3.dto.inventario.compra.response.CompraInventarioDetailResponseDto;
import com.upsjb.ms3.dto.inventario.compra.response.CompraInventarioResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.service.contract.CompraInventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
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
@RequestMapping("/api/ms3/inventario/compras")
@Tag(
        name = "MS3 - Inventario - Compras",
        description = "Endpoints administrativos para registrar, consultar, actualizar, confirmar y anular compras de inventario."
)
public class CompraInventarioController {

    private final CompraInventarioService compraInventarioService;

    @PostMapping
    @Operation(
            summary = "Registrar compra de inventario",
            description = "Registra una compra en estado funcional inicial. La resolución de proveedor, SKU, almacén, importes, auditoría y reglas de negocio se ejecuta en el service."
    )
    public ResponseEntity<ApiResponseDto<CompraInventarioDetailResponseDto>> crear(
            @Valid @RequestBody CompraInventarioCreateRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(compraInventarioService.crear(request));
    }

    @PutMapping("/{idCompra}")
    @Operation(
            summary = "Actualizar compra de inventario",
            description = "Actualiza una compra editable. El service valida estado, proveedor, detalles, importes y restricciones antes de persistir."
    )
    public ResponseEntity<ApiResponseDto<CompraInventarioDetailResponseDto>> actualizar(
            @Parameter(description = "ID técnico de la compra.", required = true)
            @Positive(message = "El ID de la compra debe ser positivo.")
            @PathVariable Long idCompra,
            @Valid @RequestBody CompraInventarioUpdateRequestDto request
    ) {
        return ResponseEntity.ok(compraInventarioService.actualizar(idCompra, request));
    }

    @PatchMapping("/{idCompra}/confirmar")
    @Operation(
            summary = "Confirmar compra de inventario",
            description = "Confirma una compra. El service registra entradas de stock, movimiento/kardex, auditoría y eventos Outbox cuando corresponda."
    )
    public ResponseEntity<ApiResponseDto<CompraInventarioDetailResponseDto>> confirmar(
            @Parameter(description = "ID técnico de la compra.", required = true)
            @Positive(message = "El ID de la compra debe ser positivo.")
            @PathVariable Long idCompra,
            @Valid @RequestBody CompraInventarioConfirmRequestDto request
    ) {
        return ResponseEntity.ok(compraInventarioService.confirmar(idCompra, request));
    }

    @PatchMapping("/{idCompra}/anular")
    @Operation(
            summary = "Anular compra de inventario",
            description = "Anula una compra según su estado funcional. No elimina físicamente registros ni revierte stock desde el controller."
    )
    public ResponseEntity<ApiResponseDto<CompraInventarioResponseDto>> anular(
            @Parameter(description = "ID técnico de la compra.", required = true)
            @Positive(message = "El ID de la compra debe ser positivo.")
            @PathVariable Long idCompra,
            @Valid @RequestBody CompraInventarioAnularRequestDto request
    ) {
        return ResponseEntity.ok(compraInventarioService.anular(idCompra, request));
    }

    @GetMapping("/{idCompra}")
    @Operation(
            summary = "Obtener compra por ID",
            description = "Obtiene la respuesta resumida de una compra de inventario."
    )
    public ResponseEntity<ApiResponseDto<CompraInventarioResponseDto>> obtenerPorId(
            @Parameter(description = "ID técnico de la compra.", required = true)
            @Positive(message = "El ID de la compra debe ser positivo.")
            @PathVariable Long idCompra
    ) {
        return ResponseEntity.ok(compraInventarioService.obtenerPorId(idCompra));
    }

    @GetMapping("/{idCompra}/detalle")
    @Operation(
            summary = "Obtener detalle de compra",
            description = "Obtiene el detalle completo de una compra, incluyendo sus líneas de SKU, almacén, cantidades e importes."
    )
    public ResponseEntity<ApiResponseDto<CompraInventarioDetailResponseDto>> obtenerDetalle(
            @Parameter(description = "ID técnico de la compra.", required = true)
            @Positive(message = "El ID de la compra debe ser positivo.")
            @PathVariable Long idCompra
    ) {
        return ResponseEntity.ok(compraInventarioService.obtenerDetalle(idCompra));
    }

    @GetMapping
    @Operation(
            summary = "Listar compras de inventario",
            description = "Lista compras con filtros y paginación. Los filtros por proveedor, moneda, estado funcional, estado lógico y fechas se resuelven en la capa de service/specification."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<CompraInventarioResponseDto>>> listar(
            @Valid @ParameterObject @ModelAttribute CompraInventarioFilterDto filter,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(compraInventarioService.listar(filter, pageRequest));
    }
}