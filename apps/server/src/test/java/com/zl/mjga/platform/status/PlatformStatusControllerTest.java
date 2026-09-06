package com.zl.mjga.platform.status;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class PlatformStatusControllerTest {
  @Test
  void reportsTheApplicationIdentityThroughTheBusinessApi() {
    var status = new PlatformStatusController("orders", "1.2.3").status();
    assertThat(status.status()).isEqualTo("UP");
    assertThat(status.application()).isEqualTo("orders");
    assertThat(status.version()).isEqualTo("1.2.3");
  }
}
