// ruta: src/main/java/com/upsjb/ms3/policy/EmpleadoInventarioPermisoPolicy.java
package com.upsjb.ms3.policy;

import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class EmpleadoInventarioPermisoPolicy {

    public boolean canGrantPermissions(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canRevokePermissions(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canViewPermissions(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canUpsertEmployeeSnapshot(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public void ensureCanGrantPermissions(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canGrantPermissions(actor), "PERMISO_INVENTARIO_OTORGAR_DENEGADO", "otorgar permisos de inventario");
    }

    public void ensureCanRevokePermissions(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canRevokePermissions(actor), "PERMISO_INVENTARIO_REVOCAR_DENEGADO", "revocar permisos de inventario");
    }

    public void ensureCanViewPermissions(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canViewPermissions(actor), "PERMISO_INVENTARIO_CONSULTA_DENEGADA", "consultar permisos de inventario");
    }

    public void ensureCanUpsertEmployeeSnapshot(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canUpsertEmployeeSnapshot(actor), "EMPLEADO_SNAPSHOT_UPSERT_DENEGADO", "registrar snapshot de empleado MS2");
    }

    public void ensureNotSelfGrant(AuthenticatedUserContext actor, Long targetIdUsuarioMs1) {
        if (actor != null
                && actor.getIdUsuarioMs1() != null
                && targetIdUsuarioMs1 != null
                && actor.getIdUsuarioMs1().equals(targetIdUsuarioMs1)) {
            PolicyGuard.ensureCan(false, "PERMISO_AUTOGESTION_DENEGADA", "otorgarse permisos a sí mismo");
        }
    }
}