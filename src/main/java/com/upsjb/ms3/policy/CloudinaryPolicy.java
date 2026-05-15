// ruta: src/main/java/com/upsjb/ms3/policy/CloudinaryPolicy.java
package com.upsjb.ms3.policy;

import com.upsjb.ms3.config.CloudinaryProperties;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.shared.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CloudinaryPolicy {

    private final CloudinaryProperties cloudinaryProperties;

    public boolean canUploadImage(AuthenticatedUserContext actor, boolean employeeCanManageImages) {
        return PolicyGuard.isAdmin(actor)
                || (PolicyGuard.isEmpleado(actor) && employeeCanManageImages);
    }

    public boolean canDeleteRemoteResource(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canInvalidateResource(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public void ensureCloudinaryEnabled() {
        if (!cloudinaryProperties.isEnabled()) {
            throw new ForbiddenException(
                    "CLOUDINARY_DESHABILITADO",
                    "La integración con Cloudinary está deshabilitada para este entorno."
            );
        }
    }

    public void ensureCanUploadImage(AuthenticatedUserContext actor, boolean employeeCanManageImages) {
        ensureCloudinaryEnabled();
        PolicyGuard.ensureCan(canUploadImage(actor, employeeCanManageImages), "CLOUDINARY_UPLOAD_DENEGADO", "subir imagen a Cloudinary");
    }

    public void ensureCanDeleteRemoteResource(AuthenticatedUserContext actor) {
        ensureCloudinaryEnabled();
        PolicyGuard.ensureCan(canDeleteRemoteResource(actor), "CLOUDINARY_DELETE_DENEGADO", "eliminar recurso remoto de Cloudinary");
    }

    public void ensureCanInvalidateResource(AuthenticatedUserContext actor) {
        ensureCloudinaryEnabled();
        PolicyGuard.ensureCan(canInvalidateResource(actor), "CLOUDINARY_INVALIDATE_DENEGADO", "invalidar recurso Cloudinary");
    }
}