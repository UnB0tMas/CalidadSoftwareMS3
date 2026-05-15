// ruta: src/main/java/com/upsjb/ms3/integration/cloudinary/CloudinaryClientImpl.java
package com.upsjb.ms3.integration.cloudinary;

import com.cloudinary.Cloudinary;
import com.upsjb.ms3.config.CloudinaryProperties;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class CloudinaryClientImpl implements CloudinaryClient {

    private static final String OPERATION_UPLOAD = "UPLOAD";
    private static final String OPERATION_DELETE = "DELETE";

    private final ObjectProvider<Cloudinary> cloudinaryProvider;
    private final CloudinaryProperties properties;
    private final CloudinaryErrorMapper errorMapper;

    @Override
    public CloudinaryUploadResponse upload(CloudinaryUploadRequest request) {
        validateUploadRequest(request);

        try {
            Map<String, Object> options = buildUploadOptions(request);

            @SuppressWarnings("unchecked")
            Map<String, Object> rawResponse = resolveClient()
                    .uploader()
                    .upload(request.content(), options);

            return CloudinaryUploadResponse.fromRaw(rawResponse);
        } catch (CloudinaryException ex) {
            throw ex;
        } catch (IOException | RuntimeException ex) {
            throw errorMapper.uploadFailed(ex);
        }
    }

    @Override
    public CloudinaryDeleteResponse delete(CloudinaryDeleteRequest request) {
        validateDeleteRequest(request);

        try {
            Map<String, Object> options = buildDeleteOptions(request);

            @SuppressWarnings("unchecked")
            Map<String, Object> rawResponse = resolveClient()
                    .uploader()
                    .destroy(request.publicId(), options);

            return CloudinaryDeleteResponse.fromRaw(request.publicId(), rawResponse);
        } catch (CloudinaryException ex) {
            throw ex;
        } catch (IOException | RuntimeException ex) {
            throw errorMapper.deleteFailed(ex);
        }
    }

    private Cloudinary resolveClient() {
        if (!properties.isEnabled()) {
            throw errorMapper.integrationDisabled("RESOLVE_CLIENT");
        }

        Cloudinary cloudinary = cloudinaryProvider.getIfAvailable();
        if (cloudinary == null) {
            throw errorMapper.clientNotConfigured("RESOLVE_CLIENT");
        }

        return cloudinary;
    }

    private void validateUploadRequest(CloudinaryUploadRequest request) {
        if (request == null) {
            throw errorMapper.invalidRequest("La solicitud de subida a Cloudinary es obligatoria.", OPERATION_UPLOAD);
        }

        if (!request.hasContent()) {
            throw errorMapper.invalidRequest("El archivo a subir está vacío.", OPERATION_UPLOAD);
        }

        if (properties.getMaxFileSizeBytes() != null
                && request.sizeBytes() > properties.getMaxFileSizeBytes()) {
            throw errorMapper.invalidRequest(
                    "El archivo supera el tamaño máximo permitido por configuración.",
                    OPERATION_UPLOAD
            );
        }

        if (StringUtils.hasText(request.contentType())
                && !properties.isAllowedContentType(request.contentType())) {
            throw errorMapper.invalidRequest(
                    "El tipo de contenido del archivo no está permitido: " + request.contentType() + ".",
                    OPERATION_UPLOAD
            );
        }
    }

    private void validateDeleteRequest(CloudinaryDeleteRequest request) {
        if (request == null) {
            throw errorMapper.invalidRequest("La solicitud de eliminación en Cloudinary es obligatoria.", OPERATION_DELETE);
        }

        if (!StringUtils.hasText(request.publicId())) {
            throw errorMapper.invalidRequest("El publicId de Cloudinary es obligatorio.", OPERATION_DELETE);
        }
    }

    private Map<String, Object> buildUploadOptions(CloudinaryUploadRequest request) {
        Map<String, Object> options = new HashMap<>();

        options.put("resource_type", resolveResourceType(request.resourceType()));
        options.put("folder", resolveFolder(request.folder()));
        options.put("overwrite", request.overwrite() == null ? Boolean.FALSE : request.overwrite());
        options.put("unique_filename", request.uniqueFilename() == null ? Boolean.TRUE : request.uniqueFilename());
        options.put("use_filename", request.useFilename() == null ? Boolean.TRUE : request.useFilename());

        if (StringUtils.hasText(request.publicId())) {
            options.put("public_id", request.publicId());
        }

        if (StringUtils.hasText(request.originalFilename())) {
            options.put("filename_override", request.originalFilename());
        }

        if (!request.metadata().isEmpty()) {
            options.put("context", request.metadata());
        }

        if (!request.tags().isEmpty()) {
            options.put("tags", request.tags());
        }

        return options;
    }

    private Map<String, Object> buildDeleteOptions(CloudinaryDeleteRequest request) {
        Map<String, Object> options = new HashMap<>();
        options.put("resource_type", resolveResourceType(request.resourceType()));
        options.put("invalidate", request.invalidate() == null
                ? properties.isInvalidateOnDelete()
                : request.shouldInvalidate());
        return options;
    }

    private String resolveResourceType(String requestedResourceType) {
        if (StringUtils.hasText(requestedResourceType)) {
            return requestedResourceType.trim();
        }

        return properties.getDefaultResourceType();
    }

    private String resolveFolder(String requestedFolder) {
        if (StringUtils.hasText(requestedFolder)) {
            return requestedFolder.trim();
        }

        return properties.productosFolderPath();
    }
}