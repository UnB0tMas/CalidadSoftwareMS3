// ruta: src/main/java/com/upsjb/ms3/controller/PrecioSkuController.java
package com.upsjb.ms3.controller;

import com.upsjb.ms3.dto.precio.filter.PrecioSkuFilterDto;
import com.upsjb.ms3.dto.precio.request.PrecioSkuCreateRequestDto;
import com.upsjb.ms3.dto.precio.response.PrecioSkuHistorialResponseDto;
import com.upsjb.ms3.dto.precio.response.PrecioSkuResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.service.contract.PrecioSkuService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ms3/admin/precios-sku")
@Tag(
        name = "MS3 - Admin - Precios SKU",
        description = "Endpoints administrativos para gestionar precios versionados por SKU."
)
public class PrecioSkuController {

    private final PrecioSkuService precioSkuService;

    @PostMapping
    @Operation(
            summary = "Registrar nuevo precio vigente de SKU",
            description = "Crea una nueva versión de precio para un SKU. El service cierra el precio vigente anterior, valida reglas, registra auditoría y Outbox."
    )
    public ResponseEntity<ApiResponseDto<PrecioSkuResponseDto>> registrarNuevoPrecio(
            @Valid @RequestBody PrecioSkuCreateRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(precioSkuService.registrarNuevoPrecio(request));
    }

    @GetMapping("/vigente")
    @Operation(
            summary = "Obtener precio vigente por referencia funcional de SKU",
            description = "Obtiene el precio vigente usando referencia funcional de SKU: id, código, codigoSku o barcode. La resolución se realiza en el service."
    )
    public ResponseEntity<ApiResponseDto<PrecioSkuResponseDto>> obtenerVigente(
            @Valid @ParameterObject @ModelAttribute EntityReferenceDto skuReference
    ) {
        return ResponseEntity.ok(precioSkuService.obtenerVigente(skuReference));
    }

    @GetMapping("/skus/{idSku}/vigente")
    @Operation(
            summary = "Obtener precio vigente por ID de SKU",
            description = "Obtiene el precio vigente activo de un SKU por su ID técnico."
    )
    public ResponseEntity<ApiResponseDto<PrecioSkuResponseDto>> obtenerVigentePorSku(
            @Parameter(description = "ID técnico del SKU.", required = true)
            @Positive(message = "El ID del SKU debe ser positivo.")
            @PathVariable Long idSku
    ) {
        return ResponseEntity.ok(precioSkuService.obtenerVigentePorSku(idSku));
    }

    @GetMapping("/{idPrecioHistorial}")
    @Operation(
            summary = "Obtener detalle de precio histórico",
            description = "Obtiene el detalle de una versión histórica de precio. No edita ni reemplaza el histórico."
    )
    public ResponseEntity<ApiResponseDto<PrecioSkuHistorialResponseDto>> obtenerDetalle(
            @Parameter(description = "ID técnico del historial de precio.", required = true)
            @Positive(message = "El ID del historial de precio debe ser positivo.")
            @PathVariable Long idPrecioHistorial
    ) {
        return ResponseEntity.ok(precioSkuService.obtenerDetalle(idPrecioHistorial));
    }

    @GetMapping
    @Operation(
            summary = "Listar historial de precios SKU",
            description = "Lista precios históricos de SKU con filtros y paginación."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<PrecioSkuHistorialResponseDto>>> listarHistorial(
            @Valid @ParameterObject @ModelAttribute PrecioSkuFilterDto filter,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(precioSkuService.listarHistorial(filter, pageRequest));
    }

    @GetMapping("/historial/por-sku")
    @Operation(
            summary = "Listar historial por referencia funcional de SKU",
            description = "Lista el historial de precios usando referencia funcional de SKU: id, código, codigoSku o barcode."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<PrecioSkuHistorialResponseDto>>> listarHistorialPorSku(
            @Valid @ParameterObject @ModelAttribute EntityReferenceDto skuReference,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(precioSkuService.listarHistorialPorSku(skuReference, pageRequest));
    }
}