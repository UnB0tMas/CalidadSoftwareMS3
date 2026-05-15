package com.upsjb.ms3.security.config;

import com.upsjb.ms3.security.jwt.JwtClaimNames;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.util.StringUtils;

@Configuration
@RequiredArgsConstructor
public class ResourceServerConfig {

    private static final String PROPERTY_JWK_SET_URI =
            "spring.security.oauth2.resourceserver.jwt.jwk-set-uri";

    private static final String PROPERTY_ISSUER_URI =
            "spring.security.oauth2.resourceserver.jwt.issuer-uri";

    private static final String PROPERTY_MS1_JWT_ISSUER =
            "ms1.jwt-issuer";

    private final Environment environment;

    @Bean
    public JwtDecoder jwtDecoder() {
        String jwkSetUri = environment.getProperty(PROPERTY_JWK_SET_URI);
        String issuerUri = resolveIssuerUri();

        NimbusJwtDecoder decoder;

        if (StringUtils.hasText(jwkSetUri)) {
            decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri.trim()).build();
        } else if (StringUtils.hasText(issuerUri)) {
            JwtDecoder issuerDecoder = JwtDecoders.fromIssuerLocation(issuerUri.trim());

            if (issuerDecoder instanceof NimbusJwtDecoder nimbusJwtDecoder) {
                decoder = nimbusJwtDecoder;
            } else {
                return issuerDecoder;
            }
        } else {
            throw new IllegalStateException(
                    "Debe configurar spring.security.oauth2.resourceserver.jwt.jwk-set-uri "
                            + "o spring.security.oauth2.resourceserver.jwt.issuer-uri."
            );
        }

