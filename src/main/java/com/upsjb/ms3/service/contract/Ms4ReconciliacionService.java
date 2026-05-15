// ruta: src/main/java/com/upsjb/ms3/service/contract/Ms4ReconciliacionService.java
package com.upsjb.ms3.service.contract;

import com.upsjb.ms3.dto.ms4.response.Ms4StockSyncResultDto;
import com.upsjb.ms3.kafka.event.Ms4StockCommandPayload;

public interface Ms4ReconciliacionService {

    Ms4StockSyncResultDto procesarReservaPendiente(Ms4StockCommandPayload payload);

    Ms4StockSyncResultDto procesarConfirmacionPendiente(Ms4StockCommandPayload payload);

    Ms4StockSyncResultDto procesarLiberacionPendiente(Ms4StockCommandPayload payload);

    Ms4StockSyncResultDto procesarAnulacionPendiente(Ms4StockCommandPayload payload);
}