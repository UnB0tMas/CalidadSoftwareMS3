// ruta: src/main/java/com/upsjb/ms3/service/impl/SlugGeneratorServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.service.contract.SlugGeneratorService;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.util.StringNormalizer;
import java.util.Locale;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SlugGeneratorServiceImpl implements SlugGeneratorService {

    private static final int MAX_SLUG_LENGTH = 150;
    private static final int MAX_ATTEMPTS = 500;

    @Override
    public String generarSlug(String base) {
        if (!StringUtils.hasText(base)) {
            throw new ValidationException(
                    "SLUG_BASE_REQUERIDA",
                    "El texto base para generar slug es obligatorio."
            );
        }

        String slug = StringNormalizer.removeAccents(base)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+", "")
                .replaceAll("-+$", "");

        if (!StringUtils.hasText(slug)) {
            slug = "registro";
        }

        return truncateSlug(slug);
    }

    @Override
    public String generarSlugUnico(String base, Predicate<String> existsPredicate) {
        if (existsPredicate == null) {
            throw new ValidationException(
                    "SLUG_VALIDATOR_REQUERIDO",
                    "Debe indicar la validación de unicidad del slug."
            );
        }

        String baseSlug = generarSlug(base);

        if (!existsPredicate.test(baseSlug)) {
            return baseSlug;
        }

        for (int i = 2; i <= MAX_ATTEMPTS; i++) {
            String candidate = appendSuffix(baseSlug, i);

            if (!existsPredicate.test(candidate)) {
                return candidate;
            }
        }

        throw new ValidationException(
                "SLUG_NO_DISPONIBLE",
                "No se pudo generar un slug único para el registro solicitado."
        );
    }

    @Override
    public String generarSlugUnicoExcluyendoId(
            String base,
            Long excludedId,
            BiPredicate<String, Long> existsPredicate
    ) {
        if (existsPredicate == null) {
            throw new ValidationException(
                    "SLUG_VALIDATOR_REQUERIDO",
                    "Debe indicar la validación de unicidad del slug."
            );
        }

        String baseSlug = generarSlug(base);

        if (!existsPredicate.test(baseSlug, excludedId)) {
            return baseSlug;
        }

        for (int i = 2; i <= MAX_ATTEMPTS; i++) {
            String candidate = appendSuffix(baseSlug, i);

            if (!existsPredicate.test(candidate, excludedId)) {
                return candidate;
            }
        }

        throw new ValidationException(
                "SLUG_NO_DISPONIBLE",
                "No se pudo generar un slug único para el registro solicitado."
        );
    }

    private String appendSuffix(String baseSlug, int suffix) {
        String suffixText = "-" + suffix;
        int maxBaseLength = MAX_SLUG_LENGTH - suffixText.length();

        String normalizedBase = baseSlug.length() <= maxBaseLength
                ? baseSlug
                : baseSlug.substring(0, maxBaseLength).replaceAll("-+$", "");

        return normalizedBase + suffixText;
    }

    private String truncateSlug(String slug) {
        if (slug.length() <= MAX_SLUG_LENGTH) {
            return slug;
        }

        return slug.substring(0, MAX_SLUG_LENGTH).replaceAll("-+$", "");
    }
}