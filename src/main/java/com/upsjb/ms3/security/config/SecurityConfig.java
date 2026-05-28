package com.upsjb.ms3.security.config;

import com.upsjb.ms3.config.AppPropertiesConfig;
import com.upsjb.ms3.security.filter.InternalServiceAuthenticationFilter;
import com.upsjb.ms3.security.filter.RequestAuditContextFilter;
import com.upsjb.ms3.security.filter.RequestTraceFilter;
import com.upsjb.ms3.security.handler.RestAccessDeniedHandler;
import com.upsjb.ms3.security.handler.RestAuthenticationEntryPoint;
import com.upsjb.ms3.security.jwt.RoleJwtAuthenticationConverter;
import com.upsjb.ms3.security.principal.AuthenticatedUserArgumentResolver;
import com.upsjb.ms3.security.roles.SecurityRoles;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig implements WebMvcConfigurer {

    private static final String[] SWAGGER_WHITELIST = {
            "/swagger-ui.html",
            "/swagger-ui/index.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/v3/api-docs.yml"
    };

    private static final String[] ACTUATOR_PUBLIC_WHITELIST = {
            "/actuator/health",
            "/actuator/health/**",
            "/actuator/info"
    };

    private final AppPropertiesConfig appProperties;
    private final RoleJwtAuthenticationConverter roleJwtAuthenticationConverter;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;
    private final RequestTraceFilter requestTraceFilter;
    private final RequestAuditContextFilter requestAuditContextFilter;
    private final InternalServiceAuthenticationFilter internalServiceAuthenticationFilter;
    private final AuthenticatedUserArgumentResolver authenticatedUserArgumentResolver;

    /*
     * Cadena exclusiva para endpoints internos entre microservicios.
     *
     * Estos endpoints no deben depender de JWT de usuario ADMIN.
     * Se protegen con X-Internal-Service-Key y reciben ROLE_INTERNAL_SERVICE.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain internalSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/internal/**")
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().hasAuthority(SecurityRoles.ROLE_INTERNAL_SERVICE)
                )
                .addFilterBefore(requestTraceFilter, SecurityContextHolderFilter.class)
                .addFilterBefore(internalServiceAuthenticationFilter, AnonymousAuthenticationFilter.class)
                .addFilterAfter(requestAuditContextFilter, AnonymousAuthenticationFilter.class);

        return http.build();
    }

    /*
     * Cadena principal para rutas públicas, administrativas y operativas del MS3.
     *
     * MS3 valida JWT emitidos por MS1 aunque el Gateway también valide.
     * El Gateway filtra entrada general; MS3 conserva seguridad propia.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        /*
                         * Swagger/OpenAPI queda libre únicamente para documentación técnica local.
                         *
                         * Rutas liberadas:
                         * - /swagger-ui.html
                         * - /swagger-ui/**
                         * - /v3/api-docs
                         * - /v3/api-docs/**
                         * - /v3/api-docs.yaml
                         * - /v3/api-docs.yml
                         *
                         * No libera endpoints funcionales del dominio.
                         */
                        .requestMatchers(SWAGGER_WHITELIST).permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/ms3/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ms3/catalogo/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ms3/productos/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ms3/promociones/public/**").permitAll()

                        .requestMatchers(ACTUATOR_PUBLIC_WHITELIST).permitAll()

                        .requestMatchers("/api/ms3/admin/**")
                        .hasAuthority(SecurityRoles.ROLE_ADMIN)

                        .requestMatchers("/api/ms3/outbox/**")
                        .hasAuthority(SecurityRoles.ROLE_ADMIN)

                        .requestMatchers("/api/ms3/auditoria/**")
                        .hasAuthority(SecurityRoles.ROLE_ADMIN)

                        .requestMatchers("/api/ms3/catalogo/**")
                        .hasAnyAuthority(SecurityRoles.ROLE_ADMIN, SecurityRoles.ROLE_EMPLEADO)

                        .requestMatchers("/api/ms3/inventario/**")
                        .hasAnyAuthority(SecurityRoles.ROLE_ADMIN, SecurityRoles.ROLE_EMPLEADO)

                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(roleJwtAuthenticationConverter))
                )
                .addFilterBefore(requestTraceFilter, BearerTokenAuthenticationFilter.class)
                .addFilterAfter(requestAuditContextFilter, BearerTokenAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        AppPropertiesConfig.Cors corsProperties = appProperties.getCors();

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        configuration.setExposedHeaders(corsProperties.getExposedHeaders());
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());
        configuration.setMaxAge(corsProperties.getMaxAgeSeconds());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(corsProperties.getPathPattern(), configuration);

        return source;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authenticatedUserArgumentResolver);
    }
}