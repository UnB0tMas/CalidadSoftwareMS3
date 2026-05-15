package com.upsjb.ms3.shared.audit;

import com.upsjb.ms3.domain.enums.ResultadoAuditoria;

public enum AuditResult {

    SUCCESS(ResultadoAuditoria.EXITOSO),
    FAILURE(ResultadoAuditoria.FALLIDO),
    DENIED(ResultadoAuditoria.DENEGADO);

    private final ResultadoAuditoria resultadoAuditoria;

    AuditResult(ResultadoAuditoria resultadoAuditoria) {
        this.resultadoAuditoria = resultadoAuditoria;
    }

    public ResultadoAuditoria toResultadoAuditoria() {
        return resultadoAuditoria;
    }

    public boolean isSuccess() {
        return this == SUCCESS;
    }

    public boolean isFailure() {
        return this == FAILURE;
    }

    public boolean isDenied() {
        return this == DENIED;
    }
}