package com.zl.mjga.platform.events

import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.modulith.events.CompletedEventPublications
import org.springframework.modulith.events.EventPublication
import org.springframework.modulith.events.IncompleteEventPublications
import org.springframework.modulith.events.ResubmissionOptions
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant

@Component
class EventPublicationRecovery(
    private val publications: IncompleteEventPublications,
    private val completedPublications: CompletedEventPublications,
    private val properties: EventPublicationProperties,
    private val jdbc: JdbcClient,
    private val meters: MeterRegistry,
) {
    @Volatile private var ready = false

    @EventListener(ApplicationReadyEvent::class)
    fun applicationReady() {
        ready = true
    }

    @Scheduled(
        initialDelayString = "\${mjga.events.initial-delay}",
        fixedDelayString = "\${mjga.events.scan-interval}",
    )
    @Transactional
    fun recover() {
        if (!ready) return
        val claimed =
            jdbc
                .sql("select pg_try_advisory_xact_lock(:lock)")
                .param("lock", RECOVERY_LOCK)
                .query(Boolean::class.java)
                .single()
        if (!claimed) {
            meters.counter("mjga.events.recovery.runs", "outcome", "contended").increment()
            return
        }
        val candidates =
            jdbc
                .sql(
                    "select count(*) from mjga_platform.event_publication where completion_date is null",
                ).query(Int::class.java)
                .single()
        publications.resubmitIncompletePublications(
            ResubmissionOptions
                .defaults()
                .withMinAge(properties.minimumAge)
                .withBatchSize(properties.batchSize)
                .withMaxInFlight(properties.concurrency)
                .withFilter(::eligible),
        )
        completedPublications.deletePublicationsOlderThan(properties.completedRetention)
        jdbc
            .sql(
                "delete from mjga_platform.event_publication_audit " +
                    "where created_at < now() - cast(:retention as interval)",
            ).param("retention", "${properties.auditRetention.toSeconds()} seconds")
            .update()
        meters.counter("mjga.events.recovery.runs", "outcome", "completed").increment()
        meters.summary("mjga.events.recovery.batch").record(minOf(candidates, properties.batchSize).toDouble())
        logger.info("Durable event recovery completed candidates={} batchLimit={}", candidates, properties.batchSize)
    }

    fun minimumAge(): Duration = properties.minimumAge

    private fun eligible(publication: EventPublication): Boolean {
        val attempts = publication.completionAttempts
        if (attempts >= properties.maxAttempts) return false
        val backoff = properties.minimumAge.multipliedBy(1L shl minOf(attempts, 20))
        val backoffOrigin: Instant = publication.lastResubmissionDate ?: publication.publicationDate
        return !backoffOrigin.plus(backoff).isAfter(Instant.now())
    }

    private companion object {
        val logger = LoggerFactory.getLogger(EventPublicationRecovery::class.java)
        const val RECOVERY_LOCK = 6_577_669_741L
    }
}
