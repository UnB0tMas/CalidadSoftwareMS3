package com.upsjb.ms3.controller;

import com.upsjb.ms3.dto.catalogo.categoria.response.CategoriaTreeResponseDto;
import com.upsjb.ms3.dto.catalogo.producto.filter.ProductoPublicFilterDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoPublicResponseDto;
import com.upsjb.ms3.dto.reference.filter.ReferenceSearchFilterDto;
import com.upsjb.ms3.dto.reference.response.CategoriaOptionDto;
import com.upsjb.ms3.dto.reference.response.MarcaOptionDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.dto.shared.SelectOptionDto;
import com.upsjb.ms3.service.contract.CatalogoLookupService;
import com.upsjb.ms3.service.contract.CategoriaService;
import com.upsjb.ms3.service.contract.ProductoPublicService;
import com.upsjb.ms3.service.contract.ReferenceDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ms3/public/catalogo")
@Tag(
        name = "MS3 - Público - Catálogo",
        description = "Endpoints públicos de navegación del catálogo."
)
public class PublicCatalogoController {

    private final CatalogoLookupService catalogoLookupService;
    private final CategoriaService categoriaService;
    private final ReferenceDataService referenceDataService;
    private final ProductoPublicService productoPublicService;

    @GetMapping("/categorias")
    @Operation(
            summary = "Listar categorías públicas",
            description = "Devuelve categorías activas para navegación pública del catálogo."
    )
    public ResponseEntity<ApiResponseDto<List<CategoriaOptionDto>>> listarCategorias(
            @Valid
            @ParameterObject
            @ModelAttribute
            ReferenceSearchFilterDto filter
    ) {
        return ResponseEntity.ok(
                catalogoLookupService.buscarCategoriasPublicas(
                        filter
                )
        );
    }

    @GetMapping("/categorias/arbol")
    @Operation(
            summary = "Obtener árbol público de categorías",
            description = "Devuelve solo categorías activas para menús públicos."
    )
    public ResponseEntity<ApiResponseDto<List<CategoriaTreeResponseDto>>> obtenerArbolCategorias() {
        return ResponseEntity.ok(
                categoriaService.obtenerArbolPublico()
        );
    }

    @GetMapping("/marcas")
    @Operation(
            summary = "Listar marcas públicas",
            description = "Devuelve marcas activas para filtros públicos del catálogo."
    )
    public ResponseEntity<ApiResponseDto<List<MarcaOptionDto>>> listarMarcas(
            @Valid
            @ParameterObject
            @ModelAttribute
            ReferenceSearchFilterDto filter
    ) {
        return ResponseEntity.ok(
                catalogoLookupService.buscarMarcasPublicas(
                        filter
                )
        );
    }

    @GetMapping("/generos-objetivo")
    @Operation(
            summary = "Listar géneros objetivo públicos",
            description = "Devuelve opciones públicas de género objetivo."
    )
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> listarGenerosObjetivo() {
        return ResponseEntity.ok(
                referenceDataService.generosObjetivo()
        );
    }

    @GetMapping("/monedas")
    @Operation(
            summary = "Listar monedas públicas",
            description = "Devuelve monedas disponibles para presentación comercial."
    )
    public ResponseEntity<ApiResponseDto<List<SelectOptionDto>>> listarMonedas() {
        return ResponseEntity.ok(
                referenceDataService.monedas()
        );
    }

    @GetMapping("/productos-recientes")
    @Operation(
            summary = "Listar productos públicos recientes",
            description = "Devuelve productos públicos paginados."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<ProductoPublicResponseDto>>> listarProductosRecientes(
            @Min(
                    value = 0,
                    message = "La página mínima es 0."
            )
            @RequestParam(defaultValue = "0")
            Integer page,

            @Min(
                    value = 1,
                    message = "El tamaño mínimo es 1."
            )
            @Max(
                    value = 24,
                    message = "El tamaño máximo público es 24."
            )
            @RequestParam(defaultValue = "12")
            Integer size
    ) {
        ProductoPublicFilterDto filter =
                ProductoPublicFilterDto.builder()
                        .soloVendibles(Boolean.FALSE)
                        .incluirProgramados(Boolean.TRUE)
                        .build();

        PageRequestDto pageRequest =
                PageRequestDto.builder()
                        .page(page)
                        .size(size)
                        .sortBy("fechaPublicacionInicio")
                        .sortDirection("DESC")
                        .build();

        return ResponseEntity.ok(
                productoPublicService.listar(
                        filter,
                        pageRequest
                )
        );
    }
}