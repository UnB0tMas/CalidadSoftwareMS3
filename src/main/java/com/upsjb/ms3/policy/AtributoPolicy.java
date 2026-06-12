package com.upsjb.ms3.policy;

import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import org.springframework.stereotype.Component;

@Component
public class AtributoPolicy {

    public boolean canCreate(
            AuthenticatedUserContext actor
    ) {
        return canCreate(
                actor,
                false
        );
    }

    public boolean canCreate(
            AuthenticatedUserContext actor,
            boolean employeeCanUpdateAttributes
    ) {
        return PolicyGuard.isAdmin(actor)
                || (
                PolicyGuard.isEmpleado(actor)
                        && employeeCanUpdateAttributes
        );
    }

    public boolean canUpdate(
            AuthenticatedUserContext actor,
            boolean employeeCanUpdateAttributes
    ) {
        return PolicyGuard.isAdmin(actor)
                || (
                PolicyGuard.isEmpleado(actor)
                        && employeeCanUpdateAttributes
        );
    }

    public boolean canAssignToCategory(
            AuthenticatedUserContext actor,
            boolean employeeCanUpdateAttributes
    ) {
        return canUpdate(
                actor,
                employeeCanUpdateAttributes
        );
    }

    public boolean canChangeState(
            AuthenticatedUserContext actor
    ) {
        return PolicyGuard.isAdmin(actor);
    }

    public boolean canViewAdmin(
            AuthenticatedUserContext actor
    ) {
        return PolicyGuard.isAdmin(actor)
                || PolicyGuard.isEmpleado(actor);
    }

    public boolean canViewPublic(
            AuthenticatedUserContext actor
    ) {
        return true;
    }

    public void ensureCanCreate(
            AuthenticatedUserContext actor
    ) {
        ensureCanCreate(
                actor,
                false
        );
    }

    public void ensureCanCreate(
            AuthenticatedUserContext actor,
            boolean employeeCanUpdateAttributes
    ) {
        PolicyGuard.ensureCan(
                canCreate(
                        actor,
                        employeeCanUpdateAttributes
                ),
                "ATRIBUTO_CREAR_DENEGADO",
                "crear atributo"
        );
    }

    public void ensureCanUpdate(
            AuthenticatedUserContext actor,
            boolean employeeCanUpdateAttributes
    ) {
        PolicyGuard.ensureCan(
                canUpdate(
                        actor,
                        employeeCanUpdateAttributes
                ),
                "ATRIBUTO_EDITAR_DENEGADO",
                "editar atributo"
        );
    }

    public void ensureCanAssignToCategory(
            AuthenticatedUserContext actor,
            boolean employeeCanUpdateAttributes
    ) {
        PolicyGuard.ensureCan(
                canAssignToCategory(
                        actor,
                        employeeCanUpdateAttributes
                ),
                "ATRIBUTO_ASIGNAR_DENEGADO",
                "asociar atributo a categoría"
        );
    }

    public void ensureCanChangeState(
            AuthenticatedUserContext actor
    ) {
        PolicyGuard.ensureCan(
                canChangeState(actor),
                "ATRIBUTO_ESTADO_DENEGADO",
                "cambiar estado de atributo"
        );
    }

    public void ensureCanViewAdmin(
            AuthenticatedUserContext actor
    ) {
        PolicyGuard.ensureCan(
                canViewAdmin(actor),
                "ATRIBUTO_CONSULTA_DENEGADA",
                "consultar atributos administrativos"
        );
    }

    public void ensureCanViewPublic(
            AuthenticatedUserContext actor
    ) {
        PolicyGuard.ensureCan(
                canViewPublic(actor),
                "ATRIBUTO_CONSULTA_PUBLICA_DENEGADA",
                "consultar atributos públicos"
        );
    }
}