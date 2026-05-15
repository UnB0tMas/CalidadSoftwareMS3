// ruta: src/main/java/com/upsjb/ms3/integration/ms4/Ms4ClientErrorMapper.java
package com.upsjb.ms3.integration.ms4;

import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class Ms4ClientErrorMapper {

    public Ms4ClientException integrationDisabled(String operation) {
        return new Ms4ClientException(
                "MS4_INTEGRACION_DESHABILITADA",
                "La integración con MS4 está deshabilitada para este entorno.",
                operation
        );
    }

    public Ms4ClientException invalidRequest(String message, String operation) {
        return new Ms4ClientException(
                "MS4_REQUEST_INVALIDA",
                message,
                operation
        );
    }

    public Ms4ClientException map(Throwable cause, String operation) {
        if (cause instanceof Ms4ClientException ms4ClientException) {
            return ms4ClientException;
        }

        if (cause instanceof RestClientResponseException responseException) {
            return fromHttpException(responseException, operation);
        }

        if (cause instanceof ResourceAccessException) {
            return new Ms4ClientException(
                    "MS4_TIMEOUT_O_CONEXION",
                    "No se pudo conectar con MS4. Intente nuevamente.",
                    operation,
                    null,
                    cause
            );
        }

        return new Ms4ClientException(
                "MS4_INTEGRATION_ERROR",
                "Ocurrió un error al comunicarse con MS4.",
                operation,
                cause
        );
    }

    public Ms4ClientException fromHttpException(RestClientResponseException ex, String operation) {
        int status = ex.getStatusCode().value();

        if (status >= 400 && status < 500) {
            return new Ms4ClientException(
                    "MS4_CLIENT_ERROR",
                    "MS4 rechazó la solicitud enviada. Revise los datos enviados.",
                    operation,
                    status,
                    ex
            );
        }

        return new Ms4ClientException(
                "MS4_SERVER_ERROR",
                "MS4 no pudo procesar la solicitud. Intente nuevamente.",
                operation,
                status,
                ex
        );
    }
}