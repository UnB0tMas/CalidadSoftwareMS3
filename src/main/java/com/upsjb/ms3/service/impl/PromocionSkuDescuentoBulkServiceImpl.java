package com.upsjb.ms3.service.impl;

import com.upsjb.ms3.dto.promocion.request.PromocionSkuDescuentoBulkCreateRequestDto;
import com.upsjb.ms3.dto.promocion.request.PromocionSkuDescuentoCreateRequestDto;
import com.upsjb.ms3.dto.promocion.response.PromocionSkuDescuentoCalculoResponseDto;
import com.upsjb.ms3.dto.promocion.response.PromocionSkuDescuentoResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.EntityReferenceDto;
import com.upsjb.ms3.service.contract.PromocionSkuDescuentoBulkService;
import com.upsjb.ms3.service.contract.PromocionSkuDescuentoService;
import com.upsjb.ms3.shared.exception.ValidationException;
import com.upsjb.ms3.shared.response.ApiResponseFactory;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PromocionSkuDescuentoBulkServiceImpl
        implements PromocionSkuDescuentoBulkService {

    private final PromocionSkuDescuentoService
            promocionSkuDescuentoService;

    private final ApiResponseFactory
            apiResponseFactory;

    @Override
    @Transactional
    public ApiResponseDto<
            List<
                    PromocionSkuDescuentoResponseDto
                    >
            > agregar(
            Long idPromocionVersion,
            PromocionSkuDescuentoBulkCreateRequestDto request
    ) {
        validateRequest(
                idPromocionVersion,
                request
        );

        List<
                PromocionSkuDescuentoCreateRequestDto
                > descuentos =
                List.copyOf(
                        request.descuentos()
                );

        validateNoRepeatedSkus(
                descuentos
        );

        validateAllCalculations(
                idPromocionVersion,
                descuentos
        );

        List<
                PromocionSkuDescuentoResponseDto
                > created =
                new ArrayList<>(
                        descuentos.size()
                );

        for (
                PromocionSkuDescuentoCreateRequestDto descuento
                : descuentos
        ) {
            ApiResponseDto<
                    PromocionSkuDescuentoResponseDto
                    > response =
                    promocionSkuDescuentoService.agregar(
                            idPromocionVersion,
                            descuento
                    );

            PromocionSkuDescuentoResponseDto data =
                    response == null
                            ? null
                            : response.data();

            if (data == null) {
                throw new ValidationException(
                        "PROMOCION_DESCUENTO_MASIVO_SIN_RESPUESTA",
                        "No se pudo completar el registro masivo de descuentos."
                );
            }

            created.add(
                    data
            );
        }

        return apiResponseFactory.dtoCreated(
                created.size() == 1
                        ? "La variante fue agregada correctamente."
                        : created.size()
                        + " variantes fueron agregadas correctamente.",
                List.copyOf(
                        created
                )
        );
    }

    private void validateRequest(
            Long idPromocionVersion,
            PromocionSkuDescuentoBulkCreateRequestDto request
    ) {
        if (
                idPromocionVersion == null
                        || idPromocionVersion <= 0
        ) {
            throw new ValidationException(
                    "PROMOCION_VERSION_ID_REQUERIDO",
                    "Debe indicar una versión de promoción válida."
            );
        }

        if (
                request == null
                        || request.descuentos() == null
                        || request.descuentos().isEmpty()
        ) {
            throw new ValidationException(
                    "PROMOCION_DESCUENTOS_REQUERIDOS",
                    "Debe seleccionar al menos una variante."
            );
        }

        if (
                request.descuentos().size() > 500
        ) {
            throw new ValidationException(
                    "PROMOCION_DESCUENTOS_LIMITE_EXCEDIDO",
                    "No se pueden registrar más de 500 variantes en una sola operación."
            );
        }

        if (
                request.descuentos()
                        .stream()
                        .anyMatch(
                                item -> item == null
                        )
        ) {
            throw new ValidationException(
                    "PROMOCION_DESCUENTO_INVALIDO",
                    "La selección contiene un descuento inválido."
            );
        }
    }

    private void validateNoRepeatedSkus(
            List<
                    PromocionSkuDescuentoCreateRequestDto
                    > descuentos
    ) {
        Set<String> skuKeys =
                new LinkedHashSet<>();

        for (
                PromocionSkuDescuentoCreateRequestDto descuento
                : descuentos
        ) {
            String key =
                    skuKey(
                            descuento.sku()
                    );

            if (key == null) {
                throw new ValidationException(
                        "SKU_REFERENCIA_REQUERIDA",
                        "Todas las variantes deben tener una referencia de SKU válida."
                );
            }

            if (
                    !skuKeys.add(
                            key
                    )
            ) {
                throw new ValidationException(
                        "PROMOCION_SKU_REPETIDO_EN_SOLICITUD",
                        "Una variante no puede aparecer más de una vez en la misma operación."
                );
            }
        }
    }

    private void validateAllCalculations(
            Long idPromocionVersion,
            List<
                    PromocionSkuDescuentoCreateRequestDto
                    > descuentos
    ) {
        for (
                PromocionSkuDescuentoCreateRequestDto descuento
                : descuentos
        ) {
            ApiResponseDto<
                    PromocionSkuDescuentoCalculoResponseDto
                    > response =
                    promocionSkuDescuentoService.calcular(
                            idPromocionVersion,
                            descuento
                    );

            PromocionSkuDescuentoCalculoResponseDto calculation =
                    response == null
                            ? null
                            : response.data();

            if (calculation == null) {
                throw new ValidationException(
                        "PROMOCION_CALCULO_NO_DISPONIBLE",
                        "No se pudo calcular el descuento de una de las variantes seleccionadas."
                );
            }

            if (
                    Boolean.TRUE.equals(
                            calculation.generaMargenNegativo()
                    )
            ) {
                throw new ValidationException(
                        "PROMOCION_MARGEN_NEGATIVO",
                        "El descuento del SKU "
                                + safeSkuCode(
                                calculation.codigoSku()
                        )
                                + " genera margen negativo."
                );
            }
        }
    }

    private String skuKey(
            EntityReferenceDto reference
    ) {
        if (reference == null) {
            return null;
        }

        if (reference.id() != null) {
            return "ID:"
                    + reference.id();
        }

        String codigoSku =
                normalizeKey(
                        reference.codigoSku()
                );

        if (codigoSku != null) {
            return "SKU:"
                    + codigoSku;
        }

        String codigo =
                normalizeKey(
                        reference.codigo()
                );

        if (codigo != null) {
            return "CODIGO:"
                    + codigo;
        }

        String barcode =
                normalizeKey(
                        reference.barcode()
                );

        if (barcode != null) {
            return "BARCODE:"
                    + barcode;
        }

        return null;
    }

    private String normalizeKey(
            String value
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            return null;
        }

        return value
                .trim()
                .toUpperCase(
                        Locale.ROOT
                );
    }

    private String safeSkuCode(
            String codigoSku
    ) {
        if (
                codigoSku == null
                        || codigoSku.isBlank()
        ) {
            return "seleccionado";
        }

        return codigoSku.trim();
    }
}