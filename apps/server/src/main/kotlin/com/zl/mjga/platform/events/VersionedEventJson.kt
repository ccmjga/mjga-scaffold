package com.zl.mjga.platform.events

import tools.jackson.databind.node.ObjectNode
import java.time.Instant
import java.util.UUID

/** Runtime boundary validation and deterministic v0-to-v1 upcasting for durable JSON events. */
object VersionedEventJson {
    private const val EVENT_TYPE_FIELD = "eventType"
    private const val SCHEMA_VERSION_FIELD = "schemaVersion"
    private val fields =
        setOf(
            "eventId",
            EVENT_TYPE_FIELD,
            SCHEMA_VERSION_FIELD,
            "occurredAt",
            "sourceModule",
            "correlationId",
            "causationId",
            "payload",
        )

    fun upcastAndValidate(input: ObjectNode): ObjectNode =
        input.deepCopy().also { event ->
            if (event.path(SCHEMA_VERSION_FIELD).asInt(-1) == 0) {
                event.remove("occurredOn")?.let { event.set("occurredAt", it) }
                event.put(SCHEMA_VERSION_FIELD, 1)
                val type = event.path(EVENT_TYPE_FIELD).stringValue("")
                if (!type.endsWith(".v1")) event.put(EVENT_TYPE_FIELD, "$type.v1")
            }
            validate(event)
        }

    fun validate(event: ObjectNode) {
        event.propertyNames().forEach { field ->
            require(field in fields) { "Unknown event envelope field $field" }
        }
        requiredText(event, "sourceModule")
        requiredText(event, "correlationId")
        val type = requiredText(event, EVENT_TYPE_FIELD)
        require(type.matches(Regex("^[a-z][a-z0-9.-]+\\.v1$"))) { "eventType must end in .v1" }
        require(event.path(SCHEMA_VERSION_FIELD).asInt(-1) == 1 && event.path("payload").isObject) {
            "schemaVersion 1 and object payload are required"
        }
        try {
            UUID.fromString(requiredText(event, "eventId"))
            Instant.parse(requiredText(event, "occurredAt"))
        } catch (exception: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid event identity or timestamp", exception)
        }
    }

    private fun requiredText(
        event: ObjectNode,
        field: String,
    ): String = event.path(field).stringValue("").also { require(it.isNotBlank()) { "$field is required" } }
}
