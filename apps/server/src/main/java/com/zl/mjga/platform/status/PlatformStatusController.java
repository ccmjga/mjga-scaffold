package com.zl.mjga.platform.status;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public final class PlatformStatusController {
  private final String application;
  private final String version;

  public PlatformStatusController(
      @Value("${spring.application.name}") String application,
      @Value("${info.app.version:1.0.0}") String version) {
    this.application = application;
    this.version = version;
  }

  @GetMapping("/api/v1/platform/status")
  public PlatformStatus status() {
    return new PlatformStatus("UP", application, version);
  }

  public record PlatformStatus(String status, String application, String version) {}
}
