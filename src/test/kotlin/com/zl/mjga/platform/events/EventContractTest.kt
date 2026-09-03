package com.zl.mjga.platform.events

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.node.ObjectNode
import java.nio.file.Files
import java.nio.file.Path

@Tag("contract")
class EventContractTest {
    private val json = ObjectMapper()

    @Test
    fun `versioned envelope exposes stable runtime validated identity`() {
        val methods =
            VersionedDomainEvent::class.java.declaredMethods
                .map { it.name }
                .toSet()
        assertThat(methods).containsExactlyInAnyOrder(
            "eventId",
            "eventType",
            "schemaVersion",
            "occurredAt",
            "sourceModule",
            "correlationId",
            "causationId",
        )
    }

    @Test
    fun `canonical JSON envelope rejects drift and upcasts legacy fixtures deterministically`() {
        val legacy =
            json.readTree(
                """{
                    "eventId":"a7bd74d8-8559-4c68-bf9d-c20a965a8c1f",
                    "eventType":"billing.invoice-issued",
                    "schemaVersion":0,
                    "occurredOn":"2026-09-01T00:00:00Z",
                    "sourceModule":"billing",
                    "correlationId":"c-1",
                    "payload":{}
                }""",
            ) as ObjectNode
        val first = VersionedEventJson.upcastAndValidate(legacy)
        val second = VersionedEventJson.upcastAndValidate(legacy)
        assertThat(first).isEqualTo(second)
        assertThat(first.path("eventType").stringValue()).isEqualTo("billing.invoice-issued.v1")
        assertThat(first.path("occurredAt").stringValue()).isEqualTo("2026-09-01T00:00:00Z")
        first.put("unexpected", true)
        assertThatThrownBy { VersionedEventJson.validate(first) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Unknown event envelope field")
        assertThat(Files.readString(Path.of("contracts/events/versioned-domain-event.schema.json")))
            .contains("versioned-domain-event:v1", "additionalProperties")
    }
}
