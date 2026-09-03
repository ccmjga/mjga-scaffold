package com.zl.mjga.platform.events

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.modulith.events.IncompleteEventPublications
import org.springframework.modulith.events.ResubmissionOptions
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.time.Duration
import java.util.UUID

@Component
class JdbcEventPublicationOperations(
    private val jdbc: JdbcClient,
    private val publications: IncompleteEventPublications,
    private val properties: EventPublicationProperties,
) : EventPublicationOperations {
    @Transactional(readOnly = true)
    override fun incomplete(limit: Int): List<EventPublicationOperations.PublicationSummary> {
        val bounded = limit.coerceIn(1, properties.managementBatchLimit)
        return jdbc
            .sql(
                """select id, listener_id, event_type, coalesce(completion_attempts, 0),
                   |coalesce(status, 'INCOMPLETE') from mjga_platform.event_publication
                   |where completion_date is null order by publication_date limit :limit
                """.trimMargin(),
            ).param("limit", bounded)
            .query(::summary)
            .list()
    }

    override fun resubmit(
        minimumAge: Duration,
        limit: Int,
        context: InvocationContext,
    ): EventPublicationOperations.RecoveryOutcome {
        val bounded = limit.coerceIn(1, properties.managementBatchLimit)
        val selected =
            jdbc
                .sql(
                    """select id, listener_id, event_type, coalesce(completion_attempts, 0),
                       |coalesce(status, 'INCOMPLETE') from mjga_platform.event_publication
                       |where completion_date is null
                       |and publication_date < now() - cast(:minimumAge as interval)
                       |order by publication_date limit :limit
                    """.trimMargin(),
                ).param("minimumAge", "${minimumAge.toSeconds()} seconds")
                .param("limit", bounded)
                .query(::summary)
                .list()
        publications.resubmitIncompletePublications(
            ResubmissionOptions.defaults().withMinAge(minimumAge).withBatchSize(bounded),
        )
        selected.forEach { audit("RESUBMIT", it, it.status, it.status, "manual resubmission", context) }
        return EventPublicationOperations.RecoveryOutcome(selected.size, "RESUBMITTED")
    }

    @Transactional
    override fun skip(
        publicationId: UUID,
        reason: String,
        context: InvocationContext,
    ): EventPublicationOperations.RecoveryOutcome {
        require(reason.isNotBlank()) { "Skip reason is required" }
        val selected = incomplete(properties.managementBatchLimit).firstOrNull { it.publicationId == publicationId }
        val affected =
            jdbc
                .sql(
                    "update mjga_platform.event_publication set completion_date = now(), " +
                        "status = 'SKIPPED' where id = :id and completion_date is null",
                ).param("id", publicationId)
                .update()
        if (affected == 1 && selected != null) audit("SKIP", selected, selected.status, "SKIPPED", reason, context)
        return EventPublicationOperations.RecoveryOutcome(affected, if (affected == 1) "SKIPPED" else "NOT_FOUND")
    }

    private fun audit(
        action: String,
        publication: EventPublicationOperations.PublicationSummary,
        beforeStatus: String,
        afterStatus: String,
        reason: String,
        context: InvocationContext,
    ) {
        jdbc
            .sql(
                """insert into mjga_platform.event_publication_audit
                   |(id, publication_id, listener_id, event_type, action, operator_id, reason,
                   |before_status, after_status, correlation_id, created_at)
                   |values (:id, :publicationId, :listenerId, :eventType, :action, :operator,
                   |:reason, :beforeStatus, :afterStatus, :correlation, now())
                """.trimMargin(),
            ).param("id", UUID.randomUUID())
            .param("publicationId", publication.publicationId)
            .param("listenerId", publication.listenerId)
            .param("eventType", publication.eventType)
            .param("action", action)
            .param("operator", context.actor)
            .param("reason", reason)
            .param("beforeStatus", beforeStatus)
            .param("afterStatus", afterStatus)
            .param("correlation", context.correlationId)
            .update()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun summary(
        row: ResultSet,
        rowNumber: Int,
    ) = EventPublicationOperations.PublicationSummary(
        row.getObject(PUBLICATION_ID_COLUMN, UUID::class.java),
        row.getString(LISTENER_ID_COLUMN),
        row.getString(EVENT_TYPE_COLUMN),
        row.getInt(ATTEMPTS_COLUMN),
        row.getString(STATUS_COLUMN),
    )

    private companion object {
        const val PUBLICATION_ID_COLUMN = 1
        const val LISTENER_ID_COLUMN = 2
        const val EVENT_TYPE_COLUMN = 3
        const val ATTEMPTS_COLUMN = 4
        const val STATUS_COLUMN = 5
    }
}
