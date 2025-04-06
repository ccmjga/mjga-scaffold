package com.zl.mjga.integration.persistence;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScans({@ComponentScan("jooq.tables.daos"), @ComponentScan("com.zl.mjga.repository")})
@Testcontainers
public class AbstractDataAccessLayerTest {

  public static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:17.3-alpine").withDatabaseName("mjga");

  @DynamicPropertySource
  static void postgresProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.flyway.locations", () -> "classpath:db/migration/test");
    registry.add("spring.flyway.default-schema", () -> "public");
  }

  static {
    postgres.start();
  }
}
