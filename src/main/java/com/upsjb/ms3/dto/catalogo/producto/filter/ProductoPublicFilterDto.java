// ruta: src/main/java/com/upsjb/ms3/dto/catalogo/producto/filter/ProductoPublicFilterDto.java
package com.upsjb.ms3.dto.catalogo.producto.filter;

import com.upsjb.ms3.domain.enums.GeneroObjetivo;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record ProductoPublicFilterDto(
        @Size(max = 250, message = "La búsqueda no debe superar 250 caracteres.")
        String search,
        String categoriaSlug,
        String marcaSlug,
        GeneroObjetivo generoObjetivo,
        String temporada,
        String deporte,
        @DecimalMin(value = "0.00", message = "El precio mínimo no puede ser negativo.")
        BigDecimal precioMin,
        @DecimalMin(value = "0.00", message = "El precio máximo no puede ser negativo.")
        BigDecimal precioMax,
        Boolean soloVendibles,
        Boolean conPromocion,
        Boolean incluirProgramados
) {
}