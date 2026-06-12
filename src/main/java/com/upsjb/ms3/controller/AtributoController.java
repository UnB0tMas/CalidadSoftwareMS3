// ruta: src/main/java/com/upsjb/ms3/controller/AtributoController.java
package com.upsjb.ms3.controller;

import com.upsjb.ms3.domain.enums.TipoDatoAtributo;
import com.upsjb.ms3.dto.catalogo.atributo.filter.AtributoFilterDto;
import com.upsjb.ms3.dto.catalogo.atributo.filter.CategoriaAtributoFilterDto;
import com.upsjb.ms3.dto.catalogo.atributo.request.AtributoCreateRequestDto;
import com.upsjb.ms3.dto.catalogo.atributo.request.AtributoUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.atributo.request.CategoriaAtributoAssignRequestDto;
import com.upsjb.ms3.dto.catalogo.atributo.request.CategoriaAtributoUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.atributo.response.AtributoDetailResponseDto;
import com.upsjb.ms3.dto.catalogo.atributo.response.AtributoResponseDto;
import com.upsjb.ms3.dto.catalogo.atributo.response.CategoriaAtributoResponseDto;
import com.upsjb.ms3.dto.reference.response.AtributoOptionDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.service.contract.AtributoService;
import com.upsjb.ms3.service.contract.CategoriaAtributoService;
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
@RequestMapping("/api/ms3/catalogo/atributos")
@Tag(
        name = "MS3 - Catálogo - Atributos",
        description = "Endpoints administrativos para gestionar atributos dinámicos y asociaciones con categorías."
)
public class AtributoController {

    private final AtributoService atributoService;
    private final CategoriaAtributoService categoriaAtributoService;

    @PostMapping
    @Operation(
            summary = "Crear atributo",
            description = "Registra un nuevo atributo dinámico. El service valida permisos funcionales, duplicados y tipo de dato."
    )
    public ResponseEntity<ApiResponseDto<AtributoResponseDto>> crear(
            @Valid @RequestBody AtributoCreateRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(atributoService.crear(request));
    }

    @PutMapping("/{idAtributo}")
    @Operation(
            summary = "Actualizar atributo",
            description = "Actualiza un atributo dinámico. El service valida si el tipo de dato puede cambiar cuando ya existen valores asociados."
    )
    public ResponseEntity<ApiResponseDto<AtributoResponseDto>> actualizar(
            @Parameter(description = "ID técnico del atributo.", required = true)
            @Positive(message = "El ID del atributo debe ser positivo.")
            @PathVariable Long idAtributo,
            @Valid @RequestBody AtributoUpdateRequestDto request
    ) {
        return ResponseEntity.ok(atributoService.actualizar(idAtributo, request));
    }

    @PatchMapping("/{idAtributo}/estado")
    @Operation(
            summary = "Cambiar estado de atributo",
            description = "Activa o inactiva lógicamente un atributo. No elimina físicamente valores ni asociaciones."
    )
    public ResponseEntity<ApiResponseDto<AtributoResponseDto>> cambiarEstado(
            @Parameter(description = "ID técnico del atributo.", required = true)
            @Positive(message = "El ID del atributo debe ser positivo.")
            @PathVariable Long idAtributo,
            @Valid @RequestBody EstadoChangeRequestDto request
    ) {
        return ResponseEntity.ok(atributoService.cambiarEstado(idAtributo, request));
    }

    @GetMapping("/{idAtributo}")
    @Operation(
            summary = "Obtener atributo por ID",
            description = "Obtiene la respuesta resumida de un atributo activo."
    )
    public ResponseEntity<ApiResponseDto<AtributoResponseDto>> obtenerPorId(
            @Parameter(description = "ID técnico del atributo.", required = true)
            @Positive(message = "El ID del atributo debe ser positivo.")
            @PathVariable Long idAtributo
    ) {
        return ResponseEntity.ok(atributoService.obtenerPorId(idAtributo));
    }

    @GetMapping("/{idAtributo}/detalle")
    @Operation(
            summary = "Obtener detalle de atributo",
            description = "Obtiene detalle administrativo del atributo, incluyendo conteos de uso y asociaciones con categorías."
    )
    public ResponseEntity<ApiResponseDto<AtributoDetailResponseDto>> obtenerDetalle(
            @Parameter(description = "ID técnico del atributo.", required = true)
            @Positive(message = "El ID del atributo debe ser positivo.")
            @PathVariable Long idAtributo
    ) {
        return ResponseEntity.ok(atributoService.obtenerDetalle(idAtributo));
    }

