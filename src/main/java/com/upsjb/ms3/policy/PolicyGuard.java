// ruta: src/main/java/com/upsjb/ms3/policy/PolicyGuard.java
package com.upsjb.ms3.policy;

import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.shared.exception.ForbiddenException;

final class PolicyGuard {

    private PolicyGuard() {
    }

    static boolean isAuthenticated(AuthenticatedUserContext actor) {
        return actor != null && actor.getIdUsuarioMs1() != null;
    }

    static boolean isAdmin(AuthenticatedUserContext actor) {
        return actor != null && actor.isAdmin();
    }

    static boolean isEmpleado(AuthenticatedUserContext actor) {
        return actor != null && actor.isEmpleado();
    }

    static boolean isCliente(AuthenticatedUserContext actor) {
        return actor != null && actor.isCliente();
    }

    static void ensureAuthenticated(AuthenticatedUserContext actor, String operation) {
        if (!isAuthenticated(actor)) {
            throw new ForbiddenException(
                    "ACTOR_NO_AUTENTICADO",
                    "Debe autenticarse para ejecutar la operación: " + operation + "."
            );
        }
    }

    static void ensureCan(boolean allowed, String code, String operation) {
        if (!allowed) {
            throw new ForbiddenException(
                    code,
                    "No tiene permisos suficientes para ejecutar la operación: " + operation + "."
            );
        }
    }

    static void ensureAdmin(AuthenticatedUserContext actor, String operation) {
        ensureAuthenticated(actor, operation);
        ensureCan(isAdmin(actor), "SOLO_ADMIN", operation);
    }

    static boolean isInternalKeyValid(String providedKey, String expectedKey) {
        return hasText(providedKey)
                && hasText(expectedKey)
                && providedKey.trim().equals(expectedKey.trim());
    }

    static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}