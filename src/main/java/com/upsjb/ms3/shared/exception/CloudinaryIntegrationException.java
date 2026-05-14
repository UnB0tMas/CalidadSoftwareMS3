package com.upsjb.ms3.shared.exception;

public class CloudinaryIntegrationException extends ExternalServiceException {

    public CloudinaryIntegrationException(String message) {
        super("Cloudinary", "CLOUDINARY_INTEGRATION_ERROR", message);
    }

    public CloudinaryIntegrationException(String message, Throwable cause) {
        super("Cloudinary", "CLOUDINARY_INTEGRATION_ERROR", message, cause);
    }

    public static CloudinaryIntegrationException uploadFailed(Throwable cause) {
        return new CloudinaryIntegrationException(
                "No se pudo subir la imagen a Cloudinary. Intente nuevamente.",
                cause
        );
    }

    public static CloudinaryIntegrationException deleteFailed(Throwable cause) {
        return new CloudinaryIntegrationException(
                "No se pudo eliminar el recurso en Cloudinary. Intente nuevamente.",
                cause
        );
    }
}