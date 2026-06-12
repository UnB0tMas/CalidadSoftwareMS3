// ruta: src/main/java/com/upsjb/ms3/service/contract/InventarioOperacionService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.dto.inventario.operacion.request.AjusteInventarioLoteRequestDto;
import com.upsjb.ms3.dto.inventario.operacion.request.EntradaInventarioLoteRequestDto;
import com.upsjb.ms3.dto.inventario.operacion.request.SalidaInventarioLoteRequestDto;
import com.upsjb.ms3.dto.inventario.operacion.request.TransferenciaInventarioRequestDto;
import com.upsjb.ms3.dto.inventario.operacion.response.MovimientoInventarioLoteResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;

public interface InventarioOperacionService {

    ApiResponseDto<MovimientoInventarioLoteResponseDto> registrarEntradas(
            EntradaInventarioLoteRequestDto request
    );

    ApiResponseDto<MovimientoInventarioLoteResponseDto> registrarSalidas(
            SalidaInventarioLoteRequestDto request
    );

    ApiResponseDto<MovimientoInventarioLoteResponseDto> registrarAjustes(
            AjusteInventarioLoteRequestDto request
    );

    ApiResponseDto<MovimientoInventarioLoteResponseDto> registrarTransferencia(
            TransferenciaInventarioRequestDto request
    );
}