    @GetMapping
    @Operation(
            summary = "Listar atributos",
            description = "Lista atributos dinámicos con filtros y paginación."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<AtributoResponseDto>>> listar(
            @Valid @ParameterObject @ModelAttribute AtributoFilterDto filter,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(atributoService.listar(filter, pageRequest));
    }

    @GetMapping("/lookup")
    @Operation(
            summary = "Buscar atributos para selección",
            description = "Devuelve opciones livianas de atributos activos para autocompletados o selects."
    )
    public ResponseEntity<ApiResponseDto<List<AtributoOptionDto>>> lookup(
            @Parameter(description = "Texto de búsqueda por código o nombre.")
            @Size(max = 120, message = "La búsqueda no debe superar 120 caracteres.")
            @RequestParam(required = false) String search,
            @Parameter(description = "Cantidad máxima de resultados.")
            @Min(value = 1, message = "El límite mínimo es 1.")
            @Max(value = 50, message = "El límite máximo es 50.")
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(atributoService.lookup(search, limit));
    }

    @GetMapping("/filtrables")
    @Operation(
            summary = "Listar atributos filtrables",
            description = "Lista atributos activos marcados como filtrables."
    )
    public ResponseEntity<ApiResponseDto<List<AtributoResponseDto>>> listarFiltrables() {
        return ResponseEntity.ok(atributoService.listarFiltrables());
    }

    @GetMapping("/visibles-publico")
    @Operation(
            summary = "Listar atributos visibles al público",
            description = "Lista atributos activos marcados como visibles públicamente. No expone valores internos de productos ni SKU."
    )
    public ResponseEntity<ApiResponseDto<List<AtributoResponseDto>>> listarVisiblesPublico() {
        return ResponseEntity.ok(atributoService.listarVisiblesPublico());
    }

    @GetMapping("/por-tipo-dato/{tipoDato}")
    @Operation(
            summary = "Listar atributos por tipo de dato",
            description = "Lista atributos activos por tipo de dato funcional."
    )
    public ResponseEntity<ApiResponseDto<List<AtributoResponseDto>>> listarPorTipoDato(
            @Parameter(description = "Tipo de dato del atributo.", required = true)
            @PathVariable TipoDatoAtributo tipoDato
    ) {
        return ResponseEntity.ok(atributoService.listarPorTipoDato(tipoDato));
    }

    @PostMapping("/categoria-asociaciones")
    @Operation(
            summary = "Asociar atributo a categoría",
            description = "Asocia un atributo dinámico a una categoría. La resolución de referencias y validaciones se realiza en el service."
    )
    public ResponseEntity<ApiResponseDto<CategoriaAtributoResponseDto>> asignarACategoria(
            @Valid @RequestBody CategoriaAtributoAssignRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoriaAtributoService.asignar(request));
    }

    @PutMapping("/categoria-asociaciones/{idCategoriaAtributo}")
    @Operation(
            summary = "Actualizar asociación atributo-categoría",
            description = "Actualiza datos funcionales de la asociación, como requerido u orden."
    )
    public ResponseEntity<ApiResponseDto<CategoriaAtributoResponseDto>> actualizarAsociacionCategoria(
            @Parameter(description = "ID técnico de la asociación.", required = true)
            @Positive(message = "El ID de la asociación debe ser positivo.")
            @PathVariable Long idCategoriaAtributo,
            @Valid @RequestBody CategoriaAtributoUpdateRequestDto request
    ) {
        return ResponseEntity.ok(categoriaAtributoService.actualizar(idCategoriaAtributo, request));
    }

    @PatchMapping("/categoria-asociaciones/{idCategoriaAtributo}/estado")
    @Operation(
            summary = "Cambiar estado de asociación atributo-categoría",
            description = "Activa o inactiva lógicamente la asociación entre atributo y categoría."
    )
    public ResponseEntity<ApiResponseDto<CategoriaAtributoResponseDto>> cambiarEstadoAsociacionCategoria(
            @Parameter(description = "ID técnico de la asociación.", required = true)
            @Positive(message = "El ID de la asociación debe ser positivo.")
            @PathVariable Long idCategoriaAtributo,
            @Valid @RequestBody EstadoChangeRequestDto request
    ) {
        return ResponseEntity.ok(categoriaAtributoService.cambiarEstado(idCategoriaAtributo, request));
    }

    @GetMapping("/categoria-asociaciones/{idCategoriaAtributo}")
    @Operation(
            summary = "Obtener detalle de asociación atributo-categoría",
            description = "Obtiene el detalle de la asociación entre un atributo y una categoría."
    )
    public ResponseEntity<ApiResponseDto<CategoriaAtributoResponseDto>> obtenerDetalleAsociacionCategoria(
            @Parameter(description = "ID técnico de la asociación.", required = true)
            @Positive(message = "El ID de la asociación debe ser positivo.")
            @PathVariable Long idCategoriaAtributo
    ) {
        return ResponseEntity.ok(categoriaAtributoService.obtenerDetalle(idCategoriaAtributo));
    }

    @GetMapping("/categoria-asociaciones")
    @Operation(
            summary = "Listar asociaciones atributo-categoría",
            description = "Lista asociaciones entre categorías y atributos con filtros y paginación."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<CategoriaAtributoResponseDto>>> listarAsociacionesCategoria(
            @Valid @ParameterObject @ModelAttribute CategoriaAtributoFilterDto filter,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(categoriaAtributoService.listar(filter, pageRequest));
    }

    @GetMapping("/categoria-asociaciones/por-categoria")
    @Operation(
            summary = "Listar asociaciones por categoría",
            description = "Lista atributos asociados a una categoría usando referencia funcional: id, código, nombre o slug."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<CategoriaAtributoResponseDto>>> listarPorCategoria(
            @Valid @ParameterObject @ModelAttribute EntityReferenceDto categoria,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(categoriaAtributoService.listarPorCategoria(categoria, pageRequest));
    }

    @GetMapping("/categoria-asociaciones/plantilla-activa")
    @Operation(
            summary = "Obtener plantilla activa por categoría",
            description = "Obtiene la plantilla activa de atributos para una categoría usando referencia funcional."
    )
    public ResponseEntity<ApiResponseDto<List<CategoriaAtributoResponseDto>>> obtenerPlantillaActiva(
            @Valid @ParameterObject @ModelAttribute EntityReferenceDto categoria
    ) {
        return ResponseEntity.ok(categoriaAtributoService.obtenerPlantillaActiva(categoria));
    }
}