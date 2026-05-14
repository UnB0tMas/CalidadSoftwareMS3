// ruta: src/main/java/com/upsjb/ms3/service/contract/MovimientoInventarioService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.dto.inventario.movimiento.filter.MovimientoInventarioFilterDto;
import com.upsjb.ms3.dto.inventario.movimiento.request.AjusteInventarioRequestDto;
import com.upsjb.ms3.dto.inventario.movimiento.request.EntradaInventarioRequestDto;
import com.upsjb.ms3.dto.inventario.movimiento.request.MovimientoCompensatorioRequestDto;
import com.upsjb.ms3.dto.inventario.movimiento.request.SalidaInventarioRequestDto;
import com.upsjb.ms3.dto.inventario.movimiento.response.MovimientoInventarioResponseDto;
import com.upsjb.ms3.dto.shared.ApiResponseDto;
import com.upsjb.ms3.dto.shared.PageRequestDto;
import com.upsjb.ms3.dto.shared.PageResponseDto;

public interface MovimientoInventarioService {

    ApiResponseDto<MovimientoInventarioResponseDto> registrarEntrada(EntradaInventarioRequestDto request);

    ApiResponseDto<MovimientoInventarioResponseDto> registrarSalida(SalidaInventarioRequestDto request);

    ApiResponseDto<MovimientoInventarioResponseDto> registrarAjuste(AjusteInventarioRequestDto request);

    ApiResponseDto<MovimientoInventarioResponseDto> registrarCompensacion(MovimientoCompensatorioRequestDto request);

    ApiResponseDto<MovimientoInventarioResponseDto> obtenerDetalle(Long idMovimiento, Boolean incluirCostos);

    ApiResponseDto<PageResponseDto<MovimientoInventarioResponseDto>> listar(
            MovimientoInventarioFilterDto filter,
            PageRequestDto pageRequest,
            Boolean incluirCostos
    );
}