package com.upsjb.ms3.shared.reference;

import com.upsjb.ms3.domain.entity.Proveedor;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class ProveedorReferenceResolver {

    private final EntityDisplayResolver displayResolver;
    private final ReferenceOptionMapper optionMapper;

    public Proveedor resolve(Long idProveedor, String ruc, String numeroDocumento, String nombre) {
        Proveedor byDirectReference = displayResolver.resolveOptional(
                        Proveedor.class,
                        "idProveedor",
                        idProveedor,
                        references(ruc, numeroDocumento)
                )
                .orElse(null);

        if (byDirectReference != null) {
            return byDirectReference;
        }

        if (StringUtils.hasText(nombre)) {
            return displayResolver.searchActive(
                            Proveedor.class,
                            List.of("razonSocial", "nombreComercial", "nombres", "apellidos"),
                            nombre,
                            1
                    )
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> com.upsjb.ms3.shared.exception.NotFoundException.activeResource("Proveedor", nombre));
        }

        throw com.upsjb.ms3.shared.exception.NotFoundException.activeResource("Proveedor", idProveedor);
    }

    public List<ReferenceOptionMapper.ReferenceOption> search(String search, Integer limit) {
        return displayResolver.searchActive(
                        Proveedor.class,
                        List.of("ruc", "numeroDocumento", "razonSocial", "nombreComercial", "nombres", "apellidos"),
                        search,
                        limit
                )
                .stream()
                .map(this::toOption)
                .toList();
    }

    public ReferenceOptionMapper.ReferenceOption toOption(Proveedor entity) {
        return optionMapper.toOption(
                displayResolver.resolveId(entity).orElse(null),
                displayResolver.value(entity),
                displayResolver.display(entity),
                buildDescription(entity),
                displayResolver.isActive(entity),
                displayResolver.metadata(entity, "tipoProveedor", "tipoDocumento", "numeroDocumento", "ruc", "correo", "telefono")
        );
    }

    private String buildDescription(Proveedor proveedor) {
        if (proveedor == null) {
            return null;
        }

        if (StringUtils.hasText(proveedor.getRuc())) {
            return "RUC: " + proveedor.getRuc();
        }

        if (StringUtils.hasText(proveedor.getNumeroDocumento())) {
            return "Documento: " + proveedor.getNumeroDocumento();
        }

        return null;
    }

    private Map<String, Object> references(String ruc, String numeroDocumento) {
        Map<String, Object> references = new LinkedHashMap<>();

        if (StringUtils.hasText(ruc)) {
            references.put("ruc", ruc.trim());
        }

        if (StringUtils.hasText(numeroDocumento)) {
            references.put("numeroDocumento", numeroDocumento.trim());
        }

        return references;
    }
}