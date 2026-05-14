// ruta: src/main/java/com/upsjb/ms3/policy/TipoProductoPolicy.java
package com.upsjb.ms3.policy;

import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class TipoProductoPolicy {

    public boolean canCreate(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canUpdate(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canChangeState(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canViewAdmin(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor) || PolicyGuard.isEmpleado(actor);
    }

    public void ensureCanCreate(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canCreate(actor), "TIPO_PRODUCTO_CREAR_DENEGADO", "crear tipo de producto");
    }

    public void ensureCanUpdate(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canUpdate(actor), "TIPO_PRODUCTO_EDITAR_DENEGADO", "editar tipo de producto");
    }

    public void ensureCanChangeState(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canChangeState(actor), "TIPO_PRODUCTO_ESTADO_DENEGADO", "cambiar estado de tipo de producto");
    }

    public void ensureCanViewAdmin(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canViewAdmin(actor), "TIPO_PRODUCTO_CONSULTA_DENEGADA", "consultar tipos de producto administrativos");
    }
}