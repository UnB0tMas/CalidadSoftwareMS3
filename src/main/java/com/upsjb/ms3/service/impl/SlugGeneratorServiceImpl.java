// ruta: src/main/java/com/upsjb/ms3/service/impl/SlugGeneratorServiceImpl.java
package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.service.contract.SlugGeneratorService;
import com.upsjb.ms3.shared.exception.ConflictException;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.util.SlugUtil;
import com.upsjb.ms3.util.StringNormalizer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;

@Service
public class SlugGeneratorServiceImpl implements SlugGeneratorService {

    private static final int MAX_SLUG_LENGTH = 150;
    private static final int MAX_ATTEMPTS = 500;
    private static final String DEFAULT_FALLBACK = "item";

    @Override
    public String generarSlug(String base) {
        validateBase(base);

        String slug = SlugUtil.toSlug(base, MAX_SLUG_LENGTH);

        if (!StringNormalizer.hasText(slug) || !SlugUtil.isValidSlug(slug)) {
            return DEFAULT_FALLBACK;
        }

        return slug;
    }

    @Override
    public String generarSlugUnico(String base, Predicate<String> existsPredicate) {
        validateBase(base);
        validatePredicate(existsPredicate);

        String baseSlug = generarSlug(base);

        if (!existsPredicate.test(baseSlug)) {
            return baseSlug;
        }

        for (int counter = 2; counter <= MAX_ATTEMPTS; counter++) {
            String candidate = appendSuffix(baseSlug, counter);

            if (!existsPredicate.test(candidate)) {
                return candidate;
            }
        }

        throw new ConflictException(
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
        validateBase(base);
        validateBiPredicate(existsPredicate);

        String baseSlug = generarSlug(base);

        if (!existsPredicate.test(baseSlug, excludedId)) {
            return baseSlug;
        }

        for (int counter = 2; counter <= MAX_ATTEMPTS; counter++) {
            String candidate = appendSuffix(baseSlug, counter);

            if (!existsPredicate.test(candidate, excludedId)) {
                return candidate;
            }
        }

        throw new ConflictException(
                "SLUG_NO_DISPONIBLE",
                "No se pudo generar un slug único para el registro solicitado."
        );
    }

    private void validateBase(String base) {
        if (!StringNormalizer.hasText(base)) {
            throw new ValidationException(
                    "SLUG_BASE_REQUERIDA",
                    "El texto base para generar slug es obligatorio."
            );
        }
    }

    private void validatePredicate(Predicate<String> existsPredicate) {
        if (existsPredicate == null) {
            throw new ValidationException(
                    "SLUG_VALIDATOR_REQUERIDO",
                    "Debe indicar la validación de unicidad del slug."
            );
        }
    }

    private void validateBiPredicate(BiPredicate<String, Long> existsPredicate) {
        if (existsPredicate == null) {
            throw new ValidationException(
                    "SLUG_VALIDATOR_REQUERIDO",
                    "Debe indicar la validación de unicidad del slug."
            );
        }
    }

    private String appendSuffix(String baseSlug, int suffix) {
        String suffixText = "-" + suffix;
        int maxBaseLength = MAX_SLUG_LENGTH - suffixText.length();

        String normalizedBase = baseSlug.length() <= maxBaseLength
                ? baseSlug
                : baseSlug.substring(0, maxBaseLength).replaceAll("-+$", "");

        if (!StringNormalizer.hasText(normalizedBase)) {
            normalizedBase = DEFAULT_FALLBACK;
        }

        return normalizedBase + suffixText;
    }
}