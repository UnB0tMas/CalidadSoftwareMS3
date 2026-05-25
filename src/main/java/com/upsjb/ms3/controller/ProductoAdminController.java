// ruta: src/main/java/com/upsjb/ms3/controller/ProductoAdminController.java
package com.upsjb.ms3.controller;

import com.upsjb.ms3.dto.catalogo.producto.filter.ProductoFilterDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoCreateRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoEstadoRegistroRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoPublicacionRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoVentaEstadoRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoDetailResponseDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.service.contract.ProductoAdminService;
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
@RequestMapping("/api/ms3/catalogo/productos")
@Tag(
        name = "MS3 - Catálogo - Productos administración",
        description = "Endpoints protegidos para administrar productos base del catálogo. No gestionan SKU, imágenes, precios ni stock directamente."
)
public class ProductoAdminController {

    private final ProductoAdminService productoAdminService;

    @PostMapping
    @Operation(
            summary = "Crear producto base",
            description = "Registra un producto base en el catálogo. El service resuelve referencias, genera código/slug, valida permisos, registra auditoría y Outbox cuando corresponde."
    )
    public ResponseEntity<ApiResponseDto<ProductoResponseDto>> crear(
            @Valid @RequestBody ProductoCreateRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productoAdminService.crear(request));
    }

    @PutMapping("/{idProducto}")
    @Operation(
            summary = "Actualizar producto base",
            description = "Actualiza datos administrativos del producto base. No modifica SKU, imágenes, precios ni stock."
    )
    public ResponseEntity<ApiResponseDto<ProductoResponseDto>> actualizar(
            @Parameter(description = "ID técnico del producto.", required = true)
            @Positive(message = "El ID del producto debe ser positivo.")
            @PathVariable Long idProducto,
            @Valid @RequestBody ProductoUpdateRequestDto request
    ) {
        return ResponseEntity.ok(productoAdminService.actualizar(idProducto, request));
    }

    @PatchMapping("/{idProducto}/estado-registro")
    @Operation(
            summary = "Cambiar estado de registro del producto",
            description = "Cambia el estado administrativo del producto, por ejemplo BORRADOR, ACTIVO, INACTIVO o DESCONTINUADO. El service valida reglas funcionales."
    )
    public ResponseEntity<ApiResponseDto<ProductoResponseDto>> cambiarEstadoRegistro(
            @Parameter(description = "ID técnico del producto.", required = true)
            @Positive(message = "El ID del producto debe ser positivo.")
            @PathVariable Long idProducto,
            @Valid @RequestBody ProductoEstadoRegistroRequestDto request
    ) {
        return ResponseEntity.ok(productoAdminService.cambiarEstadoRegistro(idProducto, request));
    }

    @PatchMapping("/{idProducto}/publicacion")
    @Operation(
            summary = "Cambiar publicación del producto",
            description = "Publica, programa, oculta o despublica un producto. El service valida SKU activo, precio vigente, imagen principal y coherencia de venta/publicación."
    )
    public ResponseEntity<ApiResponseDto<ProductoResponseDto>> cambiarPublicacion(
            @Parameter(description = "ID técnico del producto.", required = true)
            @Positive(message = "El ID del producto debe ser positivo.")
            @PathVariable Long idProducto,
            @Valid @RequestBody ProductoPublicacionRequestDto request
    ) {
        return ResponseEntity.ok(productoAdminService.publicar(idProducto, request));
    }

    @PatchMapping("/{idProducto}/estado-venta")
    @Operation(
            summary = "Cambiar estado de venta del producto",
            description = "Cambia el estado comercial de venta del producto, por ejemplo VENDIBLE, SOLO_VISIBLE, AGOTADO, PROXIMAMENTE o NO_VENDIBLE."
    )
    public ResponseEntity<ApiResponseDto<ProductoResponseDto>> cambiarEstadoVenta(
            @Parameter(description = "ID técnico del producto.", required = true)
            @Positive(message = "El ID del producto debe ser positivo.")
            @PathVariable Long idProducto,
            @Valid @RequestBody ProductoVentaEstadoRequestDto request
    ) {
        return ResponseEntity.ok(productoAdminService.cambiarEstadoVenta(idProducto, request));
    }

    @PatchMapping("/{idProducto}/estado")
    @Operation(
            summary = "Activar o inactivar lógicamente producto",
            description = "Activa o inactiva lógicamente un producto. No elimina físicamente registros ni modifica SKU, stock, precios o movimientos."
    )
    public ResponseEntity<ApiResponseDto<ProductoResponseDto>> cambiarEstadoLogico(
            @Parameter(description = "ID técnico del producto.", required = true)
            @Positive(message = "El ID del producto debe ser positivo.")
            @PathVariable Long idProducto,
            @Valid @RequestBody EstadoChangeRequestDto request
    ) {
        return ResponseEntity.ok(productoAdminService.inactivar(idProducto, request));
    }

    @GetMapping("/{idProducto}")
    @Operation(
            summary = "Obtener producto por ID",
            description = "Obtiene la respuesta resumida de un producto por su ID técnico."
    )
    public ResponseEntity<ApiResponseDto<ProductoResponseDto>> obtenerPorId(
            @Parameter(description = "ID técnico del producto.", required = true)
            @Positive(message = "El ID del producto debe ser positivo.")
            @PathVariable Long idProducto
    ) {
        return ResponseEntity.ok(productoAdminService.obtenerPorId(idProducto));
    }

    @GetMapping("/{idProducto}/detalle")
    @Operation(
            summary = "Obtener detalle administrativo de producto",
            description = "Obtiene el detalle administrativo del producto base. No debe exponer entidades JPA."
    )
    public ResponseEntity<ApiResponseDto<ProductoDetailResponseDto>> obtenerDetalle(
            @Parameter(description = "ID técnico del producto.", required = true)
            @Positive(message = "El ID del producto debe ser positivo.")
            @PathVariable Long idProducto
    ) {
        return ResponseEntity.ok(productoAdminService.obtenerDetalle(idProducto));
    }

    @GetMapping
    @Operation(
            summary = "Listar productos administrativos",
            description = "Lista productos con filtros y paginación. Los filtros se procesan en service/specification."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<ProductoResponseDto>>> listar(
            @Valid @ParameterObject @ModelAttribute ProductoFilterDto filter,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(productoAdminService.listar(filter, pageRequest));
    }
}