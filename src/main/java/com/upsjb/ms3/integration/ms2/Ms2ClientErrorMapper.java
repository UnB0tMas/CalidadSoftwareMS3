// ruta: src/main/java/com/upsjb/ms3/integration/ms2/Ms2ClientErrorMapper.java
package com.upsjb.ms3.integration.ms2;

import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class Ms2ClientErrorMapper {

    public Ms2ClientException integrationDisabled(String operation) {
        return new Ms2ClientException(
                "MS2_INTEGRACION_DESHABILITADA",
                "La integración con MS2 está deshabilitada para este entorno.",
                operation
        );
    }

    public Ms2ClientException invalidRequest(String message, String operation) {
        return new Ms2ClientException(
                "MS2_REQUEST_INVALIDA",
                message,
                operation
        );
    }

    public Ms2ClientException notFound(String operation) {
        return new Ms2ClientException(
                "MS2_EMPLEADO_NO_ENCONTRADO",
                "MS2 no devolvió información del empleado solicitado.",
                operation,
                404,
                null
        );
    }

    public Ms2ClientException map(Throwable cause, String operation) {
        if (cause instanceof Ms2ClientException ms2ClientException) {
            return ms2ClientException;
        }

        if (cause instanceof RestClientResponseException responseException) {
            return fromHttpException(responseException, operation);
        }

        if (cause instanceof ResourceAccessException) {
            return new Ms2ClientException(
                    "MS2_TIMEOUT_O_CONEXION",
                    "No se pudo conectar con MS2. Intente nuevamente.",
                    operation,
                    null,
                    cause
            );
        }

        return new Ms2ClientException(
                "MS2_INTEGRATION_ERROR",
                "Ocurrió un error al comunicarse con MS2.",
                operation,
                cause
        );
    }

    public Ms2ClientException fromHttpException(RestClientResponseException ex, String operation) {
        int status = ex.getStatusCode().value();

        if (status == 404) {
            return notFound(operation);
        }

        if (status >= 400 && status < 500) {
            return new Ms2ClientException(
                    "MS2_CLIENT_ERROR",
                    "MS2 rechazó la solicitud enviada. Revise los datos enviados.",
                    operation,
                    status,
                    ex
            );
        }

        return new Ms2ClientException(
                "MS2_SERVER_ERROR",
                "MS2 no pudo procesar la solicitud. Intente nuevamente.",
                operation,
                status,
                ex
        );
    }
}