package com.upsjb.ms3.shared.reference;

import com.upsjb.ms3.domain.entity.EmpleadoSnapshotMs2;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class EmpleadoInventarioReferenceResolver {

    private final EntityDisplayResolver displayResolver;
    private final ReferenceOptionMapper optionMapper;

    public EmpleadoSnapshotMs2 resolve(
            Long idEmpleadoSnapshot,
            Long idEmpleadoMs2,
            Long idUsuarioMs1,
            String codigoEmpleado
    ) {
        return displayResolver.resolveRequired(
                EmpleadoSnapshotMs2.class,
                "Empleado inventario",
                "idEmpleadoSnapshot",
                idEmpleadoSnapshot,
                references(idEmpleadoMs2, idUsuarioMs1, codigoEmpleado)
        );
    }

    public List<ReferenceOptionMapper.ReferenceOption> search(String search, Integer limit) {
        return displayResolver.searchActive(
                        EmpleadoSnapshotMs2.class,
                        List.of("codigoEmpleado", "nombresCompletos", "areaCodigo", "areaNombre"),
                        search,
                        limit
                )
                .stream()
                .map(this::toOption)
                .toList();
    }

    public ReferenceOptionMapper.ReferenceOption toOption(EmpleadoSnapshotMs2 entity) {
        return optionMapper.toOption(
                displayResolver.resolveId(entity).orElse(null),
                displayResolver.value(entity),
                displayResolver.display(entity),
                buildDescription(entity),
                displayResolver.isActive(entity),
                displayResolver.metadata(entity,
                        "idEmpleadoMs2",
                        "idUsuarioMs1",
                        "codigoEmpleado",
                        "areaCodigo",
                        "areaNombre",
                        "empleadoActivo",
                        "snapshotVersion",
                        "snapshotAt"
                )
        );
    }

    private String buildDescription(EmpleadoSnapshotMs2 empleado) {
        if (empleado == null) {
            return null;
        }

        if (StringUtils.hasText(empleado.getAreaNombre())) {
            return empleado.getAreaNombre();
        }

        return empleado.getAreaCodigo();
    }

    private Map<String, Object> references(Long idEmpleadoMs2, Long idUsuarioMs1, String codigoEmpleado) {
        Map<String, Object> references = new LinkedHashMap<>();

        if (idEmpleadoMs2 != null) {
            references.put("idEmpleadoMs2", idEmpleadoMs2);
        }

        if (idUsuarioMs1 != null) {
            references.put("idUsuarioMs1", idUsuarioMs1);
        }

        if (StringUtils.hasText(codigoEmpleado)) {
            references.put("codigoEmpleado", codigoEmpleado.trim());
        }

        return references;
    }
}