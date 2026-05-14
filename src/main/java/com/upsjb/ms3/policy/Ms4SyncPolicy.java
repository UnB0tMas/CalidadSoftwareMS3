// ruta: src/main/java/com/upsjb/ms3/policy/Ms4SyncPolicy.java
package com.upsjb.ms3.policy;

import com.upsjb.ms3.config.Ms4IntegrationProperties;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.shared.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Ms4SyncPolicy {

    private final Ms4IntegrationProperties ms4Properties;

    public boolean canReceiveInternalStockEvent(String providedInternalKey) {
        return ms4Properties.isEnabled()
                && PolicyGuard.isInternalKeyValid(providedInternalKey, ms4Properties.getInternalServiceKey());
    }

    public boolean canRunManualReconciliation(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canViewSyncStatus(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public void ensureIntegrationEnabled() {
        if (!ms4Properties.isEnabled()) {
            throw new ForbiddenException(
                    "MS4_INTEGRACION_DESHABILITADA",
                    "La integración con MS4 está deshabilitada para este entorno."
            );
        }
    }

    public void ensureCanReceiveInternalStockEvent(String providedInternalKey) {
        ensureIntegrationEnabled();

        if (!canReceiveInternalStockEvent(providedInternalKey)) {
            throw new ForbiddenException(
                    "MS4_INTERNAL_KEY_INVALIDA",
                    "MS4 no está autorizado para ejecutar este flujo interno de stock."
            );
        }
    }

    public void ensureCanRunManualReconciliation(AuthenticatedUserContext actor) {
        ensureIntegrationEnabled();
        PolicyGuard.ensureCan(canRunManualReconciliation(actor), "MS4_RECONCILIACION_DENEGADA", "ejecutar reconciliación manual con MS4");
    }

    public void ensureCanViewSyncStatus(AuthenticatedUserContext actor) {
        ensureIntegrationEnabled();
        PolicyGuard.ensureCan(canViewSyncStatus(actor), "MS4_SYNC_CONSULTA_DENEGADA", "consultar estado de sincronización con MS4");
    }
}