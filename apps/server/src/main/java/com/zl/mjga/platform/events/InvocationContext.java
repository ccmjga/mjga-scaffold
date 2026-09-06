package com.zl.mjga.platform.events;

import java.util.Objects;
import java.util.Optional;

/** Trusted framework-neutral identity and trace context propagated across Capability Interfaces. */
public record InvocationContext(
    String actor,
    Optional<String> tenant,
    String correlationId,
    Optional<String> causationId,
    InvocationKind kind) {

  public InvocationContext {
    actor = requireText(actor, "actor");
    Objects.requireNonNull(tenant, "tenant");
    correlationId = requireText(correlationId, "correlationId");
    Objects.requireNonNull(causationId, "causationId");
    Objects.requireNonNull(kind, "kind");
  }

  private static String requireText(String value, String name) {
    if (value.isBlank()) {
      throw new IllegalArgumentException(name + " must not be blank");
    }
    return value;
  }

  public enum InvocationKind {
    USER,
    SYSTEM,
    SCHEDULED,
    EVENT
  }
}
