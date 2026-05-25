// ruta: src/main/java/com/upsjb/ms3/controller/PublicProductoController.java
package com.upsjb.ms3.controller;

import com.upsjb.ms3.dto.catalogo.producto.filter.ProductoPublicFilterDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoPublicDetailResponseDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoPublicResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.service.contract.ProductoPublicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
@RequestMapping("/api/ms3/public/productos")
@Tag(
        name = "MS3 - Público - Productos",
        description = "Endpoints públicos para consultar productos visibles del catálogo. No exponen costos, proveedores, kardex, stock interno, movimientos ni auditoría."
)
public class PublicProductoController {

    private final ProductoPublicService productoPublicService;

    @GetMapping
    @Operation(
            summary = "Listar productos públicos",
            description = "Lista productos visibles públicamente con filtros comerciales y paginación. Permite filtrar por búsqueda, categoría, marca, género, temporada, deporte, rango de precio, promoción y vendibilidad."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<ProductoPublicResponseDto>>> listar(
            @Valid @ParameterObject @ModelAttribute ProductoPublicFilterDto filter,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(productoPublicService.listar(filter, pageRequest));
    }

    @GetMapping("/{slug}")
    @Operation(
            summary = "Obtener detalle público de producto por slug",
            description = "Obtiene el detalle público de un producto visible, publicado o programado según reglas del service. No expone datos internos de inventario, costos, proveedores, kardex, movimientos ni auditoría."
    )
    public ResponseEntity<ApiResponseDto<ProductoPublicDetailResponseDto>> obtenerDetallePorSlug(
            @Parameter(description = "Slug público del producto.", required = true)
            @NotBlank(message = "El slug del producto es obligatorio.")
            @Size(max = 240, message = "El slug del producto no debe superar 240 caracteres.")
            @PathVariable String slug
    ) {
        return ResponseEntity.ok(productoPublicService.obtenerDetallePorSlug(slug));
    }
}