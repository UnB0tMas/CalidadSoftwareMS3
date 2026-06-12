package com.upsjb.ms3.controller;

import com.upsjb.ms3.dto.promocion.request.PromocionSkuDescuentoBulkCreateRequestDto;
import com.upsjb.ms3.dto.promocion.response.PromocionSkuDescuentoResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.service.contract.PromocionSkuDescuentoBulkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(
        "/api/ms3/admin/promociones"
)
public class PromocionSkuDescuentoBulkController {

    private final PromocionSkuDescuentoBulkService
            promocionSkuDescuentoBulkService;

    @PostMapping(
            "/versiones/{idPromocionVersion}/descuentos/masivo"
    )
    @Operation(
            summary = "Agregar descuentos a múltiples SKU",
            description = """
                    Valida y registra en una sola transacción los descuentos
                    de todas las variantes seleccionadas. Si una variante no
                    cumple las reglas, no se registra ninguna.
                    """
    )
    public ResponseEntity<
            ApiResponseDto<
                    List<
                            PromocionSkuDescuentoResponseDto
                            >
                    >
            > agregarDescuentosMasivos(
            @Parameter(
                    description = "ID de la versión de promoción.",
                    required = true
            )
            @Positive(
                    message = "El ID de la versión de promoción debe ser positivo."
            )
            @PathVariable
            Long idPromocionVersion,

            @Valid
            @RequestBody
            PromocionSkuDescuentoBulkCreateRequestDto request
    ) {
        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        promocionSkuDescuentoBulkService.agregar(
                                idPromocionVersion,
                                request
                        )
                );
    }
}