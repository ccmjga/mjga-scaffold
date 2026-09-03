package com.zl.mjga.platform.events

import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.UUID

@Component
@Endpoint(id = "eventPublications")
@ConditionalOnProperty(name = ["mjga.events.management-enabled"], havingValue = "true")
class EventPublicationEndpoint(
    private val operations: EventPublicationOperations,
) {
    @ReadOperation
    fun incomplete(limit: Int?): List<EventPublicationOperations.PublicationSummary> =
        operations.incomplete(limit ?: DEFAULT_MANAGEMENT_LIMIT)

    @WriteOperation
    fun resubmit(
        minimumAgeSeconds: Int?,
        limit: Int?,
    ): EventPublicationOperations.RecoveryOutcome =
        operations.resubmit(
            Duration.ofSeconds((minimumAgeSeconds ?: DEFAULT_MINIMUM_AGE_SECONDS).toLong()),
            limit ?: DEFAULT_MANAGEMENT_LIMIT,
            context(),
        )

    @WriteOperation
    fun skip(
        publicationId: UUID,
        reason: String,
    ): EventPublicationOperations.RecoveryOutcome = operations.skip(publicationId, reason, context())

    private fun context(): InvocationContext {
        val authentication = SecurityContextHolder.getContext().authentication
        check(authentication?.isAuthenticated == true) { "Authenticated event publication operator is required" }
        return InvocationContext(
            authentication.name,
            null,
            UUID.randomUUID().toString(),
            null,
            InvocationContext.InvocationKind.SYSTEM,
        )
    }

    private companion object {
        const val DEFAULT_MANAGEMENT_LIMIT = 100
        const val DEFAULT_MINIMUM_AGE_SECONDS = 30
    }
}
