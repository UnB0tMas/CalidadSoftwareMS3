// ruta: src/main/java/com/upsjb/ms3/controller/ProductoImagenController.java
package com.upsjb.ms3.controller;

import com.upsjb.ms3.dto.catalogo.producto.filter.ProductoImagenFilterDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoImagenPrincipalRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoImagenUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoImagenUploadRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoImagenResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;
import com.upsjb.ms3.service.contract.ProductoImagenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ms3/catalogo/producto-imagenes")
@Tag(
        name = "MS3 - Catálogo - Imágenes de producto",
        description = "Endpoints protegidos para gestionar imágenes Cloudinary de productos y SKU. El controller no llama Cloudinary directamente."
)
public class ProductoImagenController {

    private final ProductoImagenService productoImagenService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Subir imagen de producto o SKU",
            description = "Recibe multipart/form-data con archivo y metadata funcional. El service valida permisos, producto/SKU, Cloudinary, persistencia, auditoría y Outbox."
    )
    public ResponseEntity<ApiResponseDto<ProductoImagenResponseDto>> subir(
            @Valid @ModelAttribute ProductoImagenUploadRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productoImagenService.subir(request));
    }

    @PutMapping("/{idImagen}/metadata")
    @Operation(
            summary = "Actualizar metadata de imagen",
            description = "Actualiza metadata funcional de una imagen, como alt text, título u orden. No reemplaza el archivo ni llama Cloudinary desde controller."
    )
    public ResponseEntity<ApiResponseDto<ProductoImagenResponseDto>> actualizarMetadata(
            @Parameter(description = "ID técnico de la imagen.", required = true)
            @Positive(message = "El ID de la imagen debe ser positivo.")
            @PathVariable Long idImagen,
            @Valid @org.springframework.web.bind.annotation.RequestBody ProductoImagenUpdateRequestDto request
    ) {
        return ResponseEntity.ok(productoImagenService.actualizarMetadata(idImagen, request));
    }

    @PatchMapping("/{idImagen}/principal")
    @Operation(
            summary = "Marcar imagen como principal",
            description = "Marca una imagen como principal de producto o SKU. El service limpia la principal anterior y valida coherencia de alcance."
    )
    public ResponseEntity<ApiResponseDto<ProductoImagenResponseDto>> marcarPrincipal(
            @Parameter(description = "ID técnico de la imagen.", required = true)
            @Positive(message = "El ID de la imagen debe ser positivo.")
            @PathVariable Long idImagen,
            @Valid @org.springframework.web.bind.annotation.RequestBody ProductoImagenPrincipalRequestDto request
    ) {
        return ResponseEntity.ok(productoImagenService.marcarPrincipal(idImagen, request));
    }

    @PatchMapping("/{idImagen}/inactivar")
    @Operation(
            summary = "Inactivar imagen",
            description = "Inactiva lógicamente una imagen. No elimina físicamente registros desde el controller ni expone credenciales de Cloudinary."
    )
    public ResponseEntity<ApiResponseDto<ProductoImagenResponseDto>> inactivar(
            @Parameter(description = "ID técnico de la imagen.", required = true)
            @Positive(message = "El ID de la imagen debe ser positivo.")
            @PathVariable Long idImagen,
            @Valid @org.springframework.web.bind.annotation.RequestBody EstadoChangeRequestDto request
    ) {
        return ResponseEntity.ok(productoImagenService.inactivar(idImagen, request));
    }

    @GetMapping("/{idImagen}")
    @Operation(
            summary = "Obtener detalle de imagen",
            description = "Obtiene el detalle administrativo de una imagen activa."
    )
    public ResponseEntity<ApiResponseDto<ProductoImagenResponseDto>> obtenerDetalle(
            @Parameter(description = "ID técnico de la imagen.", required = true)
            @Positive(message = "El ID de la imagen debe ser positivo.")
            @PathVariable Long idImagen
    ) {
        return ResponseEntity.ok(productoImagenService.obtenerDetalle(idImagen));
    }

    @GetMapping
    @Operation(
            summary = "Listar imágenes de producto/SKU",
            description = "Lista imágenes con filtros y paginación. La consulta administrativa queda en service/specification."
    )
    public ResponseEntity<ApiResponseDto<PageResponseDto<ProductoImagenResponseDto>>> listar(
            @Valid @ParameterObject @ModelAttribute ProductoImagenFilterDto filter,
            @Valid @ParameterObject @ModelAttribute PageRequestDto pageRequest
    ) {
        return ResponseEntity.ok(productoImagenService.listar(filter, pageRequest));
    }

    @GetMapping("/por-producto")
    @Operation(
            summary = "Listar imágenes por producto",
            description = "Lista imágenes activas de un producto usando referencia funcional: id, código, nombre o slug."
    )
    public ResponseEntity<ApiResponseDto<List<ProductoImagenResponseDto>>> listarPorProducto(
            @Valid @ParameterObject @ModelAttribute EntityReferenceDto productoReference
    ) {
        return ResponseEntity.ok(productoImagenService.listarPorProducto(productoReference));
    }

    @GetMapping("/por-sku")
    @Operation(
            summary = "Listar imágenes por SKU",
            description = "Lista imágenes activas de un SKU usando referencia funcional: id, código SKU, barcode o código."
    )
    public ResponseEntity<ApiResponseDto<List<ProductoImagenResponseDto>>> listarPorSku(
            @Valid @ParameterObject @ModelAttribute EntityReferenceDto skuReference
    ) {
        return ResponseEntity.ok(productoImagenService.listarPorSku(skuReference));
    }

    @GetMapping("/principal/producto")
    @Operation(
            summary = "Obtener imagen principal de producto",
            description = "Obtiene la imagen principal activa del producto usando referencia funcional."
    )
    public ResponseEntity<ApiResponseDto<ProductoImagenResponseDto>> obtenerPrincipalProducto(
            @Valid @ParameterObject @ModelAttribute EntityReferenceDto productoReference
    ) {
        return ResponseEntity.ok(productoImagenService.obtenerPrincipalProducto(productoReference));
    }

    @GetMapping("/principal/sku")
    @Operation(
            summary = "Obtener imagen principal de SKU",
            description = "Obtiene la imagen principal activa del SKU usando referencia funcional."
    )
    public ResponseEntity<ApiResponseDto<ProductoImagenResponseDto>> obtenerPrincipalSku(
            @Valid @ParameterObject @ModelAttribute EntityReferenceDto skuReference
    ) {
        return ResponseEntity.ok(productoImagenService.obtenerPrincipalSku(skuReference));
    }
}