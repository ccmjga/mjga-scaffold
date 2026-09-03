package com.zl.mjga.platform.events;

import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.modulith.events.CompletedEventPublications;
import org.springframework.modulith.events.EventPublication;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class EventPublicationRecovery {
  private static final Logger LOGGER = LoggerFactory.getLogger(EventPublicationRecovery.class);
  private static final long RECOVERY_LOCK = 6_577_669_741L;

  private final IncompleteEventPublications publications;
  private final CompletedEventPublications completedPublications;
  private final EventPublicationProperties properties;
  private final JdbcClient jdbc;
  private final MeterRegistry meters;
  private volatile boolean ready;

  EventPublicationRecovery(
      IncompleteEventPublications publications,
      CompletedEventPublications completedPublications,
      EventPublicationProperties properties,
      JdbcClient jdbc,
      MeterRegistry meters) {
    this.publications = publications;
    this.completedPublications = completedPublications;
    this.properties = properties;
    this.jdbc = jdbc;
    this.meters = meters;
  }

  @EventListener(ApplicationReadyEvent.class)
  void applicationReady() {
    ready = true;
  }

  @Scheduled(
      initialDelayString = "${mjga.events.initial-delay}",
      fixedDelayString = "${mjga.events.scan-interval}")
  @Transactional
  void recover() {
    if (!ready) {
      return;
    }
    boolean claimed =
        jdbc.sql("select pg_try_advisory_xact_lock(:lock)")
            .param("lock", RECOVERY_LOCK)
            .query(Boolean.class)
            .single();
    if (!claimed) {
      meters.counter("mjga.events.recovery.runs", "outcome", "contended").increment();
      return;
    }
    int candidates =
        jdbc.sql(
                "select count(*) from mjga_platform.event_publication where completion_date is null")
            .query(Integer.class)
            .single();
    publications.resubmitIncompletePublications(
        ResubmissionOptions.defaults()
            .withMinAge(properties.minimumAge())
            .withBatchSize(properties.batchSize())
            .withMaxInFlight(properties.concurrency())
            .withFilter(this::eligible));
    completedPublications.deletePublicationsOlderThan(properties.completedRetention());
    jdbc.sql(
            "delete from mjga_platform.event_publication_audit where created_at < now() - cast(:retention as interval)")
        .param("retention", properties.auditRetention().toSeconds() + " seconds")
        .update();
    meters.counter("mjga.events.recovery.runs", "outcome", "completed").increment();
    meters
        .summary("mjga.events.recovery.batch")
        .record(Math.min(candidates, properties.batchSize()));
    LOGGER.info(
        "Durable event recovery completed candidates={} batchLimit={}",
        candidates,
        properties.batchSize());
  }

  Duration minimumAge() {
    return properties.minimumAge();
  }

  private boolean eligible(EventPublication publication) {
    int attempts = publication.getCompletionAttempts();
    if (attempts >= properties.maxAttempts()) {
      return false;
    }
    long multiplier = 1L << Math.min(attempts, 20);
    Duration backoff = properties.minimumAge().multipliedBy(multiplier);
    Instant lastAttempt = publication.getLastResubmissionDate();
    Instant backoffOrigin = lastAttempt == null ? publication.getPublicationDate() : lastAttempt;
    return !backoffOrigin.plus(backoff).isAfter(Instant.now());
  }
}
