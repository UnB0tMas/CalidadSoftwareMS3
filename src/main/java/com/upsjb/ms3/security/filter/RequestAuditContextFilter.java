package com.upsjb.ms3.security.filter;

import com.upsjb.ms3.domain.enums.RolSistema;
import com.upsjb.ms3.security.principal.AuthenticatedUserContext;
import com.upsjb.ms3.security.principal.CurrentUserResolver;
import com.upsjb.ms3.shared.audit.AuditContext;
import com.upsjb.ms3.shared.audit.AuditContextHolder;
import com.upsjb.ms3.shared.constants.HeaderNames;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class RequestAuditContextFilter extends OncePerRequestFilter {

    public static final String AUDIT_CONTEXT_ATTRIBUTE = "ms3.auditContext";

    private static final String MDC_ACTOR_USER_ID = "actorUserId";
    private static final String MDC_ACTOR_ROLE = "actorRole";

    private final CurrentUserResolver currentUserResolver;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        AuditContext context = buildContext(request);
        AuditContextHolder.set(context);
        request.setAttribute(AUDIT_CONTEXT_ATTRIBUTE, context);

        addMdc(context);

        try {
            filterChain.doFilter(request, response);
        } finally {
            request.removeAttribute(AUDIT_CONTEXT_ATTRIBUTE);
            AuditContextHolder.clear();
            MDC.remove(MDC_ACTOR_USER_ID);
            MDC.remove(MDC_ACTOR_ROLE);
        }
    }

    private AuditContext buildContext(HttpServletRequest request) {
        AuditContext.Builder builder = AuditContext.builder()
                .ipAddress(resolveClientIp(request))
                .userAgent(request.getHeader(HeaderNames.USER_AGENT))
                .httpMethod(request.getMethod())
                .requestPath(request.getRequestURI())
                .queryString(request.getQueryString())
                .requestId(resolveAttributeOrHeader(request, RequestTraceFilter.REQUEST_ID_ATTRIBUTE, HeaderNames.REQUEST_ID))
                .correlationId(resolveAttributeOrHeader(request, RequestTraceFilter.CORRELATION_ID_ATTRIBUTE, HeaderNames.CORRELATION_ID));

        currentUserResolver.resolveOptional().ifPresent(user -> applyUser(builder, user));

        return builder.build();
    }

    private void applyUser(AuditContext.Builder builder, AuthenticatedUserContext user) {
        builder.idUsuarioActorMs1(user.getIdUsuarioMs1())
                .idEmpleadoActorMs2(user.getIdEmpleadoMs2())
                .username(user.getUsername())
                .email(user.getEmail())
                .sessionId(user.getSessionId())
                .rolActor(resolveRol(user.getRolPrincipal()));
    }

    private RolSistema resolveRol(String rolPrincipal) {
        if (!StringUtils.hasText(rolPrincipal)) {
            return RolSistema.ANONIMO;
        }

        try {
            return RolSistema.fromCode(rolPrincipal);
        } catch (IllegalArgumentException ex) {
            return RolSistema.ANONIMO;
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader(HeaderNames.FORWARDED_FOR);

        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader(HeaderNames.REAL_IP);

        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }

    private String resolveAttributeOrHeader(HttpServletRequest request, String attributeName, String headerName) {
        Object attribute = request.getAttribute(attributeName);

        if (attribute != null && StringUtils.hasText(String.valueOf(attribute))) {
            return String.valueOf(attribute);
        }

        return request.getHeader(headerName);
    }

    private void addMdc(AuditContext context) {
        if (context.idUsuarioActorMs1() != null) {
            MDC.put(MDC_ACTOR_USER_ID, String.valueOf(context.idUsuarioActorMs1()));
        }

        if (context.rolActor() != null) {
            MDC.put(MDC_ACTOR_ROLE, context.rolActor().getCode());
        }
    }
}