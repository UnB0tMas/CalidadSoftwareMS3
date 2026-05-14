package com.upsjb.ms3.shared.reference;

import com.upsjb.ms3.domain.entity.ProductoSku;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class ProductoSkuReferenceResolver {

    private final EntityDisplayResolver displayResolver;
    private final ReferenceOptionMapper optionMapper;

    public ProductoSku resolve(Long idSku, String codigoSku, String barcode) {
        return displayResolver.resolveRequired(
                ProductoSku.class,
                "SKU",
                "idSku",
                idSku,
                references(codigoSku, barcode)
        );
    }

    public List<ReferenceOptionMapper.ReferenceOption> search(String search, Integer limit) {
        return displayResolver.searchActive(
                        ProductoSku.class,
                        List.of("codigoSku", "barcode", "color", "talla", "modelo"),
                        search,
                        limit
                )
                .stream()
                .map(this::toOption)
                .toList();
    }

    public ReferenceOptionMapper.ReferenceOption toOption(ProductoSku entity) {
        return optionMapper.toOption(
                displayResolver.resolveId(entity).orElse(null),
                displayResolver.value(entity),
                displayResolver.display(entity),
                buildDescription(entity),
                displayResolver.isActive(entity),
                displayResolver.metadata(entity, "codigoSku", "barcode", "color", "talla", "modelo", "estadoSku")
        );
    }

    private String buildDescription(ProductoSku sku) {
        if (sku == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();

        append(builder, sku.getColor());
        append(builder, sku.getTalla());
        append(builder, sku.getModelo());

        return builder.isEmpty() ? null : builder.toString();
    }

    private void append(StringBuilder builder, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }

        if (!builder.isEmpty()) {
            builder.append(" - ");
        }

        builder.append(value.trim());
    }

    private Map<String, Object> references(String codigoSku, String barcode) {
        Map<String, Object> references = new LinkedHashMap<>();

        if (StringUtils.hasText(codigoSku)) {
            references.put("codigoSku", codigoSku.trim());
        }

        if (StringUtils.hasText(barcode)) {
            references.put("barcode", barcode.trim());
        }

        return references;
    }
}