package com.upsjb.ms3.shared.idempotency;

public enum DuplicateEventDecision {

    PROCESS,
    IGNORE_DUPLICATE,
    ALREADY_PROCESSED,
    REJECT_INVALID_KEY;

    public boolean shouldProcess() {
        return this == PROCESS;
    }

    public boolean isDuplicate() {
        return this == IGNORE_DUPLICATE || this == ALREADY_PROCESSED;
    }
}