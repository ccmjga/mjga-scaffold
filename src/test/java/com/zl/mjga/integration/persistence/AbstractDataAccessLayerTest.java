package com.zl.mjga.integration.persistence;

import java.nio.file.Paths;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;

@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScans({@ComponentScan("jooq.tables.daos"), @ComponentScan("com.zl.mjga.repository")})
public class AbstractDataAccessLayerTest {

  public static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16.4-alpine")
          .withDatabaseName("postgres")
          .withFileSystemBind(
              Paths.get("db.d/init/01-init.sql").toAbsolutePath().toString(),
              "/docker-entrypoint-initdb.d/01-init.sql",
              BindMode.READ_ONLY);

  @DynamicPropertySource
  static void postgresProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.docker.compose.skip.in-tests", () -> "true");
  }

  static {
    postgres.start();
  }
}
