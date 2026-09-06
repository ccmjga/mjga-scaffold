package com.zl.mjga.platform.events

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.time.Duration
import java.util.UUID

@Tag("unit")
class EventPublicationEndpointTest {
    private val operations = RecordingOperations()
    private val endpoint = EventPublicationEndpoint(operations)

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `read and recovery operations apply bounded defaults and the authenticated actor`() {
        authenticate("operator")

        assertThat(endpoint.incomplete(null)).isEmpty()
        assertThat(endpoint.resubmit(null, null))
            .isEqualTo(EventPublicationOperations.RecoveryOutcome(1, "resubmitted"))
        assertThat(operations.minimumAge).isEqualTo(Duration.ofSeconds(30))
        assertThat(operations.limit).isEqualTo(100)
        assertThat(operations.context?.actor).isEqualTo("operator")
        assertThat(operations.context?.kind).isEqualTo(InvocationContext.InvocationKind.SYSTEM)
    }

    @Test
    fun `skip requires an authenticated operator and propagates the reason`() {
        assertThatThrownBy { endpoint.skip(UUID.randomUUID(), "operator decision") }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("Authenticated event publication operator is required")

        authenticate("operator")
        val publicationId = UUID.randomUUID()
        assertThat(endpoint.skip(publicationId, "operator decision"))
            .isEqualTo(EventPublicationOperations.RecoveryOutcome(1, "skipped"))
        assertThat(operations.publicationId).isEqualTo(publicationId)
        assertThat(operations.reason).isEqualTo("operator decision")
        assertThat(operations.context?.actor).isEqualTo("operator")
    }

    private fun authenticate(name: String) {
        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = TestingAuthenticationToken(name, "ignored", "ROLE_EVENT_OPERATOR")
        SecurityContextHolder.setContext(context)
    }

    private class RecordingOperations : EventPublicationOperations {
        var minimumAge: Duration? = null
        var limit: Int? = null
        var publicationId: UUID? = null
        var reason: String? = null
        var context: InvocationContext? = null

        override fun incomplete(limit: Int): List<EventPublicationOperations.PublicationSummary> {
            this.limit = limit
            return emptyList()
        }

        override fun resubmit(
            minimumAge: Duration,
            limit: Int,
            context: InvocationContext,
        ): EventPublicationOperations.RecoveryOutcome {
            this.minimumAge = minimumAge
            this.limit = limit
            this.context = context
            return EventPublicationOperations.RecoveryOutcome(1, "resubmitted")
        }

        override fun skip(
            publicationId: UUID,
            reason: String,
            context: InvocationContext,
        ): EventPublicationOperations.RecoveryOutcome {
            this.publicationId = publicationId
            this.reason = reason
            this.context = context
            return EventPublicationOperations.RecoveryOutcome(1, "skipped")
        }
    }
}
