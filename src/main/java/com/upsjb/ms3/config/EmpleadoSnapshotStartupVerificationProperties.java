package com.upsjb.ms3.config;

import com.upsjb.ms3.kafka.consumer.Ms2EmpleadoSnapshotConsumer;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(
        prefix = "app.employee-snapshot-startup-verification"
)
public class EmpleadoSnapshotStartupVerificationProperties {

    private boolean enabled = true;

    @NotBlank
    private String topic =
            "ms2.empleado.snapshot.v1";

    @NotBlank
    private String listenerId =
            Ms2EmpleadoSnapshotConsumer.LISTENER_ID;

    @NotNull
    private Duration initialDelay =
            Duration.ofSeconds(2);

    @NotNull
    private Duration retryDelay =
            Duration.ofSeconds(2);

    @NotNull
    private Duration adminTimeout =
            Duration.ofSeconds(5);

    @Min(1)
    @Max(100)
    private int maxAttempts =
            30;

    /*
     * Solo cuenta eventos reales de empleados.
     * Los eventos KFP-EMP-* del probe no se consideran.
     */
    @Min(0)
    private long minimumConsumedEvents =
            1;

    @Min(0)
    private long minimumSnapshots =
            1;

    /*
     * En local permanece false para no derribar MS3 por una demora
     * temporal de MS2. En un despliegue estricto puede activarse.
     */
    private boolean failFast =
            false;
}