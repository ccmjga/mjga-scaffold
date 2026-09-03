package com.zl.mjga.platform.events

import java.time.Duration
import java.util.UUID

/** Protected application interface for inspecting and recovering durable event publications. */
interface EventPublicationOperations {
    fun incomplete(limit: Int): List<PublicationSummary>

    fun resubmit(
        minimumAge: Duration,
        limit: Int,
        context: InvocationContext,
    ): RecoveryOutcome

    fun skip(
        publicationId: UUID,
        reason: String,
        context: InvocationContext,
    ): RecoveryOutcome

    data class PublicationSummary(
        val publicationId: UUID,
        val listenerId: String,
        val eventType: String,
        val attempts: Int,
        val status: String,
    )

    data class RecoveryOutcome(
        val affected: Int,
        val outcome: String,
    )
}
