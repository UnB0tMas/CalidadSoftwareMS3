// ruta: src/main/java/com/upsjb/ms3/policy/TipoProductoPolicy.java
package com.upsjb.ms3.policy;

import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class TipoProductoPolicy {

    public boolean canCreate(
            AuthenticatedUserContext actor,
            boolean employeeCanCreateProductBasic
    ) {
        return PolicyGuard.isAdmin(actor)
                || (PolicyGuard.isEmpleado(actor) && employeeCanCreateProductBasic);
    }

    public boolean canUpdate(
            AuthenticatedUserContext actor,
            boolean employeeCanEditProductBasic
    ) {
        return PolicyGuard.isAdmin(actor)
                || (PolicyGuard.isEmpleado(actor) && employeeCanEditProductBasic);
    }

    public boolean canChangeState(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canViewAdmin(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor) || PolicyGuard.isEmpleado(actor);
    }

    public void ensureCanCreate(
            AuthenticatedUserContext actor,
            boolean employeeCanCreateProductBasic
    ) {
        PolicyGuard.ensureCan(
                canCreate(actor, employeeCanCreateProductBasic),
                "TIPO_PRODUCTO_CREAR_DENEGADO",
                "crear tipo de producto"
        );
    }

    public void ensureCanUpdate(
            AuthenticatedUserContext actor,
            boolean employeeCanEditProductBasic
    ) {
        PolicyGuard.ensureCan(
                canUpdate(actor, employeeCanEditProductBasic),
                "TIPO_PRODUCTO_EDITAR_DENEGADO",
                "editar tipo de producto"
        );
    }

    public void ensureCanChangeState(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(
                canChangeState(actor),
                "TIPO_PRODUCTO_ESTADO_DENEGADO",
                "cambiar estado de tipo de producto"
        );
    }

    public void ensureCanViewAdmin(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(
                canViewAdmin(actor),
                "TIPO_PRODUCTO_CONSULTA_DENEGADA",
                "consultar tipos de producto administrativos"
        );
    }
}