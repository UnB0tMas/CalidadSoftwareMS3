// ruta: src/main/java/com/upsjb/ms3/policy/MarcaPolicy.java
package com.upsjb.ms3.policy;

import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class MarcaPolicy {

    public boolean canCreate(AuthenticatedUserContext actor, boolean employeeCanCreateProductBasic) {
        return PolicyGuard.isAdmin(actor)
                || (PolicyGuard.isEmpleado(actor) && employeeCanCreateProductBasic);
    }

    public boolean canUpdate(AuthenticatedUserContext actor, boolean employeeCanEditProductBasic) {
        return PolicyGuard.isAdmin(actor)
                || (PolicyGuard.isEmpleado(actor) && employeeCanEditProductBasic);
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

    public void ensureCanCreate(AuthenticatedUserContext actor, boolean employeeCanCreateProductBasic) {
        PolicyGuard.ensureCan(canCreate(actor, employeeCanCreateProductBasic), "MARCA_CREAR_DENEGADO", "crear marca");
    }

    public void ensureCanUpdate(AuthenticatedUserContext actor, boolean employeeCanEditProductBasic) {
        PolicyGuard.ensureCan(canUpdate(actor, employeeCanEditProductBasic), "MARCA_EDITAR_DENEGADO", "editar marca");
    }

    public void ensureCanChangeState(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canChangeState(actor), "MARCA_ESTADO_DENEGADO", "cambiar estado de marca");
    }

    public void ensureCanViewAdmin(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canViewAdmin(actor), "MARCA_CONSULTA_DENEGADA", "consultar marcas administrativas");
    }
}