package com.zl.mjga.platform.events;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Set;
import java.util.UUID;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/** Runtime boundary validation and deterministic v0-to-v1 upcasting for durable JSON events. */
public final class VersionedEventJson {
  private static final String EVENT_TYPE_FIELD = "eventType";
  private static final String SCHEMA_VERSION_FIELD = "schemaVersion";
  private static final Set<String> FIELDS =
      Set.of(
          "eventId",
          EVENT_TYPE_FIELD,
          SCHEMA_VERSION_FIELD,
          "occurredAt",
          "sourceModule",
          "correlationId",
          "causationId",
          "payload");

  private VersionedEventJson() {}

  public static ObjectNode upcastAndValidate(ObjectNode input) {
    ObjectNode event = input.deepCopy();
    if (event.path(SCHEMA_VERSION_FIELD).asInt(-1) == 0) {
      JsonNode occurredOn = event.remove("occurredOn");
      if (occurredOn != null) {
        event.set("occurredAt", occurredOn);
      }
      event.put(SCHEMA_VERSION_FIELD, 1);
      String type = event.path(EVENT_TYPE_FIELD).stringValue("");
      if (!type.endsWith(".v1")) {
        event.put(EVENT_TYPE_FIELD, type + ".v1");
      }
    }
    validate(event);
    return event;
  }

  public static void validate(ObjectNode event) {
    event
        .propertyNames()
        .forEach(
            field -> {
              if (!FIELDS.contains(field)) {
                throw new IllegalArgumentException("Unknown event envelope field " + field);
              }
            });
    requiredText(event, "sourceModule");
    requiredText(event, "correlationId");
    String type = requiredText(event, EVENT_TYPE_FIELD);
    if (!type.matches("^[a-z][a-z0-9.-]+\\.v1$")) {
      throw new IllegalArgumentException("eventType must end in .v1");
    }
    if (event.path(SCHEMA_VERSION_FIELD).asInt(-1) != 1 || !event.path("payload").isObject()) {
      throw new IllegalArgumentException("schemaVersion 1 and object payload are required");
    }
    try {
      UUID.fromString(requiredText(event, "eventId"));
      Instant.parse(requiredText(event, "occurredAt"));
    } catch (IllegalArgumentException | DateTimeParseException exception) {
      throw new IllegalArgumentException("Invalid event identity or timestamp", exception);
    }
  }

  private static String requiredText(ObjectNode event, String field) {
    String value = event.path(field).stringValue("");
    if (value.isBlank()) {
      throw new IllegalArgumentException(field + " is required");
    }
    return value;
  }
}
