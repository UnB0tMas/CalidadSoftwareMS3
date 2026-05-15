// ruta: src/main/java/com/upsjb/ms3/policy/StockPolicy.java
package com.upsjb.ms3.policy;

import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class StockPolicy {

    public boolean canViewInternalStock(
            AuthenticatedUserContext actor,
            boolean employeeHasInventoryPermission
    ) {
        return PolicyGuard.isAdmin(actor)
                || (PolicyGuard.isEmpleado(actor) && employeeHasInventoryPermission);
    }

    public boolean canViewPublicStock() {
        return true;
    }

    public boolean canViewCost(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canRegisterEntry(AuthenticatedUserContext actor, boolean employeeCanRegisterEntry) {
        return PolicyGuard.isAdmin(actor)
                || (PolicyGuard.isEmpleado(actor) && employeeCanRegisterEntry);
    }

    public boolean canRegisterOutput(AuthenticatedUserContext actor, boolean employeeCanRegisterOutput) {
        return PolicyGuard.isAdmin(actor)
                || (PolicyGuard.isEmpleado(actor) && employeeCanRegisterOutput);
    }

    public boolean canRegisterAdjustment(AuthenticatedUserContext actor, boolean employeeCanRegisterAdjustment) {
        return PolicyGuard.isAdmin(actor)
                || (PolicyGuard.isEmpleado(actor) && employeeCanRegisterAdjustment);
    }

    public void ensureCanViewInternalStock(
            AuthenticatedUserContext actor,
            boolean employeeHasInventoryPermission
    ) {
        PolicyGuard.ensureCan(
                canViewInternalStock(actor, employeeHasInventoryPermission),
                "STOCK_CONSULTA_INTERNA_DENEGADA",
                "consultar stock interno"
        );
    }

    public void ensureCanViewCost(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(
                canViewCost(actor),
                "STOCK_COSTO_DENEGADO",
                "consultar costos de stock"
        );
    }

    public void ensureCanRegisterEntry(AuthenticatedUserContext actor, boolean employeeCanRegisterEntry) {
        PolicyGuard.ensureCan(
                canRegisterEntry(actor, employeeCanRegisterEntry),
                "STOCK_ENTRADA_DENEGADA",
                "registrar entrada de stock"
        );
    }

    public void ensureCanRegisterOutput(AuthenticatedUserContext actor, boolean employeeCanRegisterOutput) {
        PolicyGuard.ensureCan(
                canRegisterOutput(actor, employeeCanRegisterOutput),
                "STOCK_SALIDA_DENEGADA",
                "registrar salida de stock"
        );
    }

    public void ensureCanRegisterAdjustment(AuthenticatedUserContext actor, boolean employeeCanRegisterAdjustment) {
        PolicyGuard.ensureCan(
                canRegisterAdjustment(actor, employeeCanRegisterAdjustment),
                "STOCK_AJUSTE_DENEGADO",
                "registrar ajuste de stock"
        );
    }
}