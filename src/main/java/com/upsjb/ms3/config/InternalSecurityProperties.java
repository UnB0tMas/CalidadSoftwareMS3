// ruta: src/main/java/com/upsjb/ms3/config/InternalSecurityProperties.java
package com.upsjb.ms3.config;

import com.upsjb.ms3.shared.constants.HeaderNames;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "internal.security")
public class InternalSecurityProperties {

    private boolean enabled = true;

    @NotBlank
    private String headerName = HeaderNames.INTERNAL_SERVICE_KEY;

    private String serviceKey = "local-ms3-ms4-internal-key-change-me";

    @AssertTrue(message = "internal.security.service-key es obligatorio cuando internal.security.enabled=true.")
    public boolean isValidServiceKeyConfiguration() {
        if (!enabled) {
            return true;
        }

        return StringUtils.hasText(serviceKey);
    }
}