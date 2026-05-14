// ruta: src/main/java/com/upsjb/ms3/integration/cloudinary/CloudinaryErrorMapper.java
package com.upsjb.ms3.integration.cloudinary;

import java.io.IOException;
import org.springframework.stereotype.Component;

@Component
public class CloudinaryErrorMapper {

    public CloudinaryException integrationDisabled(String operation) {
        return new CloudinaryException(
                "CLOUDINARY_DESHABILITADO",
                "La integración con Cloudinary está deshabilitada para este entorno.",
                operation
        );
    }

    public CloudinaryException clientNotConfigured(String operation) {
        return new CloudinaryException(
                "CLOUDINARY_CLIENTE_NO_CONFIGURADO",
                "El cliente de Cloudinary no está configurado correctamente.",
                operation
        );
    }

    public CloudinaryException invalidRequest(String message, String operation) {
        return new CloudinaryException(
                "CLOUDINARY_REQUEST_INVALIDA",
                message,
                operation
        );
    }

    public CloudinaryException uploadFailed(Throwable cause) {
        return new CloudinaryException(
                "CLOUDINARY_UPLOAD_ERROR",
                "No se pudo subir la imagen a Cloudinary. Intente nuevamente.",
                "UPLOAD",
                cause
        );
    }

    public CloudinaryException deleteFailed(Throwable cause) {
        return new CloudinaryException(
                "CLOUDINARY_DELETE_ERROR",
                "No se pudo eliminar el recurso en Cloudinary. Intente nuevamente.",
                "DELETE",
                cause
        );
    }

    public CloudinaryException map(Throwable cause, String operation) {
        if (cause instanceof CloudinaryException cloudinaryException) {
            return cloudinaryException;
        }

        if (cause instanceof IOException) {
            return new CloudinaryException(
                    "CLOUDINARY_IO_ERROR",
                    "No se pudo comunicar correctamente con Cloudinary. Intente nuevamente.",
                    operation,
                    cause
            );
        }

        return new CloudinaryException(
                "CLOUDINARY_INTEGRATION_ERROR",
                "Ocurrió un error al comunicarse con Cloudinary.",
                operation,
                cause
        );
    }
}