// ruta: src/main/java/com/upsjb/ms3/policy/CompraInventarioPolicy.java
package com.upsjb.ms3.policy;

import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class CompraInventarioPolicy {

    public boolean canCreateDraft(AuthenticatedUserContext actor, boolean employeeCanRegisterEntry) {
        return PolicyGuard.isAdmin(actor)
                || (PolicyGuard.isEmpleado(actor) && employeeCanRegisterEntry);
    }

    public boolean canUpdateDraft(AuthenticatedUserContext actor, boolean employeeCanRegisterEntry) {
        return canCreateDraft(actor, employeeCanRegisterEntry);
    }

    public boolean canConfirm(AuthenticatedUserContext actor, boolean employeeCanRegisterEntry) {
        return canCreateDraft(actor, employeeCanRegisterEntry);
    }

    public boolean canAnnul(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canViewAdmin(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor) || PolicyGuard.isEmpleado(actor);
    }

    public boolean canViewCosts(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public void ensureCanCreateDraft(AuthenticatedUserContext actor, boolean employeeCanRegisterEntry) {
        PolicyGuard.ensureCan(canCreateDraft(actor, employeeCanRegisterEntry), "COMPRA_CREAR_DENEGADA", "crear compra de inventario");
    }

    public void ensureCanUpdateDraft(AuthenticatedUserContext actor, boolean employeeCanRegisterEntry) {
        PolicyGuard.ensureCan(canUpdateDraft(actor, employeeCanRegisterEntry), "COMPRA_EDITAR_DENEGADA", "editar compra de inventario");
    }

    public void ensureCanConfirm(AuthenticatedUserContext actor, boolean employeeCanRegisterEntry) {
        PolicyGuard.ensureCan(canConfirm(actor, employeeCanRegisterEntry), "COMPRA_CONFIRMAR_DENEGADA", "confirmar compra de inventario");
    }

    public void ensureCanAnnul(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canAnnul(actor), "COMPRA_ANULAR_DENEGADA", "anular compra de inventario");
    }

    public void ensureCanViewAdmin(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canViewAdmin(actor), "COMPRA_CONSULTA_DENEGADA", "consultar compras de inventario");
    }

    public void ensureCanViewCosts(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canViewCosts(actor), "COMPRA_COSTO_DENEGADO", "consultar costos de compra");
    }
}