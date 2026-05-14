// ruta: src/main/java/com/upsjb/ms3/policy/ReservaStockPolicy.java
package com.upsjb.ms3.policy;

import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class ReservaStockPolicy {

    public boolean canCreateManualReservation(AuthenticatedUserContext actor, boolean employeeCanRegisterOutput) {
        return PolicyGuard.isAdmin(actor)
                || (PolicyGuard.isEmpleado(actor) && employeeCanRegisterOutput);
    }

    public boolean canConfirmReservation(AuthenticatedUserContext actor, boolean employeeCanRegisterOutput) {
        return canCreateManualReservation(actor, employeeCanRegisterOutput);
    }

    public boolean canReleaseReservation(AuthenticatedUserContext actor, boolean employeeCanRegisterOutput) {
        return canCreateManualReservation(actor, employeeCanRegisterOutput);
    }

    public boolean canExpireReservation(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canViewAdmin(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor) || PolicyGuard.isEmpleado(actor);
    }

    public void ensureCanCreateManualReservation(AuthenticatedUserContext actor, boolean employeeCanRegisterOutput) {
        PolicyGuard.ensureCan(canCreateManualReservation(actor, employeeCanRegisterOutput), "RESERVA_CREAR_DENEGADA", "crear reserva de stock");
    }

    public void ensureCanConfirmReservation(AuthenticatedUserContext actor, boolean employeeCanRegisterOutput) {
        PolicyGuard.ensureCan(canConfirmReservation(actor, employeeCanRegisterOutput), "RESERVA_CONFIRMAR_DENEGADA", "confirmar reserva de stock");
    }

    public void ensureCanReleaseReservation(AuthenticatedUserContext actor, boolean employeeCanRegisterOutput) {
        PolicyGuard.ensureCan(canReleaseReservation(actor, employeeCanRegisterOutput), "RESERVA_LIBERAR_DENEGADA", "liberar reserva de stock");
    }

    public void ensureCanExpireReservation(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canExpireReservation(actor), "RESERVA_VENCER_DENEGADA", "vencer reserva de stock");
    }

    public void ensureCanViewAdmin(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canViewAdmin(actor), "RESERVA_CONSULTA_DENEGADA", "consultar reservas de stock");
    }
}