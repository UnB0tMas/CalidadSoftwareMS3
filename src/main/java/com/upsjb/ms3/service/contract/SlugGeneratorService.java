// ruta: src/main/java/com/upsjb/ms3/service/contract/SlugGeneratorService.java
package com.upsjb.ms3.service.contract;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

public interface SlugGeneratorService {

    String generarSlug(String base);

    String generarSlugUnico(String base, Predicate<String> existsPredicate);

    String generarSlugUnicoExcluyendoId(
            String base,
            Long excludedId,
            BiPredicate<String, Long> existsPredicate
    );
}