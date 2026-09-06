package com.zl.mjga.platform.events;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Endpoint(id = "eventPublications")
@ConditionalOnProperty(name = "mjga.events.management-enabled", havingValue = "true")
final class EventPublicationEndpoint {

  private final EventPublicationOperations operations;

  EventPublicationEndpoint(EventPublicationOperations operations) {
    this.operations = operations;
  }

  @ReadOperation
  List<EventPublicationOperations.PublicationSummary> incomplete(Integer limit) {
    return operations.incomplete(limit == null ? 100 : limit);
  }

  @WriteOperation
  EventPublicationOperations.RecoveryOutcome resubmit(Integer minimumAgeSeconds, Integer limit) {
    return operations.resubmit(
        Duration.ofSeconds(minimumAgeSeconds == null ? 30 : minimumAgeSeconds),
        limit == null ? 100 : limit,
        context());
  }

  @WriteOperation
  EventPublicationOperations.RecoveryOutcome skip(UUID publicationId, String reason) {
    return operations.skip(publicationId, reason, context());
  }

  private InvocationContext context() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new IllegalStateException("Authenticated event publication operator is required");
    }
    return new InvocationContext(
        authentication.getName(),
        java.util.Optional.empty(),
        UUID.randomUUID().toString(),
        java.util.Optional.empty(),
        InvocationContext.InvocationKind.SYSTEM);
  }
}
