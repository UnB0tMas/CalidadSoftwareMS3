// ruta: src/main/java/com/upsjb/ms3/policy/AuditoriaPolicy.java
package com.upsjb.ms3.policy;

import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class AuditoriaPolicy {

    public boolean canViewAudit(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canViewAuditDetail(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canExportAudit(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public void ensureCanViewAudit(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canViewAudit(actor), "AUDITORIA_CONSULTA_DENEGADA", "consultar auditoría funcional");
    }

    public void ensureCanViewAuditDetail(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canViewAuditDetail(actor), "AUDITORIA_DETALLE_DENEGADO", "consultar detalle de auditoría");
    }

    public void ensureCanExportAudit(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canExportAudit(actor), "AUDITORIA_EXPORTAR_DENEGADO", "exportar auditoría");
    }
}