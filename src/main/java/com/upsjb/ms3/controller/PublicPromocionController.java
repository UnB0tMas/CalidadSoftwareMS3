// ruta: src/main/java/com/upsjb/ms3/controller/PublicPromocionController.java
package com.upsjb.ms3.controller;

import com.upsjb.ms3.dto.promocion.response.PromocionPublicResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.service.contract.PromocionVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ms3/public/promociones")
@Tag(
        name = "MS3 - Público - Promociones",
        description = "Endpoints públicos para consultar promociones visibles y descuentos comerciales aplicables."
)
public class PublicPromocionController {

    private final PromocionVersionService promocionVersionService;

    @GetMapping
    @Operation(
            summary = "Listar promociones públicas vigentes",
            description = "Devuelve promociones visibles públicamente, activas o programadas aplicables a la fecha actual. No expone costos, márgenes, proveedores, kardex, auditoría ni stock interno."
    )
    public ResponseEntity<ApiResponseDto<List<PromocionPublicResponseDto>>> listarPublicasVigentes() {
        return ResponseEntity.ok(promocionVersionService.listarPublicasVigentes());
    }

    @GetMapping("/vigentes")
    @Operation(
            summary = "Listar promociones públicas vigentes",
            description = "Alias explícito para consultar promociones públicas vigentes desde Angular a través del API Gateway."
    )
    public ResponseEntity<ApiResponseDto<List<PromocionPublicResponseDto>>> listarVigentes() {
        return ResponseEntity.ok(promocionVersionService.listarPublicasVigentes());
    }
}