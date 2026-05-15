// ruta: src/main/java/com/upsjb/ms3/policy/PromocionPolicy.java
package com.upsjb.ms3.policy;

import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class PromocionPolicy {

    public boolean canCreate(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canUpdate(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canCreateVersion(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canChangeVersionState(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canManageSkuDiscounts(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canCancel(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canViewAdmin(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor) || PolicyGuard.isEmpleado(actor);
    }

    public boolean canViewPublic() {
        return true;
    }

    public void ensureCanCreate(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canCreate(actor), "PROMOCION_CREAR_DENEGADO", "crear promoción");
    }

    public void ensureCanUpdate(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canUpdate(actor), "PROMOCION_EDITAR_DENEGADO", "editar promoción");
    }

    public void ensureCanCreateVersion(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canCreateVersion(actor), "PROMOCION_VERSIONAR_DENEGADO", "crear versión de promoción");
    }

    public void ensureCanChangeVersionState(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canChangeVersionState(actor), "PROMOCION_ESTADO_DENEGADO", "cambiar estado de promoción");
    }

    public void ensureCanManageSkuDiscounts(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canManageSkuDiscounts(actor), "PROMOCION_DESCUENTO_DENEGADO", "gestionar descuentos por SKU");
    }

    public void ensureCanCancel(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canCancel(actor), "PROMOCION_CANCELAR_DENEGADO", "cancelar promoción");
    }

    public void ensureCanViewAdmin(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canViewAdmin(actor), "PROMOCION_CONSULTA_ADMIN_DENEGADA", "consultar promociones administrativas");
    }
}