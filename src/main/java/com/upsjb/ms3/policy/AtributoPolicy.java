// ruta: src/main/java/com/upsjb/ms3/policy/AtributoPolicy.java
package com.upsjb.ms3.policy;

import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class AtributoPolicy {

    public boolean canCreate(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canUpdate(AuthenticatedUserContext actor, boolean employeeCanUpdateAttributes) {
        return PolicyGuard.isAdmin(actor)
                || (PolicyGuard.isEmpleado(actor) && employeeCanUpdateAttributes);
    }

    public boolean canAssignToProductType(AuthenticatedUserContext actor, boolean employeeCanUpdateAttributes) {
        return canUpdate(actor, employeeCanUpdateAttributes);
    }

    public boolean canChangeState(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canViewAdmin(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor) || PolicyGuard.isEmpleado(actor);
    }

    public void ensureCanCreate(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canCreate(actor), "ATRIBUTO_CREAR_DENEGADO", "crear atributo");
    }

    public void ensureCanUpdate(AuthenticatedUserContext actor, boolean employeeCanUpdateAttributes) {
        PolicyGuard.ensureCan(canUpdate(actor, employeeCanUpdateAttributes), "ATRIBUTO_EDITAR_DENEGADO", "editar atributo");
    }

    public void ensureCanAssignToProductType(AuthenticatedUserContext actor, boolean employeeCanUpdateAttributes) {
        PolicyGuard.ensureCan(
                canAssignToProductType(actor, employeeCanUpdateAttributes),
                "ATRIBUTO_ASIGNAR_DENEGADO",
                "asociar atributo a tipo de producto"
        );
    }

    public void ensureCanChangeState(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canChangeState(actor), "ATRIBUTO_ESTADO_DENEGADO", "cambiar estado de atributo");
    }

    public void ensureCanViewAdmin(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canViewAdmin(actor), "ATRIBUTO_CONSULTA_DENEGADA", "consultar atributos administrativos");
    }
}