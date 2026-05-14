// ruta: src/main/java/com/upsjb/ms3/service/impl/CloudinaryServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.config.CloudinaryProperties;
import com.upsjb.ms3.integration.cloudinary.CloudinaryClient;
import com.upsjb.ms3.integration.cloudinary.CloudinaryDeleteRequest;
import com.upsjb.ms3.integration.cloudinary.CloudinaryDeleteResponse;
import com.upsjb.ms3.integration.cloudinary.CloudinaryException;
import com.upsjb.ms3.integration.cloudinary.CloudinaryUploadRequest;
import com.upsjb.ms3.integration.cloudinary.CloudinaryUploadResponse;
import com.upsjb.ms3.policy.CloudinaryPolicy;
import com.upsjb.ms3.service.contract.CloudinaryService;
import com.upsjb.ms3.shared.exception.CloudinaryIntegrationException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.util.FileNameUtil;
import com.upsjb.ms3.util.StringNormalizer;
import com.upsjb.ms3.validator.CloudinaryImageValidator;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private static final String RESOURCE_TYPE_IMAGE = "image";
    private static final String DEFAULT_PRODUCT_TAG = "ms3-producto";
    private static final String DEFAULT_SKU_TAG = "ms3-sku";

    private final CloudinaryClient cloudinaryClient;
    private final CloudinaryProperties cloudinaryProperties;
    private final CloudinaryImageValidator cloudinaryImageValidator;
    private final CloudinaryPolicy cloudinaryPolicy;

    @Override
    public CloudinaryUploadResponse subirImagenProducto(MultipartFile archivo, String codigoProducto) {
        return subirImagenProducto(archivo, codigoProducto, Map.of(), List.of(DEFAULT_PRODUCT_TAG));
    }

    @Override
    public CloudinaryUploadResponse subirImagenProducto(
            MultipartFile archivo,
            String codigoProducto,
            Map<String, String> metadata,
            List<String> tags
    ) {
        String safeCodigoProducto = requireReference(codigoProducto, "codigoProducto");
        String folder = folderProductos(safeCodigoProducto);
        String publicId = buildPublicId("producto-" + safeCodigoProducto, archivo);

        Map<String, String> safeMetadata = safeMetadata(metadata);
        safeMetadata.put("scope", "producto");
        safeMetadata.put("codigo_producto", safeCodigoProducto);

        return subirMultipart(
                archivo,
                folder,
                publicId,
                safeMetadata,
                mergeTags(tags, DEFAULT_PRODUCT_TAG)
        );
    }

    @Override
    public CloudinaryUploadResponse subirImagenSku(
            MultipartFile archivo,
            String codigoProducto,
            String codigoSku
    ) {
        return subirImagenSku(archivo, codigoProducto, codigoSku, Map.of(), List.of(DEFAULT_SKU_TAG));
    }

    @Override
    public CloudinaryUploadResponse subirImagenSku(
            MultipartFile archivo,
            String codigoProducto,
            String codigoSku,
            Map<String, String> metadata,
            List<String> tags
    ) {
        String safeCodigoProducto = requireReference(codigoProducto, "codigoProducto");
        String safeCodigoSku = requireReference(codigoSku, "codigoSku");
        String folder = folderSku(safeCodigoProducto, safeCodigoSku);
        String publicId = buildPublicId("sku-" + safeCodigoProducto + "-" + safeCodigoSku, archivo);

        Map<String, String> safeMetadata = safeMetadata(metadata);
        safeMetadata.put("scope", "sku");
        safeMetadata.put("codigo_producto", safeCodigoProducto);
        safeMetadata.put("codigo_sku", safeCodigoSku);

        return subirMultipart(
                archivo,
                folder,
                publicId,
                safeMetadata,
                mergeTags(tags, DEFAULT_SKU_TAG)
        );
    }

    @Override
    public CloudinaryUploadResponse subir(CloudinaryUploadRequest request) {
        cloudinaryPolicy.ensureCloudinaryEnabled();

        try {
            CloudinaryUploadResponse response = cloudinaryClient.upload(request);
            validateUploadResponse(response);

            log.info(
                    "Imagen subida a Cloudinary. publicId={}, assetId={}, folder={}, bytes={}",
                    response.publicId(),
                    response.assetId(),
                    response.folder(),
                    response.bytes()
            );

            return response;
        } catch (CloudinaryException | ValidationException ex) {
            log.warn(
                    "No se pudo completar la operación de subida a Cloudinary. code={}, message={}",
                    ex instanceof CloudinaryException cloudinaryException ? cloudinaryException.getCode() : ex.getCode(),
                    ex.getMessage()
            );
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico inesperado al subir imagen a Cloudinary. publicId={}, folder={}",
                    request == null ? null : request.publicId(),
                    request == null ? null : request.folder(),
                    ex
            );
            throw CloudinaryIntegrationException.uploadFailed(ex);
        }
    }

    @Override
    public CloudinaryDeleteResponse eliminar(String publicId) {
        return eliminar(publicId, RESOURCE_TYPE_IMAGE, cloudinaryProperties.isInvalidateOnDelete());
    }

    @Override
    public CloudinaryDeleteResponse eliminar(String publicId, String resourceType, Boolean invalidate) {
        return eliminar(new CloudinaryDeleteRequest(publicId, resourceType, invalidate));
    }

    @Override
    public CloudinaryDeleteResponse eliminar(CloudinaryDeleteRequest request) {
        cloudinaryPolicy.ensureCloudinaryEnabled();

        try {
            CloudinaryDeleteResponse response = cloudinaryClient.delete(request);

            log.info(
                    "Recurso Cloudinary procesado para eliminación. publicId={}, result={}, deleted={}",
                    response.publicId(),
                    response.result(),
                    response.deleted()
            );

            return response;
        } catch (CloudinaryException | ValidationException ex) {
            log.warn(
                    "No se pudo completar la operación de eliminación en Cloudinary. code={}, message={}",
                    ex instanceof CloudinaryException cloudinaryException ? cloudinaryException.getCode() : ex.getCode(),
                    ex.getMessage()
            );
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico inesperado al eliminar recurso de Cloudinary. publicId={}",
                    request == null ? null : request.publicId(),
                    ex
            );
            throw CloudinaryIntegrationException.deleteFailed(ex);
        }
    }

    @Override
    public String folderProductos(String codigoProducto) {
        String safeCodigoProducto = requireReference(codigoProducto, "codigoProducto");

        return FileNameUtil.normalizeFolder(
                cloudinaryProperties.productosFolderPath()
                        + "/"
                        + StringNormalizer.normalizeForCode(safeCodigoProducto).toLowerCase()
        );
    }

    @Override
    public String folderSku(String codigoProducto, String codigoSku) {
        String safeCodigoProducto = requireReference(codigoProducto, "codigoProducto");
        String safeCodigoSku = requireReference(codigoSku, "codigoSku");

        return FileNameUtil.normalizeFolder(
                cloudinaryProperties.skuFolderPath()
                        + "/"
                        + StringNormalizer.normalizeForCode(safeCodigoProducto).toLowerCase()
                        + "/"
                        + StringNormalizer.normalizeForCode(safeCodigoSku).toLowerCase()
        );
    }

    private CloudinaryUploadResponse subirMultipart(
            MultipartFile archivo,
            String folder,
            String publicId,
            Map<String, String> metadata,
            List<String> tags
    ) {
        cloudinaryImageValidator.validateUploadFile(archivo);

        byte[] content = readBytes(archivo);

        CloudinaryUploadRequest request = new CloudinaryUploadRequest(
                content,
                FileNameUtil.sanitizeOriginalFilename(archivo.getOriginalFilename()),
                archivo.getContentType(),
                folder,
                publicId,
                RESOURCE_TYPE_IMAGE,
                Boolean.FALSE,
                Boolean.TRUE,
                Boolean.TRUE,
                metadata,
                tags
        );

        return subir(request);
    }

    private byte[] readBytes(MultipartFile archivo) {
        try {
            return archivo.getBytes();
        } catch (IOException ex) {
            log.error(
                    "No se pudo leer el archivo MultipartFile antes de subirlo a Cloudinary. originalFilename={}, size={}",
                    archivo == null ? null : archivo.getOriginalFilename(),
                    archivo == null ? null : archivo.getSize(),
                    ex
            );
            throw CloudinaryIntegrationException.uploadFailed(ex);
        }
    }

    private void validateUploadResponse(CloudinaryUploadResponse response) {
        if (response == null) {
            throw new CloudinaryIntegrationException("Cloudinary no devolvió respuesta de subida.");
        }

        cloudinaryImageValidator.validateCloudinaryResponse(
                response.publicId(),
                response.secureUrl(),
                response.resourceType(),
                response.format(),
                response.bytes(),
                response.width(),
                response.height()
        );
    }

    private String buildPublicId(String prefix, MultipartFile archivo) {
        String storedFilename = FileNameUtil.generateStoredFilename(
                StringNormalizer.normalizeForCode(prefix),
                archivo == null ? null : archivo.getOriginalFilename()
        );

        return FileNameUtil.baseName(storedFilename);
    }

    private String requireReference(String value, String field) {
        if (!StringNormalizer.hasText(value)) {
            throw new ValidationException(
                    "CLOUDINARY_REFERENCIA_REQUERIDA",
                    "Debe indicar la referencia funcional requerida: " + field + "."
            );
        }

        return StringNormalizer.normalizeForCode(value);
    }

    private Map<String, String> safeMetadata(Map<String, String> metadata) {
        Map<String, String> safe = new LinkedHashMap<>();

        if (metadata == null || metadata.isEmpty()) {
            return safe;
        }

        metadata.forEach((key, value) -> {
            String safeKey = StringNormalizer.truncateOrNull(
                    StringNormalizer.normalizeForCode(key).toLowerCase(),
                    60
            );
            String safeValue = StringNormalizer.truncateOrNull(value, 250);

            if (StringNormalizer.hasText(safeKey) && StringNormalizer.hasText(safeValue)) {
                safe.put(safeKey, safeValue);
            }
        });

        return safe;
    }

    private List<String> mergeTags(List<String> tags, String defaultTag) {
        Map<String, String> safe = new LinkedHashMap<>();

        if (StringNormalizer.hasText(defaultTag)) {
            safe.put(defaultTag, defaultTag);
        }

        if (tags != null) {
            tags.stream()
                    .filter(StringNormalizer::hasText)
                    .map(String::trim)
                    .map(value -> value.replaceAll("[^A-Za-z0-9_-]+", "-"))
                    .map(value -> StringNormalizer.truncate(value, 60))
                    .filter(StringNormalizer::hasText)
                    .forEach(value -> safe.putIfAbsent(value, value));
        }

        return List.copyOf(safe.values());
    }
}