package com.zl.mjga.platform.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class EventPublicationPropertiesTest {

  @Test
  void defaultsRemainBoundedAndAuditable() {
    EventPublicationProperties properties =
        new EventPublicationProperties(
            false,
            null,
            null,
            Duration.ofSeconds(30),
            Duration.ofSeconds(30),
            100,
            4,
            10,
            Duration.ofSeconds(30),
            Duration.ofDays(30),
            Duration.ofDays(365),
            100);

    assertThat(properties.maxAttempts()).isEqualTo(10);
    assertThat(properties.completedRetention()).isEqualTo(Duration.ofDays(30));
    assertThat(properties.auditRetention()).isEqualTo(Duration.ofDays(365));
  }

  @Test
  void managementCannotBeEnabledWithoutIndependentCredentials() {
    assertThatThrownBy(
            () ->
                new EventPublicationProperties(
                    true,
                    null,
                    null,
                    Duration.ofSeconds(30),
                    Duration.ofSeconds(30),
                    100,
                    4,
                    10,
                    Duration.ofSeconds(30),
                    Duration.ofDays(30),
                    Duration.ofDays(365),
                    100))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("management credentials are required");
  }
}
