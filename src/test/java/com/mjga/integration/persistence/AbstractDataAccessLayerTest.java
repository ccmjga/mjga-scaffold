package com.mjga.integration.persistence;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@TestPropertySource(
    properties = {
      "spring.datasource.url=jdbc:tc:postgresql:15.4-alpine:///postgres?TC_INITSCRIPT=file:db.d/init/01-init.sql",
      "spring.docker.compose.skip.in-tests=true",
    })
@ComponentScans({@ComponentScan("jooq.tables.daos"), @ComponentScan("com.mjga.repository")})
public class AbstractDataAccessLayerTest {}
