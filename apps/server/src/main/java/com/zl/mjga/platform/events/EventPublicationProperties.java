package com.zl.mjga.platform.events;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("mjga.events")
public record EventPublicationProperties(
    boolean managementEnabled,
    @Nullable String managementUsername,
    @Nullable String managementPassword,
    @NotNull Duration initialDelay,
    @NotNull Duration scanInterval,
    @Min(1) @Max(1000) int batchSize,
    @Min(1) @Max(64) int concurrency,
    @Min(1) @Max(100) int maxAttempts,
    @NotNull Duration minimumAge,
    @NotNull Duration completedRetention,
    @NotNull Duration auditRetention,
    @Min(1) @Max(1000) int managementBatchLimit) {

  public EventPublicationProperties {
    if (managementEnabled
        && (managementUsername == null
            || managementUsername.isBlank()
            || managementPassword == null
            || managementPassword.isBlank())) {
      throw new IllegalArgumentException(
          "mjga.events management credentials are required when management is enabled");
    }
    requirePositive(initialDelay, "initial-delay");
    requirePositive(scanInterval, "scan-interval");
    requirePositive(minimumAge, "minimum-age");
    requirePositive(completedRetention, "completed-retention");
    requirePositive(auditRetention, "audit-retention");
  }

  private static void requirePositive(Duration value, String name) {
    if (value == null || value.isZero() || value.isNegative()) {
      throw new IllegalArgumentException("mjga.events." + name + " must be positive");
    }
  }
}
