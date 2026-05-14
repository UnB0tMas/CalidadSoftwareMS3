// ruta: src/main/java/com/upsjb/ms3/policy/ProductoPolicy.java
package com.upsjb.ms3.policy;

import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class ProductoPolicy {

    public boolean canCreate(AuthenticatedUserContext actor, boolean employeeCanCreateProductBasic) {
        return PolicyGuard.isAdmin(actor)
                || (PolicyGuard.isEmpleado(actor) && employeeCanCreateProductBasic);
    }

    public boolean canUpdate(AuthenticatedUserContext actor, boolean employeeCanEditProductBasic) {
        return PolicyGuard.isAdmin(actor)
                || (PolicyGuard.isEmpleado(actor) && employeeCanEditProductBasic);
    }

    public boolean canPublish(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canSchedulePublication(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canHide(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canChangeSaleState(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canDiscontinue(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canDeactivate(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canViewAdmin(AuthenticatedUserContext actor) {
        return PolicyGuard.isAdmin(actor) || PolicyGuard.isEmpleado(actor);
    }

    public boolean canViewPublic() {
        return true;
    }

    public void ensureCanCreate(AuthenticatedUserContext actor, boolean employeeCanCreateProductBasic) {
        PolicyGuard.ensureCan(canCreate(actor, employeeCanCreateProductBasic), "PRODUCTO_CREAR_DENEGADO", "crear producto");
    }

    public void ensureCanUpdate(AuthenticatedUserContext actor, boolean employeeCanEditProductBasic) {
        PolicyGuard.ensureCan(canUpdate(actor, employeeCanEditProductBasic), "PRODUCTO_EDITAR_DENEGADO", "editar producto");
    }

    public void ensureCanPublish(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canPublish(actor), "PRODUCTO_PUBLICAR_DENEGADO", "publicar producto");
    }

    public void ensureCanSchedulePublication(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canSchedulePublication(actor), "PRODUCTO_PROGRAMAR_DENEGADO", "programar publicación de producto");
    }

    public void ensureCanHide(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canHide(actor), "PRODUCTO_OCULTAR_DENEGADO", "ocultar producto");
    }

    public void ensureCanChangeSaleState(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canChangeSaleState(actor), "PRODUCTO_ESTADO_VENTA_DENEGADO", "cambiar estado de venta de producto");
    }

    public void ensureCanDiscontinue(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canDiscontinue(actor), "PRODUCTO_DESCONTINUAR_DENEGADO", "descontinuar producto");
    }

    public void ensureCanDeactivate(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canDeactivate(actor), "PRODUCTO_INACTIVAR_DENEGADO", "inactivar producto");
    }

    public void ensureCanViewAdmin(AuthenticatedUserContext actor) {
        PolicyGuard.ensureCan(canViewAdmin(actor), "PRODUCTO_CONSULTA_ADMIN_DENEGADA", "consultar productos administrativos");
    }
}