// ruta: src/main/java/com/upsjb/ms3/policy/MovimientoInventarioPolicy.java
package com.upsjb.ms3.policy;

import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class MovimientoInventarioPolicy {

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

    public boolean canRegisterCompensatoryMovement(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canViewMovements(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor) || PolicyGuard.isEmpleado(actor);
    }

    public boolean canViewCosts(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public void ensureCanRegisterEntry(AuthenticatedUserContext actor, boolean employeeCanRegisterEntry) {
        PolicyGuard.ensureCan(canRegisterEntry(actor, employeeCanRegisterEntry), "MOVIMIENTO_ENTRADA_DENEGADO", "registrar entrada de inventario");
    }

    public void ensureCanRegisterOutput(AuthenticatedUserContext actor, boolean employeeCanRegisterOutput) {
        PolicyGuard.ensureCan(canRegisterOutput(actor, employeeCanRegisterOutput), "MOVIMIENTO_SALIDA_DENEGADO", "registrar salida de inventario");
    }

    public void ensureCanRegisterAdjustment(AuthenticatedUserContext actor, boolean employeeCanRegisterAdjustment) {
        PolicyGuard.ensureCan(canRegisterAdjustment(actor, employeeCanRegisterAdjustment), "MOVIMIENTO_AJUSTE_DENEGADO", "registrar ajuste de inventario");
    }

    public void ensureCanRegisterCompensatoryMovement(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canRegisterCompensatoryMovement(actor), "MOVIMIENTO_COMPENSATORIO_DENEGADO", "registrar movimiento compensatorio");
    }

    public void ensureCanViewMovements(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canViewMovements(actor), "MOVIMIENTO_CONSULTA_DENEGADA", "consultar movimientos de inventario");
    }

    public void ensureCanViewCosts(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canViewCosts(actor), "MOVIMIENTO_COSTO_DENEGADO", "consultar costos del movimiento");
    }
}