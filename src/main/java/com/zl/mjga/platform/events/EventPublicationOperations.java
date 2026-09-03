package com.zl.mjga.platform.events;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/** Protected application interface for inspecting and recovering durable event publications. */
public interface EventPublicationOperations {

  List<PublicationSummary> incomplete(int limit);

  RecoveryOutcome resubmit(Duration minimumAge, int limit, InvocationContext context);

  RecoveryOutcome skip(UUID publicationId, String reason, InvocationContext context);

  record PublicationSummary(
      UUID publicationId, String listenerId, String eventType, int attempts, String status) {}

  record RecoveryOutcome(int affected, String outcome) {}
}
