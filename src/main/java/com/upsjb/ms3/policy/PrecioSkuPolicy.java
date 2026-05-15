// ruta: src/main/java/com/upsjb/ms3/policy/PrecioSkuPolicy.java
package com.upsjb.ms3.policy;

import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class PrecioSkuPolicy {

    public boolean canCreatePrice(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canViewHistory(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canViewCurrentPrice(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor) || PolicyGuard.isEmpleado(actor);
    }

    public void ensureCanCreatePrice(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canCreatePrice(actor), "PRECIO_CREAR_DENEGADO", "registrar nuevo precio de SKU");
    }

    public void ensureCanViewHistory(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canViewHistory(actor), "PRECIO_HISTORIAL_DENEGADO", "consultar historial de precios");
    }

    public void ensureCanViewCurrentPrice(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canViewCurrentPrice(actor), "PRECIO_CONSULTA_DENEGADA", "consultar precio vigente");
    }
}