// ruta: src/main/java/com/upsjb/ms3/policy/CategoriaPolicy.java
package com.upsjb.ms3.policy;

import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.shared.exception.ForbiddenException;
import org.springframework.stereotype.Component;

@Component
public class CategoriaPolicy {

    public void ensureCanCreate(
            AuthenticatedUserContext actor,
            boolean employeeCanCreateProductBasic
    ) {
        requireAuthenticated(actor);

        if (actor.isAdmin()) {
            return;
        }

        if (actor.isEmpleado() && employeeCanCreateProductBasic) {
            return;
        }

        deny();
    }

    public void ensureCanUpdate(
            AuthenticatedUserContext actor,
            boolean employeeCanEditProductBasic
    ) {
        requireAuthenticated(actor);

        if (actor.isAdmin()) {
            return;
        }

        if (actor.isEmpleado() && employeeCanEditProductBasic) {
            return;
        }

        deny();
    }

    public void ensureCanChangeState(AuthenticatedUserContext actor) {
        requireAuthenticated(actor);

        if (!actor.isAdmin()) {
            deny();
        }
    }

    public void ensureCanViewAdmin(AuthenticatedUserContext actor) {
        requireAuthenticated(actor);

        if (actor.isAdmin() || actor.isEmpleado()) {
            return;
        }

        deny();
    }

    public void ensureCanLookup(AuthenticatedUserContext actor) {
        ensureCanViewAdmin(actor);
    }

    private void requireAuthenticated(AuthenticatedUserContext actor) {
        if (actor == null) {
            throw new ForbiddenException(
                    "ACCESO_DENEGADO",
                    "Acceso denegado."
            );
        }
    }

    private void deny() {
        throw new ForbiddenException(
                "ACCESO_DENEGADO",
                "No tiene permisos para realizar esta operación."
        );
    }
}