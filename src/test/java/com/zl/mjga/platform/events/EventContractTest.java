package com.zl.mjga.platform.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@Tag("contract")
class EventContractTest {
  private final ObjectMapper json = new ObjectMapper();

  @Test
  void versionedEnvelopeExposesStableRuntimeValidatedIdentity() {
    Set<String> methods =
        java.util.Arrays.stream(VersionedDomainEvent.class.getDeclaredMethods())
            .map(Method::getName)
            .collect(Collectors.toSet());

    assertThat(methods)
        .containsExactlyInAnyOrder(
            "eventId",
            "eventType",
            "schemaVersion",
            "occurredAt",
            "sourceModule",
            "correlationId",
            "causationId");
  }

  @Test
  void canonicalJsonEnvelopeRejectsDriftAndUpcastsLegacyFixturesDeterministically()
      throws Exception {
    ObjectNode legacy =
        (ObjectNode)
            json.readTree(
                """
				  {"eventId":"a7bd74d8-8559-4c68-bf9d-c20a965a8c1f","eventType":"billing.invoice-issued","schemaVersion":0,"occurredOn":"2026-09-01T00:00:00Z","sourceModule":"billing","correlationId":"c-1","payload":{}}
				  """);

    ObjectNode first = VersionedEventJson.upcastAndValidate(legacy);
    ObjectNode second = VersionedEventJson.upcastAndValidate(legacy);

    assertThat(first).isEqualTo(second);
    assertThat(first.path("eventType").stringValue()).isEqualTo("billing.invoice-issued.v1");
    assertThat(first.path("occurredAt").stringValue()).isEqualTo("2026-09-01T00:00:00Z");
    first.put("unexpected", true);
    assertThatThrownBy(() -> VersionedEventJson.validate(first))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unknown event envelope field");
    assertThat(
            java.nio.file.Files.readString(
                java.nio.file.Path.of("contracts/events/versioned-domain-event.schema.json")))
        .contains("versioned-domain-event:v1", "additionalProperties");
  }
}
