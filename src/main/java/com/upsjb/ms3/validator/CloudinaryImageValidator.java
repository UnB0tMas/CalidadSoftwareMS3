// ruta: src/main/java/com/upsjb/ms3/validator/CloudinaryImageValidator.java
package com.upsjb.ms3.validator;

import com.upsjb.ms3.config.CloudinaryProperties;
import com.upsjb.ms3.domain.enums.CloudinaryResourceType;
import com.upsjb.ms3.shared.exception.ExternalServiceException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.validation.ValidationErrorCollector;
import com.upsjb.ms3.util.FileNameUtil;
import com.upsjb.ms3.util.MimeTypeUtil;
import com.upsjb.ms3.util.StringNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class CloudinaryImageValidator {

    private final CloudinaryProperties cloudinaryProperties;

    public void validateUploadFile(MultipartFile file) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        if (file == null || file.isEmpty()) {
            errors.add("file", "Debe enviar una imagen.", "REQUIRED", null);
            errors.throwIfAny("No se puede subir la imagen.");
            return;
        }

        if (file.getSize() <= 0) {
            errors.add("file", "La imagen está vacía.", "EMPTY_FILE", file.getOriginalFilename());
        }

        if (file.getSize() > cloudinaryProperties.getMaxFileSizeBytes()) {
            errors.add(
                    "file",
                    "La imagen supera el tamaño máximo permitido.",
                    "MAX_FILE_SIZE",
                    file.getOriginalFilename()
            );
        }

        String contentType = file.getContentType();
        if (!MimeTypeUtil.isAllowedProductImage(contentType)
                && !cloudinaryProperties.isAllowedContentType(contentType)) {
            errors.add(
                    "contentType",
                    "El formato de imagen no está permitido. Use jpg, jpeg, png o webp.",
                    "INVALID_MIME_TYPE",
                    contentType
            );
        }

        String extension = FileNameUtil.extension(file.getOriginalFilename());
        if (StringNormalizer.hasText(extension) && !MimeTypeUtil.isAllowedProductImageExtension(extension)) {
            errors.add(
                    "extension",
                    "La extensión del archivo no está permitida.",
                    "INVALID_EXTENSION",
                    extension
            );
        }

        errors.throwIfAny("No se puede subir la imagen.");
    }

    public void validateCloudinaryResponse(
            String publicId,
            String secureUrl,
            String resourceType,
            String format,
            Long bytes,
            Integer width,
            Integer height
    ) {
        ValidationErrorCollector errors = ValidationErrorCollector.create();

        if (!StringNormalizer.hasText(publicId)) {
            errors.add("publicId", "Cloudinary no devolvió public_id.", "REQUIRED", publicId);
        }

        if (!StringNormalizer.hasText(secureUrl)) {
            errors.add("secureUrl", "Cloudinary no devolvió secure_url.", "REQUIRED", secureUrl);
        } else if (!secureUrl.startsWith("https://")) {
            errors.add("secureUrl", "Cloudinary devolvió una URL no segura.", "INVALID_VALUE", secureUrl);
        }

        if (!StringNormalizer.hasText(resourceType)) {
            errors.add("resourceType", "Cloudinary no devolvió resource_type.", "REQUIRED", resourceType);
        } else if (!isValidImageResourceType(resourceType)) {
            errors.add("resourceType", "Cloudinary devolvió un resource_type inválido.", "INVALID_VALUE", resourceType);
        }

        if (!StringNormalizer.hasText(format)) {
            errors.add("format", "Cloudinary no devolvió formato de imagen.", "REQUIRED", format);
        }

        if (bytes == null || bytes <= 0) {
            errors.add("bytes", "Cloudinary devolvió tamaño inválido.", "INVALID_VALUE", bytes);
        }

        if (width != null && width <= 0) {
            errors.add("width", "Cloudinary devolvió ancho inválido.", "INVALID_VALUE", width);
        }

        if (height != null && height <= 0) {
            errors.add("height", "Cloudinary devolvió alto inválido.", "INVALID_VALUE", height);
        }

        if (errors.hasErrors()) {
            throw new ExternalServiceException(
                    "Cloudinary",
                    "CLOUDINARY_RESPONSE_INVALIDA",
                    "Cloudinary devolvió una respuesta incompleta o inválida."
            );
        }
    }

    public void validateResourceType(String resourceType) {
        try {
            CloudinaryResourceType parsed = CloudinaryResourceType.fromCode(resourceType);

            if (!parsed.isImage()) {
                throw new ValidationException(
                        "CLOUDINARY_RESOURCE_TYPE_INVALIDO",
                        "Solo se permiten recursos Cloudinary de tipo imagen."
                );
            }
        } catch (IllegalArgumentException ex) {
            throw new ValidationException(
                    "CLOUDINARY_RESOURCE_TYPE_INVALIDO",
                    "El resource type de Cloudinary no es válido."
            );
        }
    }

    public void validateSecureUrl(String secureUrl) {
        if (!StringNormalizer.hasText(secureUrl) || !secureUrl.startsWith("https://")) {
            throw new ValidationException(
                    "CLOUDINARY_SECURE_URL_INVALIDA",
                    "La imagen debe usar una URL segura HTTPS."
            );
        }
    }

    private boolean isValidImageResourceType(String resourceType) {
        try {
            return CloudinaryResourceType.fromCode(resourceType).isImage();
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}