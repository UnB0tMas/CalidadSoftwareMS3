// ruta: src/main/java/com/upsjb/ms3/controller/ProductoSkuController.java
package com.upsjb.ms3.controller;

import com.upsjb.ms3.dto.catalogo.producto.filter.ProductoSkuFilterDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoSkuCreateRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoSkuUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoSkuDetailResponseDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoSkuResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.service.contract.ProductoSkuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
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
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ms3/catalogo/skus")
@Tag(
        name = "MS3 - Catálogo - SKU",
        description = "Endpoints protegidos para administrar variantes/SKU de productos. No modifican precio, stock ni kardex directamente."
)
public class ProductoSkuController {

    private final ProductoSkuService productoSkuService;

    @PostMapping
    @Operation(
            summary = "Crear SKU",
            description = "Crea una variante SKU para un producto. El service resuelve producto, genera código SKU, valida atributos, permisos, auditoría y Outbox."
    )
    public ResponseEntity<ApiResponseDto<ProductoSkuResponseDto>> crear(
            @Valid @RequestBody ProductoSkuCreateRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productoSkuService.crear(request));
    }

    @PutMapping("/{idSku}")
    @Operation(
            summary = "Actualizar SKU",
            description = "Actualiza datos funcionales del SKU y sus atributos si corresponde. No actualiza precio, stock ni movimientos."
    )
    public ResponseEntity<ApiResponseDto<ProductoSkuResponseDto>> actualizar(
            @Parameter(description = "ID técnico del SKU.", required = true)
            @Positive(message = "El ID del SKU debe ser positivo.")
            @PathVariable Long idSku,
            @Valid @RequestBody ProductoSkuUpdateRequestDto request
    ) {
        return ResponseEntity.ok(productoSkuService.actualizar(idSku, request));
    }

    @PatchMapping("/{idSku}/inactivar")
    @Operation(
            summary = "Inactivar SKU",
            description = "Inactiva lógicamente un SKU. El service valida stock, reservas pendientes y reglas funcionales antes de aplicar el cambio."
    )
    public ResponseEntity<ApiResponseDto<ProductoSkuResponseDto>> inactivar(
            @Parameter(description = "ID técnico del SKU.", required = true)
            @Positive(message = "El ID del SKU debe ser positivo.")
            @PathVariable Long idSku,
            @Valid @RequestBody EstadoChangeRequestDto request
    ) {
        return ResponseEntity.ok(productoSkuService.inactivar(idSku, request));
    }

    @PatchMapping("/{idSku}/descontinuar")
    @Operation(
            summary = "Descontinuar SKU",
            description = "Marca el SKU como descontinuado. No elimina físicamente registros ni modifica ventas pasadas."
    )
    public ResponseEntity<ApiResponseDto<ProductoSkuResponseDto>> descontinuar(
            @Parameter(description = "ID técnico del SKU.", required = true)
            @Positive(message = "El ID del SKU debe ser positivo.")
            @PathVariable Long idSku,
            @Valid @RequestBody EstadoChangeRequestDto request
    ) {
        return ResponseEntity.ok(productoSkuService.descontinuar(idSku, request));
    }

    @GetMapping("/{idSku}")
    @Operation(
            summary = "Obtener SKU por ID",
            description = "Obtiene la respuesta resumida de un SKU activo por su ID técnico."
    )
    public ResponseEntity<ApiResponseDto<ProductoSkuResponseDto>> obtenerPorId(
            @Parameter(description = "ID técnico del SKU.", required = true)
            @Positive(message = "El ID del SKU debe ser positivo.")
            @PathVariable Long idSku
    ) {
        return ResponseEntity.ok(productoSkuService.obtenerPorId(idSku));
    }

    @GetMapping("/{idSku}/detalle")
    @Operation(
            summary = "Obtener detalle administrativo de SKU",
            description = "Obtiene detalle del SKU, incluyendo datos complementarios expuestos por el service. No expone entidades JPA."
    )
    public ResponseEntity<ApiResponseDto<ProductoSkuDetailResponseDto>> obtenerDetalle(
            @Parameter(description = "ID técnico del SKU.", required = true)
            @Positive(message = "El ID del SKU debe ser positivo.")
            @PathVariable Long idSku
    ) {
        return ResponseEntity.ok(productoSkuService.obtenerDetalle(idSku));
    }

    @GetMapping
    @Operation(
            summary = "Listar SKU",
            description = "Lista SKU con filtros y paginación. Los filtros se aplican en service/specification."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<ProductoSkuResponseDto>>> listar(
            @Valid @ParameterObject @ModelAttribute ProductoSkuFilterDto filter,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(productoSkuService.listar(filter, pageRequest));
    }

    @GetMapping("/por-producto")
    @Operation(
            summary = "Listar SKU por producto",
            description = "Lista SKU de un producto usando referencia funcional del producto: id, código, nombre o slug."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<ProductoSkuResponseDto>>> listarPorProducto(
            @Valid @ParameterObject @ModelAttribute EntityReferenceDto productoReference,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(productoSkuService.listarPorProducto(productoReference, pageRequest));
    }

    @GetMapping("/por-producto/activos")
    @Operation(
            summary = "Listar SKU activos por producto",
            description = "Lista SKU activos de un producto para selección administrativa u operativa. La resolución de referencia queda en el service."
    )
    public ResponseEntity<ApiResponseDto<List<ProductoSkuResponseDto>>> listarActivosPorProducto(
            @Valid @ParameterObject @ModelAttribute EntityReferenceDto productoReference
    ) {
        return ResponseEntity.ok(productoSkuService.listarActivosPorProducto(productoReference));
    }
}