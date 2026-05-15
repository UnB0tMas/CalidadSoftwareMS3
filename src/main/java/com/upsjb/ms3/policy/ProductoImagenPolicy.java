// ruta: src/main/java/com/upsjb/ms3/policy/ProductoImagenPolicy.java
package com.upsjb.ms3.policy;

import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class ProductoImagenPolicy {

    public boolean canUpload(AuthenticatedUserContext actor, boolean employeeCanManageImages) {
        return PolicyGuard.isAdmin(actor)
                || (PolicyGuard.isEmpleado(actor) && employeeCanManageImages);
    }

    public boolean canUpdateMetadata(AuthenticatedUserContext actor, boolean employeeCanManageImages) {
        return canUpload(actor, employeeCanManageImages);
    }

    public boolean canSetPrincipal(AuthenticatedUserContext actor, boolean employeeCanManageImages) {
        return canUpload(actor, employeeCanManageImages);
    }

    public boolean canDeactivate(AuthenticatedUserContext actor, boolean employeeCanManageImages) {
        return canUpload(actor, employeeCanManageImages);
    }

    public boolean canViewAdmin(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor) || PolicyGuard.isEmpleado(actor);
    }

    public boolean canViewPublic() {
        return true;
    }

    public void ensureCanUpload(AuthenticatedUserContext actor, boolean employeeCanManageImages) {
        PolicyGuard.ensureCan(canUpload(actor, employeeCanManageImages), "IMAGEN_SUBIR_DENEGADO", "subir imagen de producto");
    }

    public void ensureCanUpdateMetadata(AuthenticatedUserContext actor, boolean employeeCanManageImages) {
        PolicyGuard.ensureCan(canUpdateMetadata(actor, employeeCanManageImages), "IMAGEN_EDITAR_DENEGADO", "editar metadata de imagen");
    }

    public void ensureCanSetPrincipal(AuthenticatedUserContext actor, boolean employeeCanManageImages) {
        PolicyGuard.ensureCan(canSetPrincipal(actor, employeeCanManageImages), "IMAGEN_PRINCIPAL_DENEGADO", "marcar imagen principal");
    }

    public void ensureCanDeactivate(AuthenticatedUserContext actor, boolean employeeCanManageImages) {
        PolicyGuard.ensureCan(canDeactivate(actor, employeeCanManageImages), "IMAGEN_INACTIVAR_DENEGADO", "inactivar imagen");
    }

    public void ensureCanViewAdmin(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canViewAdmin(actor), "IMAGEN_CONSULTA_DENEGADA", "consultar imágenes administrativas");
    }
}