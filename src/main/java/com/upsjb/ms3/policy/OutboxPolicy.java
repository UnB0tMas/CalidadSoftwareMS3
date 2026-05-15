// ruta: src/main/java/com/upsjb/ms3/policy/OutboxPolicy.java
package com.upsjb.ms3.policy;

import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class OutboxPolicy {

    public boolean canViewOutbox(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canViewPayload(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canRetryOutbox(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canForcePublish(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public void ensureCanViewOutbox(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canViewOutbox(actor), "OUTBOX_CONSULTA_DENEGADA", "consultar eventos outbox");
    }

    public void ensureCanViewPayload(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canViewPayload(actor), "OUTBOX_PAYLOAD_DENEGADO", "consultar payload outbox");
    }

    public void ensureCanRetryOutbox(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canRetryOutbox(actor), "OUTBOX_REINTENTO_DENEGADO", "reintentar evento outbox");
    }

    public void ensureCanForcePublish(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canForcePublish(actor), "OUTBOX_PUBLICACION_MANUAL_DENEGADA", "forzar publicación de evento outbox");
    }
}