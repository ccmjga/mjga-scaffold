package com.zl.mjga.platform.events;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class JdbcEventPublicationOperations implements EventPublicationOperations {

  private final JdbcClient jdbc;
  private final IncompleteEventPublications publications;
  private final EventPublicationProperties properties;

  JdbcEventPublicationOperations(
      JdbcClient jdbc,
      IncompleteEventPublications publications,
      EventPublicationProperties properties) {
    this.jdbc = jdbc;
    this.publications = publications;
    this.properties = properties;
  }

  @Override
  @Transactional(readOnly = true)
  public List<PublicationSummary> incomplete(int limit) {
    int bounded = Math.min(Math.max(limit, 1), properties.managementBatchLimit());
    return jdbc.sql(
            """
            select id, listener_id, event_type, coalesce(completion_attempts, 0), coalesce(status, 'INCOMPLETE')
            from mjga_platform.event_publication
            where completion_date is null
            order by publication_date
            limit :limit
            """)
        .param("limit", bounded)
        .query(JdbcEventPublicationOperations::summary)
        .list();
  }

  @Override
  public RecoveryOutcome resubmit(Duration minimumAge, int limit, InvocationContext context) {
    int bounded = Math.min(Math.max(limit, 1), properties.managementBatchLimit());
    List<PublicationSummary> selected =
        jdbc.sql(
                """
				select id, listener_id, event_type, coalesce(completion_attempts, 0), coalesce(status, 'INCOMPLETE')
				from mjga_platform.event_publication
				where completion_date is null
				  and publication_date < now() - cast(:minimumAge as interval)
				order by publication_date
				limit :limit
				""")
            .param("minimumAge", minimumAge.toSeconds() + " seconds")
            .param("limit", bounded)
            .query(JdbcEventPublicationOperations::summary)
            .list();
    publications.resubmitIncompletePublications(
        ResubmissionOptions.defaults().withMinAge(minimumAge).withBatchSize(bounded));
    selected.forEach(
        publication ->
            audit(
                "RESUBMIT",
                publication,
                publication.status(),
                publication.status(),
                "manual resubmission",
                context));
    return new RecoveryOutcome(selected.size(), "RESUBMITTED");
  }

  @Override
  @Transactional
  public RecoveryOutcome skip(UUID publicationId, String reason, InvocationContext context) {
    if (reason == null || reason.isBlank()) {
      throw new IllegalArgumentException("Skip reason is required");
    }
    PublicationSummary selected =
        incomplete(properties.managementBatchLimit()).stream()
            .filter(candidate -> candidate.publicationId().equals(publicationId))
            .findFirst()
            .orElse(null);
    int affected =
        jdbc.sql(
                """
                update mjga_platform.event_publication
                set completion_date = now(), status = 'SKIPPED'
                where id = :id and completion_date is null
                """)
            .param("id", publicationId)
            .update();
    if (affected == 1 && selected != null) {
      audit("SKIP", selected, selected.status(), "SKIPPED", reason, context);
    }
    return new RecoveryOutcome(affected, affected == 1 ? "SKIPPED" : "NOT_FOUND");
  }

  private void audit(
      String action,
      PublicationSummary publication,
      String beforeStatus,
      String afterStatus,
      String reason,
      InvocationContext invocationContext) {
    jdbc.sql(
            """
            insert into mjga_platform.event_publication_audit
              (id, publication_id, listener_id, event_type, action, operator_id, reason,
               before_status, after_status, correlation_id, created_at)
            values (:id, :publicationId, :listenerId, :eventType, :action, :operator, :reason,
                    :beforeStatus, :afterStatus, :correlation, now())
            """)
        .param("id", UUID.randomUUID())
        .param("publicationId", publication.publicationId())
        .param("listenerId", publication.listenerId())
        .param("eventType", publication.eventType())
        .param("action", action)
        .param("operator", invocationContext.actor())
        .param("reason", reason)
        .param("beforeStatus", beforeStatus)
        .param("afterStatus", afterStatus)
        .param("correlation", invocationContext.correlationId())
        .update();
  }

  @SuppressWarnings("PMD.UnusedFormalParameter")
  private static PublicationSummary summary(ResultSet row, int rowNumber) throws SQLException {
    return new PublicationSummary(
        row.getObject(1, UUID.class),
        row.getString(2),
        row.getString(3),
        row.getInt(4),
        row.getString(5));
  }
}