        decoder.setJwtValidator(buildJwtValidator(issuerUri));
        return decoder;
    }

    private OAuth2TokenValidator<Jwt> buildJwtValidator(String issuerUri) {
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();

        if (StringUtils.hasText(issuerUri)) {
            validators.add(JwtValidators.createDefaultWithIssuer(issuerUri.trim()));
        } else {
            validators.add(JwtValidators.createDefault());
        }

        validators.add(new AudienceValidator(resolveIndexedList("ms1.required-audiences")));

        validators.add(new RequiredMs1ClaimsValidator(
                environment.getProperty("ms1.claims.user-id", JwtClaimNames.USER_ID),
                environment.getProperty("ms1.claims.session-id", JwtClaimNames.SESSION_ID),
                environment.getProperty("ms1.claims.token-type", JwtClaimNames.TOKEN_TYPE),
                environment.getProperty("ms1.claims.role", JwtClaimNames.ROL),
                environment.getProperty("ms1.claims.roles", JwtClaimNames.ROLES),
                environment.getProperty("ms1.claims.authorities", JwtClaimNames.AUTHORITIES),
                environment.getProperty("ms1.access-token-type", "access"),
                environment.getProperty("ms1.require-user-id-claim", Boolean.class, true),
                environment.getProperty("ms1.require-session-claim", Boolean.class, true),
                environment.getProperty("ms1.require-role-claim", Boolean.class, true),
                environment.getProperty("ms1.require-token-type-claim", Boolean.class, true)
        ));

        return jwt -> {
            for (OAuth2TokenValidator<Jwt> validator : validators) {
                OAuth2TokenValidatorResult result = validator.validate(jwt);

                if (result.hasErrors()) {
                    return result;
                }
            }

            return OAuth2TokenValidatorResult.success();
        };
    }

    private String resolveIssuerUri() {
        String ms1Issuer = environment.getProperty(PROPERTY_MS1_JWT_ISSUER);

        if (StringUtils.hasText(ms1Issuer)) {
            return ms1Issuer.trim();
        }

        String springIssuer = environment.getProperty(PROPERTY_ISSUER_URI);

        if (StringUtils.hasText(springIssuer)) {
            return springIssuer.trim();
        }

        return null;
    }

    private List<String> resolveIndexedList(String prefix) {
        List<String> values = new ArrayList<>();

        for (int index = 0; index < 50; index++) {
            String value = environment.getProperty(prefix + "[" + index + "]");

            if (!StringUtils.hasText(value)) {
                break;
            }

            values.add(value.trim());
        }

        return values;
    }

    private static final class AudienceValidator implements OAuth2TokenValidator<Jwt> {

        private final List<String> acceptedAudiences;

        private AudienceValidator(List<String> acceptedAudiences) {
            this.acceptedAudiences = acceptedAudiences == null ? List.of() : acceptedAudiences;
        }

        @Override
        public OAuth2TokenValidatorResult validate(Jwt jwt) {
            if (acceptedAudiences.isEmpty()) {
                return OAuth2TokenValidatorResult.success();
            }

            List<String> tokenAudiences = jwt.getAudience();

            boolean valid = tokenAudiences != null
                    && tokenAudiences.stream().anyMatch(acceptedAudiences::contains);

            if (valid) {
                return OAuth2TokenValidatorResult.success();
            }

            return invalid("El JWT no contiene una audiencia válida para MS3.");
        }

        private OAuth2TokenValidatorResult invalid(String message) {
            return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                    OAuth2ErrorCodes.INVALID_TOKEN,
                    message,
                    null
            ));
        }
    }

    private static final class RequiredMs1ClaimsValidator implements OAuth2TokenValidator<Jwt> {

        private final String userIdClaim;
        private final String sessionIdClaim;
        private final String tokenTypeClaim;
        private final String roleClaim;
        private final String rolesClaim;
        private final String authoritiesClaim;
        private final String requiredTokenType;
        private final boolean requireUserId;
        private final boolean requireSession;
        private final boolean requireRole;
        private final boolean requireTokenType;

        private RequiredMs1ClaimsValidator(
                String userIdClaim,
                String sessionIdClaim,
                String tokenTypeClaim,
                String roleClaim,
                String rolesClaim,
                String authoritiesClaim,
                String requiredTokenType,
                boolean requireUserId,
                boolean requireSession,
                boolean requireRole,
                boolean requireTokenType
        ) {
            this.userIdClaim = userIdClaim;
            this.sessionIdClaim = sessionIdClaim;
            this.tokenTypeClaim = tokenTypeClaim;
            this.roleClaim = roleClaim;
            this.rolesClaim = rolesClaim;
            this.authoritiesClaim = authoritiesClaim;
            this.requiredTokenType = requiredTokenType;
            this.requireUserId = requireUserId;
            this.requireSession = requireSession;
            this.requireRole = requireRole;
            this.requireTokenType = requireTokenType;
        }

        @Override
        public OAuth2TokenValidatorResult validate(Jwt jwt) {
            if (requireUserId && !hasTextClaim(jwt, userIdClaim)) {
                return invalid("El JWT no contiene el claim obligatorio de usuario MS1.");
            }

            if (requireSession && !hasTextClaim(jwt, sessionIdClaim)) {
                return invalid("El JWT no contiene el claim obligatorio de sesión.");
            }

            if (requireRole && !hasAnyRoleClaim(jwt)) {
                return invalid("El JWT no contiene rol o authorities válidos.");
            }

            if (requireTokenType && StringUtils.hasText(requiredTokenType)) {
                String tokenType = claimAsString(jwt, tokenTypeClaim);

                if (!requiredTokenType.equalsIgnoreCase(tokenType)) {
                    return invalid("El JWT no es de tipo access token.");
                }
            }

            return OAuth2TokenValidatorResult.success();
        }

        private boolean hasAnyRoleClaim(Jwt jwt) {
            return hasValue(jwt, roleClaim)
                    || hasValue(jwt, rolesClaim)
                    || hasValue(jwt, authoritiesClaim)
                    || hasValue(jwt, JwtClaimNames.ROL)
                    || hasValue(jwt, JwtClaimNames.ROLE)
                    || hasValue(jwt, JwtClaimNames.ROLES)
                    || hasValue(jwt, JwtClaimNames.AUTHORITIES);
        }

        private boolean hasValue(Jwt jwt, String claimName) {
            if (!StringUtils.hasText(claimName)) {
                return false;
            }

            Object value = jwt.getClaims().get(claimName);

            if (value instanceof String stringValue) {
                return StringUtils.hasText(stringValue);
            }

            if (value instanceof Collection<?> collection) {
                return !collection.isEmpty();
            }

            return value != null;
        }

        private boolean hasTextClaim(Jwt jwt, String claimName) {
            return StringUtils.hasText(claimAsString(jwt, claimName));
        }

        private String claimAsString(Jwt jwt, String claimName) {
            if (!StringUtils.hasText(claimName)) {
                return null;
            }

            Object value = jwt.getClaims().get(claimName);
            return value == null ? null : String.valueOf(value);
        }

        private OAuth2TokenValidatorResult invalid(String message) {
            return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                    OAuth2ErrorCodes.INVALID_TOKEN,
                    message,
                    null
            ));
        }
    }
}