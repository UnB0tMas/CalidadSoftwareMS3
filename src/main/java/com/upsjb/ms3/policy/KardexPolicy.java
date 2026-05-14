// ruta: src/main/java/com/upsjb/ms3/policy/KardexPolicy.java
package com.upsjb.ms3.policy;

import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class KardexPolicy {

    public boolean canViewKardex(AuthenticatedUserContext actor, boolean employeeCanViewKardex) {
        return PolicyGuard.isAdmin(actor)
                || (PolicyGuard.isEmpleado(actor) && employeeCanViewKardex);
    }

    public boolean canViewCosts(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canExportKardex(AuthenticatedUserContext actor, boolean employeeCanViewKardex) {
        return canViewKardex(actor, employeeCanViewKardex);
    }

    public void ensureCanViewKardex(AuthenticatedUserContext actor, boolean employeeCanViewKardex) {
        PolicyGuard.ensureCan(canViewKardex(actor, employeeCanViewKardex), "KARDEX_CONSULTA_DENEGADA", "consultar kardex");
    }

    public void ensureCanViewCosts(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canViewCosts(actor), "KARDEX_COSTO_DENEGADO", "consultar costos del kardex");
    }

    public void ensureCanExportKardex(AuthenticatedUserContext actor, boolean employeeCanViewKardex) {
        PolicyGuard.ensureCan(canExportKardex(actor, employeeCanViewKardex), "KARDEX_EXPORTAR_DENEGADO", "exportar kardex");
    }
}