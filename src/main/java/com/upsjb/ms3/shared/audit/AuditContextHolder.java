package com.upsjb.ms3.shared.audit;

import java.util.Optional;

public final class AuditContextHolder {

    private static final ThreadLocal<AuditContext> CONTEXT = new ThreadLocal<>();

    private AuditContextHolder() {
    }

    public static void set(AuditContext context) {
        if (context == null) {
            clear();
            return;
        }

        CONTEXT.set(context);
    }

    public static Optional<AuditContext> getOptional() {
        return Optional.ofNullable(CONTEXT.get());
    }

    public static AuditContext getOrEmpty() {
        return getOptional().orElseGet(AuditContext::empty);
    }

    public static AuditContext getOrSystem() {
        return getOptional().orElseGet(AuditContext::system);
    }

    public static boolean hasContext() {
        return CONTEXT.get() != null;
    }

    public static void clear() {
        CONTEXT.remove();
    }
}