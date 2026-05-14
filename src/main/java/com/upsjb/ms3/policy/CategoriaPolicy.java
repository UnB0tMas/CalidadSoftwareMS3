// ruta: src/main/java/com/upsjb/ms3/policy/CategoriaPolicy.java
package com.upsjb.ms3.policy;

import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class CategoriaPolicy {

    public boolean canCreate(AuthenticatedUserContext actor, boolean employeeCanUpdateAttributes) {
        return PolicyGuard.isAdmin(actor)
                || (PolicyGuard.isEmpleado(actor) && employeeCanUpdateAttributes);
    }

    public boolean canUpdate(AuthenticatedUserContext actor, boolean employeeCanUpdateAttributes) {
        return canCreate(actor, employeeCanUpdateAttributes);
    }

    public boolean canChangeHierarchy(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canChangeState(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canViewAdmin(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor) || PolicyGuard.isEmpleado(actor);
    }

    public boolean canViewPublic() {
        return true;
    }

    public void ensureCanCreate(AuthenticatedUserContext actor, boolean employeeCanUpdateAttributes) {
        PolicyGuard.ensureCan(canCreate(actor, employeeCanUpdateAttributes), "CATEGORIA_CREAR_DENEGADO", "crear categoría");
    }

    public void ensureCanUpdate(AuthenticatedUserContext actor, boolean employeeCanUpdateAttributes) {
        PolicyGuard.ensureCan(canUpdate(actor, employeeCanUpdateAttributes), "CATEGORIA_EDITAR_DENEGADO", "editar categoría");
    }

    public void ensureCanChangeHierarchy(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canChangeHierarchy(actor), "CATEGORIA_JERARQUIA_DENEGADA", "cambiar jerarquía de categoría");
    }

    public void ensureCanChangeState(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canChangeState(actor), "CATEGORIA_ESTADO_DENEGADO", "cambiar estado de categoría");
    }

    public void ensureCanViewAdmin(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canViewAdmin(actor), "CATEGORIA_CONSULTA_DENEGADA", "consultar categorías administrativas");
    }
}