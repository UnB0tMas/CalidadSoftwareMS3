// ruta: src/main/java/com/upsjb/ms3/service/contract/CloudinaryService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.integration.cloudinary.CloudinaryDeleteRequest;
import com.upsjb.ms3.integration.cloudinary.CloudinaryDeleteResponse;
import com.upsjb.ms3.integration.cloudinary.CloudinaryUploadRequest;
import com.upsjb.ms3.integration.cloudinary.CloudinaryUploadResponse;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {

    CloudinaryUploadResponse subirImagenProducto(MultipartFile archivo, String codigoProducto);

    CloudinaryUploadResponse subirImagenProducto(
            MultipartFile archivo,
            String codigoProducto,
            Map<String, String> metadata,
            List<String> tags
    );

    CloudinaryUploadResponse subirImagenSku(
            MultipartFile archivo,
            String codigoProducto,
            String codigoSku
    );

    CloudinaryUploadResponse subirImagenSku(
            MultipartFile archivo,
            String codigoProducto,
            String codigoSku,
            Map<String, String> metadata,
            List<String> tags
    );

    CloudinaryUploadResponse subir(CloudinaryUploadRequest request);

    CloudinaryDeleteResponse eliminar(String publicId);

    CloudinaryDeleteResponse eliminar(String publicId, String resourceType, Boolean invalidate);

    CloudinaryDeleteResponse eliminar(CloudinaryDeleteRequest request);

    String folderProductos(String codigoProducto);

    String folderSku(String codigoProducto, String codigoSku);
}