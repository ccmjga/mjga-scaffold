package com.zl.mjga.platform.events;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Stable event envelope; payload implementations must also have a versioned JSON schema. */
public interface VersionedDomainEvent {
  UUID eventId();

  String eventType();

  int schemaVersion();

  Instant occurredAt();

  String sourceModule();

  String correlationId();

  Optional<String> causationId();
}
