package com.zl.mjga.platform.events

import java.time.Instant
import java.util.UUID

/** Stable event envelope; payload implementations must also have a versioned JSON schema. */
interface VersionedDomainEvent {
    fun eventId(): UUID

    fun eventType(): String

    fun schemaVersion(): Int

    fun occurredAt(): Instant

    fun sourceModule(): String

    fun correlationId(): String

    fun causationId(): String?
}
