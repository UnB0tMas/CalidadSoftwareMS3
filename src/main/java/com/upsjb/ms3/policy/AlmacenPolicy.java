// ruta: src/main/java/com/upsjb/ms3/policy/AlmacenPolicy.java
package com.upsjb.ms3.policy;

import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class AlmacenPolicy {

    public boolean canCreate(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canUpdate(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canChangeState(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canSetPrincipal(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canViewAdmin(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor) || PolicyGuard.isEmpleado(actor);
    }

    public void ensureCanCreate(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canCreate(actor), "ALMACEN_CREAR_DENEGADO", "crear almacén");
    }

    public void ensureCanUpdate(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canUpdate(actor), "ALMACEN_EDITAR_DENEGADO", "editar almacén");
    }

    public void ensureCanChangeState(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canChangeState(actor), "ALMACEN_ESTADO_DENEGADO", "cambiar estado de almacén");
    }

    public void ensureCanSetPrincipal(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canSetPrincipal(actor), "ALMACEN_PRINCIPAL_DENEGADO", "marcar almacén principal");
    }

    public void ensureCanViewAdmin(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canViewAdmin(actor), "ALMACEN_CONSULTA_DENEGADA", "consultar almacenes");
    }
}