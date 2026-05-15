// ruta: src/main/java/com/upsjb/ms3/validator/ProductoImagenValidator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.domain.entity.Producto;
import com.upsjb.ms3.domain.entity.ProductoImagenCloudinary;
import com.upsjb.ms3.domain.entity.ProductoSku;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.NotFoundException;
import com.upsjb.ms3.shared.validation.ValidationErrorCollector;
import com.upsjb.ms3.util.StringNormalizer;
import org.springframework.stereotype.Component;

@Component
public class ProductoImagenValidator {

    public void validateCreate(
            Producto producto,
            ProductoSku sku,
            Boolean principal,
            Integer orden,
            boolean duplicatedPrincipalForProduct,
            boolean duplicatedPrincipalForSku
    ) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        if (producto == null) {
            errors.add("producto", "El producto es obligatorio.", "REQUIRED", null);
        } else if (!producto.isActivo()) {
            errors.add("producto", "El producto debe estar activo.", "INACTIVE", producto.getIdProducto());
        }

        if (sku != null) {
            if (!sku.isActivo()) {
                errors.add("sku", "El SKU debe estar activo.", "INACTIVE", sku.getIdSku());
            }

            if (producto != null
                    && sku.getProducto() != null
                    && !producto.getIdProducto().equals(sku.getProducto().getIdProducto())) {
                errors.add("sku", "El SKU no pertenece al producto indicado.", "INVALID_RELATION", sku.getIdSku());
            }
        }

        validateOrden(orden, errors);

        errors.throwIfAny("No se puede registrar la imagen del producto.");

        if (Boolean.TRUE.equals(principal) && sku == null && duplicatedPrincipalForProduct) {
            throw new ConflictException(
                    "IMAGEN_PRINCIPAL_PRODUCTO_DUPLICADA",
                    "El producto ya tiene una imagen principal activa."
            );
        }

        if (Boolean.TRUE.equals(principal) && sku != null && duplicatedPrincipalForSku) {
            throw new ConflictException(
                    "IMAGEN_PRINCIPAL_SKU_DUPLICADA",
                    "El SKU ya tiene una imagen principal activa."
            );
        }
    }

    public void validateUpdate(
            String altText,
            String titulo,
            Integer orden
    ) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        if (StringNormalizer.hasText(altText) && altText.length() > 250) {
            errors.add("altText", "El texto alternativo no debe superar 250 caracteres.", "MAX_LENGTH", altText);
        }

        if (StringNormalizer.hasText(titulo) && titulo.length() > 180) {
            errors.add("titulo", "El título no debe superar 180 caracteres.", "MAX_LENGTH", titulo);
        }

        validateOrden(orden, errors);

        errors.throwIfAny("No se puede actualizar la metadata de la imagen.");
    }

    public void validateMetadata(
            String cloudinaryPublicId,
            String secureUrl,
            String resourceType,
            String format,
            Long bytes,
            Integer width,
            Integer height
    ) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        if (!StringNormalizer.hasText(cloudinaryPublicId)) {
            errors.add("cloudinaryPublicId", "El public_id de Cloudinary es obligatorio.", "REQUIRED", cloudinaryPublicId);
        }

        if (!StringNormalizer.hasText(secureUrl)) {
            errors.add("secureUrl", "La URL segura de Cloudinary es obligatoria.", "REQUIRED", secureUrl);
        }

        if (!StringNormalizer.hasText(resourceType)) {
            errors.add("resourceType", "El resource type de Cloudinary es obligatorio.", "REQUIRED", resourceType);
        }

        if (StringNormalizer.hasText(format) && format.length() > 30) {
            errors.add("format", "El formato no debe superar 30 caracteres.", "MAX_LENGTH", format);
        }

        if (bytes != null && bytes <= 0) {
            errors.add("bytes", "El tamaño de la imagen debe ser mayor a cero.", "INVALID_VALUE", bytes);
        }

        if (width != null && width <= 0) {
            errors.add("width", "El ancho de la imagen debe ser mayor a cero.", "INVALID_VALUE", width);
        }

        if (height != null && height <= 0) {
            errors.add("height", "El alto de la imagen debe ser mayor a cero.", "INVALID_VALUE", height);
        }

        errors.throwIfAny("La metadata de Cloudinary no es válida.");
    }

    public void validateCanSetPrincipal(
            ProductoImagenCloudinary imagen,
            boolean duplicatedPrincipalForProduct,
            boolean duplicatedPrincipalForSku
    ) {
        requireActive(imagen);

        if (imagen.getSku() == null && duplicatedPrincipalForProduct) {
            throw new ConflictException(
                    "IMAGEN_PRINCIPAL_PRODUCTO_DUPLICADA",
                    "El producto ya tiene otra imagen principal activa."
            );
        }

        if (imagen.getSku() != null && duplicatedPrincipalForSku) {
            throw new ConflictException(
                    "IMAGEN_PRINCIPAL_SKU_DUPLICADA",
                    "El SKU ya tiene otra imagen principal activa."
            );
        }
    }

    public void requireActive(ProductoImagenCloudinary imagen) {
        if (imagen == null) {
            throw new NotFoundException(
                    "IMAGEN_PRODUCTO_NO_ENCONTRADA",
                    "Imagen de producto no encontrada."
            );
        }

        if (!imagen.isActivo()) {
            throw new NotFoundException(
                    "IMAGEN_PRODUCTO_INACTIVA",
                    "La imagen de producto no está activa."
            );
        }
    }

    private void validateOrden(Integer orden, ValidationErrorCollector errors) {
        if (orden != null && orden < 0) {
            errors.add("orden", "El orden no puede ser negativo.", "INVALID_VALUE", orden);
        }
    }
}