package com.zl.mjga.platform.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Tag("integration")
@Testcontainers(disabledWithoutDocker = true)
class EventPublicationRecoveryTest {

  @Container
  static final PostgreSQLContainer postgres =
      new PostgreSQLContainer(
              DockerImageName.parse(
                      "postgres:18.6@sha256:4ef4dbc939d61acea57712655ddb4b4ab27419c913f94cca0cd57cb3ea3c2280")
                  .asCompatibleSubstituteFor("postgres"))
          .withDatabaseName("contract_first");

  @Test
  void incompletePublicationIsRecoveredWithoutExposingManagementOperations() {
    DataSource dataSource = dataSource();
    Flyway.configure().dataSource(dataSource).locations("classpath:db/platform").load().migrate();
    JdbcClient jdbc = JdbcClient.create(dataSource);
    UUID publicationId = UUID.randomUUID();
    jdbc.sql(
            """
            insert into mjga_platform.event_publication
              (id, listener_id, event_type, serialized_event, publication_date, status, completion_attempts)
            values (:id, 'verification-listener', 'verification.event.v1', '{}', :published, 'INCOMPLETE', 0)
            """)
        .param("id", publicationId)
        .param("published", OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(5))
        .update();

    EventPublicationOperations operations =
        new JdbcEventPublicationOperations(
            jdbc,
            mock(IncompleteEventPublications.class),
            new EventPublicationProperties(
                false,
                null,
                null,
                Duration.ofSeconds(1),
                Duration.ofSeconds(1),
                100,
                4,
                10,
                Duration.ofSeconds(1),
                Duration.ofDays(30),
                Duration.ofDays(365),
                100));

    assertThat(operations.incomplete(10))
        .extracting(EventPublicationOperations.PublicationSummary::publicationId)
        .containsExactly(publicationId);
    assertThat(
            operations.skip(
                publicationId,
                "verified operator recovery",
                new InvocationContext(
                    "verification-operator",
                    Optional.empty(),
                    "correlation-1",
                    Optional.empty(),
                    InvocationContext.InvocationKind.SYSTEM)))
        .extracting(EventPublicationOperations.RecoveryOutcome::outcome)
        .isEqualTo("SKIPPED");
    assertThat(
            jdbc.sql(
                    "select count(*) from mjga_platform.event_publication_audit where publication_id = :id")
                .param("id", publicationId)
                .query(Integer.class)
                .single())
        .isEqualTo(1);
    assertThat(
            System.getProperty("management.endpoints.web.exposure.include", "health,info,metrics"))
        .doesNotContain("eventPublications");
  }

  private static DataSource dataSource() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setUrl(postgres.getJdbcUrl());
    dataSource.setUsername(postgres.getUsername());
    dataSource.setPassword(postgres.getPassword());
    return dataSource;
  }
}
