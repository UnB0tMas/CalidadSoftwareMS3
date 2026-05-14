// ruta: src/main/java/com/upsjb/ms3/policy/CatalogoLookupPolicy.java
package com.upsjb.ms3.policy;

import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.shared.exception.ForbiddenException;
import org.springframework.stereotype.Component;

@Component
public class CatalogoLookupPolicy {

    public void ensureCanLookup(AuthenticatedUserContext actor) {
        if (actor == null) {
            deny();
        }

        if (actor.isAdmin() || actor.isEmpleado()) {
            return;
        }

        deny();
    }

    private void deny() {
        throw new ForbiddenException(
                "ACCESO_DENEGADO",
                "No tiene permisos para consultar datos de referencia del catálogo."
        );
    }
}