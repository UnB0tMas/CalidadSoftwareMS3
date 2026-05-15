// ruta: src/main/java/com/upsjb/ms3/policy/ProveedorPolicy.java
package com.upsjb.ms3.policy;

import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class ProveedorPolicy {

    public boolean canCreate(AuthenticatedUserContext actor, boolean employeeCanRegisterEntry) {
        return PolicyGuard.isAdmin(actor)
                || (PolicyGuard.isEmpleado(actor) && employeeCanRegisterEntry);
    }

    public boolean canUpdate(AuthenticatedUserContext actor, boolean employeeCanRegisterEntry) {
        return canCreate(actor, employeeCanRegisterEntry);
    }

    public boolean canChangeState(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canViewAdmin(AuthenticatedUserContext actor, boolean employeeHasInventoryPermission) {
        return PolicyGuard.isAdmin(actor)
                || (PolicyGuard.isEmpleado(actor) && employeeHasInventoryPermission);
    }

    public void ensureCanCreate(AuthenticatedUserContext actor, boolean employeeCanRegisterEntry) {
        PolicyGuard.ensureCan(
                canCreate(actor, employeeCanRegisterEntry),
                "PROVEEDOR_CREAR_DENEGADO",
                "registrar proveedor"
        );
    }

    public void ensureCanUpdate(AuthenticatedUserContext actor, boolean employeeCanRegisterEntry) {
        PolicyGuard.ensureCan(
                canUpdate(actor, employeeCanRegisterEntry),
                "PROVEEDOR_EDITAR_DENEGADO",
                "editar proveedor"
        );
    }

    public void ensureCanChangeState(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(
                canChangeState(actor),
                "PROVEEDOR_ESTADO_DENEGADO",
                "cambiar estado de proveedor"
        );
    }

    public void ensureCanViewAdmin(AuthenticatedUserContext actor, boolean employeeHasInventoryPermission) {
        PolicyGuard.ensureCan(
                canViewAdmin(actor, employeeHasInventoryPermission),
                "PROVEEDOR_CONSULTA_DENEGADA",
                "consultar proveedores"
        );
    }
}