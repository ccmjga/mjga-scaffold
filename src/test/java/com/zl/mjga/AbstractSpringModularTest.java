package com.zl.mjga;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Transactional(rollbackFor = Throwable.class)
@Rollback
@ImportAutoConfiguration(exclude = {RabbitAutoConfiguration.class, RedisAutoConfiguration.class})
public class AbstractSpringModularTest {

  public static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:17.2-alpine").withDatabaseName("mjga");

  @DynamicPropertySource
  static void postgresProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.flyway.locations", () -> "classpath:db/migration/test");
    registry.add("spring.flyway.default-schema", () -> "public");
    registry.add("spring.modulith.events.jdbc.schema", () -> "public");
  }

  static {
    postgres.start();
  }
}
