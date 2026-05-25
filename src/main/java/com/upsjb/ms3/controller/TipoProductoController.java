// ruta: src/main/java/com/upsjb/ms3/controller/TipoProductoController.java
package com.upsjb.ms3.controller;

import com.upsjb.ms3.dto.catalogo.tipoproducto.filter.TipoProductoFilterDto;
import com.upsjb.ms3.dto.catalogo.tipoproducto.request.TipoProductoCreateRequestDto;
import com.upsjb.ms3.dto.catalogo.tipoproducto.request.TipoProductoUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.tipoproducto.response.TipoProductoDetailResponseDto;
import com.upsjb.ms3.dto.catalogo.tipoproducto.response.TipoProductoResponseDto;
import com.upsjb.ms3.dto.reference.response.TipoProductoOptionDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.service.contract.TipoProductoService;
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
@RequestMapping("/api/ms3/catalogo/tipos-producto")
@Tag(
        name = "MS3 - Catálogo - Tipos de producto",
        description = "Endpoints protegidos para administrar tipos de producto usados para clasificar productos y definir atributos esperados."
)
public class TipoProductoController {

    private final TipoProductoService tipoProductoService;

    @PostMapping
    @Operation(
            summary = "Crear tipo de producto",
            description = "Registra un nuevo tipo de producto. El service valida permisos, duplicados, reglas funcionales, auditoría y eventos Outbox si corresponde."
    )
    public ResponseEntity<ApiResponseDto<TipoProductoResponseDto>> crear(
            @Valid @RequestBody TipoProductoCreateRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(tipoProductoService.crear(request));
    }

    @PutMapping("/{idTipoProducto}")
    @Operation(
            summary = "Actualizar tipo de producto",
            description = "Actualiza datos funcionales de un tipo de producto. No crea productos, no modifica SKU, no modifica stock y no gestiona atributos desde el controller."
    )
    public ResponseEntity<ApiResponseDto<TipoProductoResponseDto>> actualizar(
            @Parameter(description = "ID técnico del tipo de producto.", required = true)
            @Positive(message = "El ID del tipo de producto debe ser positivo.")
            @PathVariable Long idTipoProducto,
            @Valid @RequestBody TipoProductoUpdateRequestDto request
    ) {
        return ResponseEntity.ok(tipoProductoService.actualizar(idTipoProducto, request));
    }

    @PatchMapping("/{idTipoProducto}/estado")
    @Operation(
            summary = "Cambiar estado de tipo de producto",
            description = "Activa o inactiva lógicamente un tipo de producto. No elimina físicamente registros ni productos relacionados."
    )
    public ResponseEntity<ApiResponseDto<TipoProductoResponseDto>> cambiarEstado(
            @Parameter(description = "ID técnico del tipo de producto.", required = true)
            @Positive(message = "El ID del tipo de producto debe ser positivo.")
            @PathVariable Long idTipoProducto,
            @Valid @RequestBody EstadoChangeRequestDto request
    ) {
        return ResponseEntity.ok(tipoProductoService.cambiarEstado(idTipoProducto, request));
    }

    @GetMapping("/{idTipoProducto}")
    @Operation(
            summary = "Obtener tipo de producto por ID",
            description = "Obtiene la respuesta resumida de un tipo de producto por su ID técnico."
    )
    public ResponseEntity<ApiResponseDto<TipoProductoResponseDto>> obtenerPorId(
            @Parameter(description = "ID técnico del tipo de producto.", required = true)
            @Positive(message = "El ID del tipo de producto debe ser positivo.")
            @PathVariable Long idTipoProducto
    ) {
        return ResponseEntity.ok(tipoProductoService.obtenerPorId(idTipoProducto));
    }

    @GetMapping("/{idTipoProducto}/detalle")
    @Operation(
            summary = "Obtener detalle de tipo de producto",
            description = "Obtiene el detalle administrativo de un tipo de producto, incluyendo conteos y atributos asociados si el service los expone."
    )
    public ResponseEntity<ApiResponseDto<TipoProductoDetailResponseDto>> obtenerDetalle(
            @Parameter(description = "ID técnico del tipo de producto.", required = true)
            @Positive(message = "El ID del tipo de producto debe ser positivo.")
            @PathVariable Long idTipoProducto
    ) {
        return ResponseEntity.ok(tipoProductoService.obtenerDetalle(idTipoProducto));
    }

    @GetMapping
    @Operation(
            summary = "Listar tipos de producto",
            description = "Lista tipos de producto con filtros y paginación. Los filtros, estado activo por defecto, ordenamiento permitido y Specification se resuelven en el service."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<TipoProductoResponseDto>>> listar(
            @Valid @ParameterObject @ModelAttribute TipoProductoFilterDto filter,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(tipoProductoService.listar(filter, pageRequest));
    }

    @GetMapping("/lookup")
    @Operation(
            summary = "Buscar tipos de producto para selección",
            description = "Devuelve opciones livianas de tipos de producto activos para autocompletados, combos o selectores del frontend administrativo."
    )
    public ResponseEntity<ApiResponseDto<List<TipoProductoOptionDto>>> lookup(
            @Parameter(description = "Texto de búsqueda por código, nombre o descripción.")
            @Size(max = 120, message = "La búsqueda no debe superar 120 caracteres.")
            @RequestParam(required = false) String search,
            @Parameter(description = "Cantidad máxima de resultados.")
            @Min(value = 1, message = "El límite mínimo es 1.")
            @Max(value = 50, message = "El límite máximo es 50.")
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(tipoProductoService.lookup(search, limit));
    }
}