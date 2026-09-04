package com.zl.mjga.platform.events

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.modulith.events.IncompleteEventPublications
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import javax.sql.DataSource

@Tag("integration")
@Testcontainers(disabledWithoutDocker = true)
class EventPublicationRecoveryTest {
    @Test
    fun `incomplete publication is recovered without exposing management operations`() {
        val dataSource = dataSource()
        Flyway
            .configure()
            .dataSource(dataSource)
            .locations("classpath:db/platform")
            .load()
            .migrate()
        val jdbc = JdbcClient.create(dataSource)
        val publicationId = UUID.randomUUID()
        jdbc
            .sql(
                """insert into mjga_platform.event_publication
               (id, listener_id, event_type, serialized_event, publication_date, status, completion_attempts)
               values (:id, 'verification-listener', 'verification.event.v1', '{}', :published, 'INCOMPLETE', 0)""",
            ).param("id", publicationId)
            .param("published", OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(5))
            .update()
        val operations: EventPublicationOperations =
            JdbcEventPublicationOperations(
                jdbc,
                mockk<IncompleteEventPublications>(),
                properties(),
            )
        assertThat(operations.incomplete(10).map { it.publicationId }).containsExactly(publicationId)
        assertThat(
            operations
                .skip(
                    publicationId,
                    "verified operator recovery",
                    InvocationContext(
                        "verification-operator",
                        null,
                        "correlation-1",
                        null,
                        InvocationContext.InvocationKind.SYSTEM,
                    ),
                ).outcome,
        ).isEqualTo("SKIPPED")
        assertThat(
            jdbc
                .sql("select count(*) from mjga_platform.event_publication_audit where publication_id = :id")
                .param("id", publicationId)
                .query(Int::class.java)
                .single(),
        ).isEqualTo(1)
    }

    private fun dataSource(): DataSource =
        DriverManagerDataSource().apply {
            setUrl(postgres.jdbcUrl)
            username = postgres.username
            password = postgres.password
        }

    private fun properties() =
        EventPublicationProperties(
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
            100,
        )

    companion object {
        @Container
        @JvmStatic
        val postgres =
            PostgreSQLContainer(
                DockerImageName
                    .parse("postgres:18.6@sha256:4ef4dbc939d61acea57712655ddb4b4ab27419c913f94cca0cd57cb3ea3c2280")
                    .asCompatibleSubstituteFor("postgres"),
            ).withDatabaseName("contract_first")
    }
}
