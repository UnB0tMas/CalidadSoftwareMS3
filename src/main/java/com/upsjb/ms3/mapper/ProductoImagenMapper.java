// ruta: src/main/java/com/upsjb/ms3/mapper/ProductoImagenMapper.java
package com.upsjb.ms3.mapper;

import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoImagenCloudinary;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoImagenUpdateRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.request.ProductoImagenUploadRequestDto;
import com.upsjb.ms3.dto.catalogo.producto.response.ProductoImagenResponseDto;
import org.springframework.stereotype.Component;

@Component
public class ProductoImagenMapper {

    public ProductoImagenCloudinary toEntity(
            ProductoImagenUploadRequestDto request,
            Producto producto,
            ProductoSku sku,
            String cloudinaryAssetId,
            String cloudinaryPublicId,
            Long cloudinaryVersion,
            String secureUrl,
            String url,
            String resourceType,
            String format,
            Long bytes,
            Integer width,
            Integer height,
            String folder,
            String originalFilename,
            Long creadoPorIdUsuarioMs1
    ) {
        if (request == null) {
            return null;
        }

        ProductoImagenCloudinary entity = new ProductoImagenCloudinary();
        entity.setProducto(producto);
        entity.setSku(sku);
        entity.setCloudinaryAssetId(cloudinaryAssetId);
        entity.setCloudinaryPublicId(cloudinaryPublicId);
        entity.setCloudinaryVersion(cloudinaryVersion);
        entity.setSecureUrl(secureUrl);
        entity.setUrl(url);
        entity.setResourceType(resourceType == null ? "image" : resourceType);
        entity.setFormat(format);
        entity.setBytes(bytes);
        entity.setWidth(width);
        entity.setHeight(height);
        entity.setFolder(folder);
        entity.setOriginalFilename(originalFilename);
        entity.setAltText(request.altText());
        entity.setTitulo(request.titulo());
        entity.setOrden(defaultInteger(request.orden(), 0));
        entity.setPrincipal(defaultBoolean(request.principal(), false));
        entity.setCreadoPorIdUsuarioMs1(creadoPorIdUsuarioMs1);

        return entity;
    }

    public void updateEntity(ProductoImagenCloudinary entity, ProductoImagenUpdateRequestDto request) {
        if (entity == null || request == null) {
            return;
        }

        entity.setAltText(request.altText());
        entity.setTitulo(request.titulo());

        if (request.orden() != null) {
            entity.setOrden(request.orden());
        }
    }

    public ProductoImagenResponseDto toResponse(ProductoImagenCloudinary entity) {
        if (entity == null) {
            return null;
        }

        Producto producto = entity.getProducto();
        ProductoSku sku = entity.getSku();

        return ProductoImagenResponseDto.builder()
                .idImagen(entity.getIdImagen())
                .idProducto(producto == null ? null : producto.getIdProducto())
                .idSku(sku == null ? null : sku.getIdSku())
                .codigoProducto(producto == null ? null : producto.getCodigoProducto())
                .nombreProducto(producto == null ? null : producto.getNombre())
                .codigoSku(sku == null ? null : sku.getCodigoSku())
                .cloudinaryAssetId(entity.getCloudinaryAssetId())
                .cloudinaryPublicId(entity.getCloudinaryPublicId())
                .cloudinaryVersion(entity.getCloudinaryVersion())
                .secureUrl(entity.getSecureUrl())
                .url(entity.getUrl())
                .resourceType(entity.getResourceType())
                .format(entity.getFormat())
                .bytes(entity.getBytes())
                .width(entity.getWidth())
                .height(entity.getHeight())
                .folder(entity.getFolder())
                .originalFilename(entity.getOriginalFilename())
                .altText(entity.getAltText())
                .titulo(entity.getTitulo())
                .orden(entity.getOrden())
                .principal(entity.getPrincipal())
                .creadoPorIdUsuarioMs1(entity.getCreadoPorIdUsuarioMs1())
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public void markPrincipal(ProductoImagenCloudinary entity, Boolean principal) {
        if (entity == null) {
            return;
        }

        entity.setPrincipal(defaultBoolean(principal, false));
    }

    public void deactivate(ProductoImagenCloudinary entity) {
        if (entity == null) {
            return;
        }

        entity.setPrincipal(Boolean.FALSE);
        entity.inactivar();
    }

    private Integer defaultInteger(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }

    private Boolean defaultBoolean(Boolean value, boolean defaultValue) {
        return value == null ? defaultValue : value;
    }
}