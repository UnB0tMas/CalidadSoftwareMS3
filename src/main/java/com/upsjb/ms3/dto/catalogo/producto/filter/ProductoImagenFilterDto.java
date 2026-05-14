// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/producto/filter/ProductoImagenFilterDto.java
package com.upsjb.ms3.dto.catalogo.producto.filter;

import com.upsjb.ms3.dto.shared.DateRangeFilterDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ProductoImagenFilterDto(

        @Size(max = 250, message = "La búsqueda no debe superar 250 caracteres.")
        String search,

        Long idImagen,

        Long idProducto,

        @Size(max = 80, message = "El código de producto no debe superar 80 caracteres.")
        String codigoProducto,

        @Size(max = 240, message = "El slug de producto no debe superar 240 caracteres.")
        String slugProducto,

        Long idSku,

        @Size(max = 100, message = "El código SKU no debe superar 100 caracteres.")
        String codigoSku,

        @Size(max = 100, message = "El barcode no debe superar 100 caracteres.")
        String barcode,

        @Size(max = 300, message = "El public_id de Cloudinary no debe superar 300 caracteres.")
        String cloudinaryPublicId,

        @Size(max = 150, message = "El asset_id de Cloudinary no debe superar 150 caracteres.")
        String cloudinaryAssetId,

        @Size(max = 30, message = "El resource type no debe superar 30 caracteres.")
        String resourceType,

        @Size(max = 30, message = "El formato no debe superar 30 caracteres.")
        String format,

        Boolean principal,

        Boolean soloProductoBase,

        Boolean soloSku,

        Long creadoPorIdUsuarioMs1,

        Boolean estado,

        @Valid
        DateRangeFilterDto fechaCreacion
) {
}