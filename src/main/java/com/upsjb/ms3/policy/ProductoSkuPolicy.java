// ruta: src/main/java/com/upsjb/ms3/policy/ProductoSkuPolicy.java
package com.upsjb.ms3.policy;

import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class ProductoSkuPolicy {

    public boolean canCreate(AuthenticatedUserContext actor, boolean employeeCanCreateProductBasic) {
        return PolicyGuard.isAdmin(actor)
                || (PolicyGuard.isEmpleado(actor) && employeeCanCreateProductBasic);
    }

    public boolean canUpdate(AuthenticatedUserContext actor, boolean employeeCanEditProductBasic) {
        return PolicyGuard.isAdmin(actor)
                || (PolicyGuard.isEmpleado(actor) && employeeCanEditProductBasic);
    }

    public boolean canUpdateAttributes(AuthenticatedUserContext actor, boolean employeeCanUpdateAttributes) {
        return PolicyGuard.isAdmin(actor)
                || (PolicyGuard.isEmpleado(actor) && employeeCanUpdateAttributes);
    }

    public boolean canDeactivate(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canDiscontinue(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canViewAdmin(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor) || PolicyGuard.isEmpleado(actor);
    }

    public void ensureCanCreate(AuthenticatedUserContext actor, boolean employeeCanCreateProductBasic) {
        PolicyGuard.ensureCan(canCreate(actor, employeeCanCreateProductBasic), "SKU_CREAR_DENEGADO", "crear SKU");
    }

    public void ensureCanUpdate(AuthenticatedUserContext actor, boolean employeeCanEditProductBasic) {
        PolicyGuard.ensureCan(canUpdate(actor, employeeCanEditProductBasic), "SKU_EDITAR_DENEGADO", "editar SKU");
    }

    public void ensureCanUpdateAttributes(AuthenticatedUserContext actor, boolean employeeCanUpdateAttributes) {
        PolicyGuard.ensureCan(canUpdateAttributes(actor, employeeCanUpdateAttributes), "SKU_ATRIBUTOS_DENEGADO", "actualizar atributos de SKU");
    }

    public void ensureCanDeactivate(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canDeactivate(actor), "SKU_INACTIVAR_DENEGADO", "inactivar SKU");
    }

    public void ensureCanDiscontinue(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canDiscontinue(actor), "SKU_DESCONTINUAR_DENEGADO", "descontinuar SKU");
    }

    public void ensureCanViewAdmin(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canViewAdmin(actor), "SKU_CONSULTA_DENEGADA", "consultar SKU administrativos");
    }
}