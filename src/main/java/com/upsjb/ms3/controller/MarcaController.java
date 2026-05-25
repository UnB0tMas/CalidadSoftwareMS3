// ruta: src/main/java/com/upsjb/ms3/controller/MarcaController.java
package com.upsjb.ms3.controller;

import com.upsjb.ms3.dto.catalogo.marca.filter.MarcaFilterDto;
import com.upsjb.ms3.dto.catalogo.marca.request.MarcaCreateRequestDto;
import com.upsjb.ms3.dto.catalogo.marca.request.MarcaUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.marca.response.MarcaDetailResponseDto;
import com.upsjb.ms3.dto.catalogo.marca.response.MarcaResponseDto;
import com.upsjb.ms3.dto.reference.response.MarcaOptionDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.service.contract.MarcaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
@RequestMapping("/api/ms3/catalogo/marcas")
@Tag(
        name = "MS3 - Catálogo - Marcas",
        description = "Endpoints administrativos para gestionar marcas comerciales del catálogo."
)
public class MarcaController {

    private final MarcaService marcaService;

    @PostMapping
    @Operation(
            summary = "Crear marca",
            description = "Registra una nueva marca comercial. La generación de slug, validación de duplicados, permisos y auditoría se ejecuta en el service."
    )
    public ResponseEntity<ApiResponseDto<MarcaResponseDto>> crear(
            @Valid @RequestBody MarcaCreateRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(marcaService.crear(request));
    }

    @PutMapping("/{idMarca}")
    @Operation(
            summary = "Actualizar marca",
            description = "Actualiza los datos funcionales de una marca. No modifica productos ni stock."
    )
    public ResponseEntity<ApiResponseDto<MarcaResponseDto>> actualizar(
            @Parameter(description = "ID técnico de la marca.", required = true)
            @Positive(message = "El ID de la marca debe ser positivo.")
            @PathVariable Long idMarca,
            @Valid @RequestBody MarcaUpdateRequestDto request
    ) {
        return ResponseEntity.ok(marcaService.actualizar(idMarca, request));
    }

    @PatchMapping("/{idMarca}/estado")
    @Operation(
            summary = "Cambiar estado de marca",
            description = "Activa o inactiva lógicamente una marca. No elimina registros físicos ni productos relacionados."
    )
    public ResponseEntity<ApiResponseDto<MarcaResponseDto>> cambiarEstado(
            @Parameter(description = "ID técnico de la marca.", required = true)
            @Positive(message = "El ID de la marca debe ser positivo.")
            @PathVariable Long idMarca,
            @Valid @RequestBody EstadoChangeRequestDto request
    ) {
        return ResponseEntity.ok(marcaService.cambiarEstado(idMarca, request));
    }

    @GetMapping("/{idMarca}")
    @Operation(
            summary = "Obtener marca por ID",
            description = "Obtiene una marca activa por su ID técnico."
    )
    public ResponseEntity<ApiResponseDto<MarcaResponseDto>> obtenerPorId(
            @Parameter(description = "ID técnico de la marca.", required = true)
            @Positive(message = "El ID de la marca debe ser positivo.")
            @PathVariable Long idMarca
    ) {
        return ResponseEntity.ok(marcaService.obtenerPorId(idMarca));
    }

    @GetMapping("/codigo/{codigo}")
    @Operation(
            summary = "Obtener marca por código",
            description = "Obtiene una marca activa usando su código funcional."
    )
    public ResponseEntity<ApiResponseDto<MarcaResponseDto>> obtenerPorCodigo(
            @Parameter(description = "Código funcional de la marca.", required = true)
            @NotBlank(message = "El código de la marca es obligatorio.")
            @Size(max = 50, message = "El código no debe superar 50 caracteres.")
            @PathVariable String codigo
    ) {
        return ResponseEntity.ok(marcaService.obtenerPorCodigo(codigo));
    }

    @GetMapping("/slug/{slug}")
    @Operation(
            summary = "Obtener marca por slug",
            description = "Obtiene una marca activa usando su slug."
    )
    public ResponseEntity<ApiResponseDto<MarcaResponseDto>> obtenerPorSlug(
            @Parameter(description = "Slug de la marca.", required = true)
            @NotBlank(message = "El slug de la marca es obligatorio.")
            @Size(max = 150, message = "El slug no debe superar 150 caracteres.")
            @PathVariable String slug
    ) {
        return ResponseEntity.ok(marcaService.obtenerPorSlug(slug));
    }

    @GetMapping("/referencia")
    @Operation(
            summary = "Obtener marca por referencia funcional",
            description = "Obtiene una marca usando referencia funcional: id, código, nombre o slug. La resolución final se realiza en el service."
    )
    public ResponseEntity<ApiResponseDto<MarcaResponseDto>> obtenerPorReferencia(
            @Valid @ParameterObject @ModelAttribute EntityReferenceDto reference
    ) {
        return ResponseEntity.ok(marcaService.obtenerPorReferencia(reference));
    }

    @GetMapping("/{idMarca}/detalle")
    @Operation(
            summary = "Obtener detalle de marca",
            description = "Obtiene detalle administrativo de la marca, incluyendo conteos funcionales expuestos por el service."
    )
    public ResponseEntity<ApiResponseDto<MarcaDetailResponseDto>> obtenerDetalle(
            @Parameter(description = "ID técnico de la marca.", required = true)
            @Positive(message = "El ID de la marca debe ser positivo.")
            @PathVariable Long idMarca
    ) {
        return ResponseEntity.ok(marcaService.obtenerDetalle(idMarca));
    }

    @GetMapping
    @Operation(
            summary = "Listar marcas",
            description = "Lista marcas con filtros y paginación. Por defecto el service aplica estado activo cuando corresponde."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<MarcaResponseDto>>> listar(
            @Valid @ParameterObject @ModelAttribute MarcaFilterDto filter,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(marcaService.listar(filter, pageRequest));
    }

    @GetMapping("/lookup")
    @Operation(
            summary = "Buscar marcas para selección",
            description = "Devuelve opciones livianas de marcas activas para autocompletados, combos o selectores del frontend."
    )
    public ResponseEntity<ApiResponseDto<List<MarcaOptionDto>>> lookup(
            @Parameter(description = "Texto de búsqueda por código, nombre o slug.")
            @Size(max = 120, message = "La búsqueda no debe superar 120 caracteres.")
            @RequestParam(required = false) String search,
            @Parameter(description = "Cantidad máxima de resultados.")
            @Min(value = 1, message = "El límite mínimo es 1.")
            @Max(value = 50, message = "El límite máximo es 50.")
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(marcaService.lookup(search, limit));
    }
}