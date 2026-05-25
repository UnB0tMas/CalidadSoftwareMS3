// ruta: src/main/java/com/upsjb/ms3/controller/CategoriaController.java
package com.upsjb.ms3.controller;

import com.upsjb.ms3.dto.catalogo.categoria.filter.CategoriaFilterDto;
import com.upsjb.ms3.dto.catalogo.categoria.request.CategoriaCreateRequestDto;
import com.upsjb.ms3.dto.catalogo.categoria.request.CategoriaUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.categoria.response.CategoriaDetailResponseDto;
import com.upsjb.ms3.dto.catalogo.categoria.response.CategoriaResponseDto;
import com.upsjb.ms3.dto.catalogo.categoria.response.CategoriaTreeResponseDto;
import com.upsjb.ms3.dto.reference.response.CategoriaOptionDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.service.contract.CategoriaService;
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
@RequestMapping("/api/ms3/catalogo/categorias")
@Tag(
        name = "MS3 - Catálogo - Categorías",
        description = "Endpoints administrativos para gestionar categorías jerárquicas del catálogo."
)
public class CategoriaController {

    private final CategoriaService categoriaService;

    @PostMapping
    @Operation(
            summary = "Crear categoría",
            description = "Registra una nueva categoría del catálogo. La generación de slug, validación de jerarquía, duplicados, permisos y auditoría se realiza en el service."
    )
    public ResponseEntity<ApiResponseDto<CategoriaResponseDto>> crear(
            @Valid @RequestBody CategoriaCreateRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoriaService.crear(request));
    }

    @PutMapping("/{idCategoria}")
    @Operation(
            summary = "Actualizar categoría",
            description = "Actualiza datos funcionales de una categoría. El cambio de padre se procesa desde el DTO y se valida en el service."
    )
    public ResponseEntity<ApiResponseDto<CategoriaResponseDto>> actualizar(
            @Parameter(description = "ID técnico de la categoría.", required = true)
            @Positive(message = "El ID de la categoría debe ser positivo.")
            @PathVariable Long idCategoria,
            @Valid @RequestBody CategoriaUpdateRequestDto request
    ) {
        return ResponseEntity.ok(categoriaService.actualizar(idCategoria, request));
    }

    @PatchMapping("/{idCategoria}/estado")
    @Operation(
            summary = "Cambiar estado de categoría",
            description = "Activa o inactiva lógicamente una categoría. No elimina registros físicos ni productos relacionados."
    )
    public ResponseEntity<ApiResponseDto<CategoriaResponseDto>> cambiarEstado(
            @Parameter(description = "ID técnico de la categoría.", required = true)
            @Positive(message = "El ID de la categoría debe ser positivo.")
            @PathVariable Long idCategoria,
            @Valid @RequestBody EstadoChangeRequestDto request
    ) {
        return ResponseEntity.ok(categoriaService.cambiarEstado(idCategoria, request));
    }

    @GetMapping("/{idCategoria}")
    @Operation(
            summary = "Obtener categoría por ID",
            description = "Obtiene una categoría por su ID técnico."
    )
    public ResponseEntity<ApiResponseDto<CategoriaResponseDto>> obtenerPorId(
            @Parameter(description = "ID técnico de la categoría.", required = true)
            @Positive(message = "El ID de la categoría debe ser positivo.")
            @PathVariable Long idCategoria
    ) {
        return ResponseEntity.ok(categoriaService.obtenerPorId(idCategoria));
    }

    @GetMapping("/{idCategoria}/detalle")
    @Operation(
            summary = "Obtener detalle de categoría",
            description = "Obtiene el detalle administrativo de una categoría, incluyendo datos de padre, subcategorías y conteos expuestos por el service."
    )
    public ResponseEntity<ApiResponseDto<CategoriaDetailResponseDto>> obtenerDetalle(
            @Parameter(description = "ID técnico de la categoría.", required = true)
            @Positive(message = "El ID de la categoría debe ser positivo.")
            @PathVariable Long idCategoria
    ) {
        return ResponseEntity.ok(categoriaService.obtenerDetalle(idCategoria));
    }

    @GetMapping
    @Operation(
            summary = "Listar categorías",
            description = "Lista categorías con filtros y paginación. Los criterios de búsqueda, estado, nivel, padre y fechas se procesan mediante Specification en la capa de service."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<CategoriaResponseDto>>> listar(
            @Valid @ParameterObject @ModelAttribute CategoriaFilterDto filter,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(categoriaService.listar(filter, pageRequest));
    }

    @GetMapping("/lookup")
    @Operation(
            summary = "Buscar categorías para selección",
            description = "Devuelve opciones livianas de categorías activas para autocompletados, combos o selectores del frontend."
    )
    public ResponseEntity<ApiResponseDto<List<CategoriaOptionDto>>> lookup(
            @Parameter(description = "Texto de búsqueda por código, nombre o slug.")
            @Size(max = 120, message = "La búsqueda no debe superar 120 caracteres.")
            @RequestParam(required = false) String search,
            @Parameter(description = "Cantidad máxima de resultados.")
            @Min(value = 1, message = "El límite mínimo es 1.")
            @Max(value = 50, message = "El límite máximo es 50.")
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(categoriaService.lookup(search, limit));
    }

    @GetMapping("/arbol")
    @Operation(
            summary = "Obtener árbol de categorías",
            description = "Obtiene la jerarquía de categorías. Por defecto el service devuelve solo activas, salvo que se indique explícitamente lo contrario."
    )
    public ResponseEntity<ApiResponseDto<List<CategoriaTreeResponseDto>>> obtenerArbol(
            @Parameter(description = "Indica si solo deben mostrarse categorías activas.")
            @RequestParam(required = false) Boolean soloActivas
    ) {
        return ResponseEntity.ok(categoriaService.obtenerArbol(soloActivas));
    }

    @GetMapping("/{idCategoriaPadre}/subcategorias")
    @Operation(
            summary = "Listar subcategorías",
            description = "Lista las subcategorías activas de una categoría padre."
    )
    public ResponseEntity<ApiResponseDto<List<CategoriaResponseDto>>> listarSubcategorias(
            @Parameter(description = "ID técnico de la categoría padre.", required = true)
            @Positive(message = "El ID de la categoría padre debe ser positivo.")
            @PathVariable Long idCategoriaPadre
    ) {
        return ResponseEntity.ok(categoriaService.listarSubcategorias(idCategoriaPadre));
    }
}