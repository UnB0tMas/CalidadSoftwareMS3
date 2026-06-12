package com.upsjb.ms3.controller;

import com.upsjb.ms3.dto.reference.filter.ReferenceSearchFilterDto;
import com.upsjb.ms3.dto.reference.response.AlmacenOptionDto;
import com.upsjb.ms3.dto.reference.response.AtributoOptionDto;
import com.upsjb.ms3.dto.reference.response.CategoriaOptionDto;
import com.upsjb.ms3.dto.reference.response.EmpleadoInventarioOptionDto;
import com.upsjb.ms3.dto.reference.response.MarcaOptionDto;
import com.upsjb.ms3.dto.reference.response.ProductoOptionDto;
import com.upsjb.ms3.dto.reference.response.ProductoSkuOptionDto;
import com.upsjb.ms3.dto.reference.response.PromocionOptionDto;
import com.upsjb.ms3.dto.reference.response.ProveedorOptionDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.service.contract.CatalogoLookupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ms3/catalogo/lookups")
@Tag(
        name = "MS3 - Catálogo - Lookups",
        description = "Endpoints protegidos para búsquedas livianas de referencias funcionales del catálogo e inventario."
)
public class CatalogoLookupController {

    private final CatalogoLookupService catalogoLookupService;

    @GetMapping("/categorias")
    @Operation(
            summary = "Buscar categorías",
            description = "Devuelve opciones livianas de categorías por código, nombre, slug o búsqueda general."
    )
    public ResponseEntity<ApiResponseDto<List<CategoriaOptionDto>>> buscarCategorias(
            @Valid
            @ParameterObject
            @ModelAttribute
            ReferenceSearchFilterDto filter
    ) {
        return ResponseEntity.ok(
                catalogoLookupService.buscarCategorias(
                        filter
                )
        );
    }

    @GetMapping("/marcas")
    @Operation(
            summary = "Buscar marcas",
            description = "Devuelve opciones livianas de marcas por código, nombre, slug o búsqueda general."
    )
    public ResponseEntity<ApiResponseDto<List<MarcaOptionDto>>> buscarMarcas(
            @Valid
            @ParameterObject
            @ModelAttribute
            ReferenceSearchFilterDto filter
    ) {
        return ResponseEntity.ok(
                catalogoLookupService.buscarMarcas(
                        filter
                )
        );
    }

    @GetMapping("/atributos")
    @Operation(
            summary = "Buscar atributos",
            description = "Devuelve opciones livianas de atributos dinámicos para formularios de producto y SKU."
    )
    public ResponseEntity<ApiResponseDto<List<AtributoOptionDto>>> buscarAtributos(
            @Valid
            @ParameterObject
            @ModelAttribute
            ReferenceSearchFilterDto filter
    ) {
        return ResponseEntity.ok(
                catalogoLookupService.buscarAtributos(
                        filter
                )
        );
    }

    @GetMapping("/productos")
    @Operation(
            summary = "Buscar productos",
            description = "Devuelve opciones livianas de productos por código, nombre, slug o búsqueda general."
    )
    public ResponseEntity<ApiResponseDto<List<ProductoOptionDto>>> buscarProductos(
            @Valid
            @ParameterObject
            @ModelAttribute
            ReferenceSearchFilterDto filter
    ) {
        return ResponseEntity.ok(
                catalogoLookupService.buscarProductos(
                        filter
                )
        );
    }

    @GetMapping("/skus")
    @Operation(
            summary = "Buscar SKU",
            description = "Devuelve opciones livianas de SKU por código, barcode, producto o búsqueda general."
    )
    public ResponseEntity<ApiResponseDto<List<ProductoSkuOptionDto>>> buscarSkus(
            @Valid
            @ParameterObject
            @ModelAttribute
            ReferenceSearchFilterDto filter
    ) {
        return ResponseEntity.ok(
                catalogoLookupService.buscarSkus(
                        filter
                )
        );
    }

    @GetMapping("/proveedores")
    @Operation(
            summary = "Buscar proveedores",
            description = "Devuelve opciones livianas de proveedores para compras e inventario."
    )
    public ResponseEntity<ApiResponseDto<List<ProveedorOptionDto>>> buscarProveedores(
            @Valid
            @ParameterObject
            @ModelAttribute
            ReferenceSearchFilterDto filter
    ) {
        return ResponseEntity.ok(
                catalogoLookupService.buscarProveedores(
                        filter
                )
        );
    }

    @GetMapping("/almacenes")
    @Operation(
            summary = "Buscar almacenes",
            description = "Devuelve opciones livianas de almacenes para operaciones de compra, reserva, stock y movimientos."
    )
    public ResponseEntity<ApiResponseDto<List<AlmacenOptionDto>>> buscarAlmacenes(
            @Valid
            @ParameterObject
            @ModelAttribute
            ReferenceSearchFilterDto filter
    ) {
        return ResponseEntity.ok(
                catalogoLookupService.buscarAlmacenes(
                        filter
                )
        );
    }

    @GetMapping("/promociones")
    @Operation(
            summary = "Buscar promociones",
            description = "Devuelve opciones livianas de promociones para formularios administrativos."
    )
    public ResponseEntity<ApiResponseDto<List<PromocionOptionDto>>> buscarPromociones(
            @Valid
            @ParameterObject
            @ModelAttribute
            ReferenceSearchFilterDto filter
    ) {
        return ResponseEntity.ok(
                catalogoLookupService.buscarPromociones(
                        filter
                )
        );
    }

    @GetMapping("/empleados-inventario")
    @Operation(
            summary = "Buscar empleados de inventario",
            description = "Devuelve opciones livianas de empleados sincronizados desde MS2."
    )
    public ResponseEntity<ApiResponseDto<List<EmpleadoInventarioOptionDto>>> buscarEmpleadosInventario(
            @Valid
            @ParameterObject
            @ModelAttribute
            ReferenceSearchFilterDto filter
    ) {
        return ResponseEntity.ok(
                catalogoLookupService.buscarEmpleadosInventario(
                        filter
                )
        );
    }
}