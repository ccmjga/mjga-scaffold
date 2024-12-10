package com.zl.mjga.integration.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.generated.mjga.tables.User.USER;
import static org.jooq.impl.DSL.asterisk;

import java.util.List;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.generated.mjga.tables.pojos.User;
import org.jooq.generated.mjga.tables.records.UserRecord;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

public class JooqTutorialsTest extends AbstractDataAccessLayerTest {

  @Autowired private DSLContext dsl;

  @Test
  @Sql(
      statements = {
        "INSERT INTO mjga.user (id, username, password) VALUES (1, 'testUserA','5EUX1AIlV09n2o')",
        "INSERT INTO mjga.user (id, username, password) VALUES (2, 'testUserB','lbHHwHjTzpOiRHTs')"
      })
  void queryWithDsl() {
    List<User> users = dsl.selectFrom(USER).fetchInto(User.class);
    assertThat(users.size()).isEqualTo(2);
    assertThat(users.get(0).getUsername()).isEqualTo("testUserA");
    assertThat(users.get(1).getUsername()).isEqualTo("testUserB");
  }

  @Test
  void insertWithOrmFeel() {
    UserRecord userRecord = dsl.newRecord(USER);
    userRecord.setUsername("9hrb5Fv@gmail.com");
    userRecord.setPassword("falr2b9nCVY5hS1o");
    userRecord.store();
    UserRecord fetchedOne = dsl.fetchOne(USER, USER.USERNAME.eq("9hrb5Fv@gmail.com"));
    assertThat(fetchedOne.getPassword()).isEqualTo("falr2b9nCVY5hS1o");
  }

  @Test
  void updateWithOrmFeel() {
    UserRecord userRecord = dsl.newRecord(USER);
    userRecord.setUsername("9hrb5Fv@gmail.com");
    userRecord.setPassword("falr2b9nCVY5hS1o");
    userRecord.store();

    UserRecord fetchedOne = dsl.fetchOne(USER, USER.USERNAME.eq("9hrb5Fv@gmail.com"));
    assertThat(fetchedOne.getPassword()).isEqualTo("falr2b9nCVY5hS1o");

    userRecord.setPassword("JHMDoQPKuEcgILE6");
    userRecord.store();

    fetchedOne.refresh();
    assertThat(fetchedOne.getPassword()).isEqualTo("JHMDoQPKuEcgILE6");
  }

  @Test
  @Sql(
      statements = {
        "INSERT INTO mjga.user (id, username, password) VALUES (1, 'testUserA','5EUX1AIlV09n2o')",
        "INSERT INTO mjga.user (id, username, password) VALUES (2, 'testUserB','lbHHwHjTzpOiRHTs')"
      })
  void deleteWithOrmFeel() {
    UserRecord userRecord1 = dsl.fetchOne(USER, USER.USERNAME.eq("testUserA"));
    assertThat(userRecord1.get(USER.USERNAME)).isEqualTo("testUserA");
    userRecord1.delete();
    UserRecord userRecord2 = dsl.fetchOne(USER, USER.USERNAME.eq("testUserA"));
    assertThat(userRecord2).isNull();
  }

  @Test
  @Sql(
      statements = {
        "INSERT INTO mjga.user (id, username, password) VALUES (1, 'testUserA','5EUX1AIlV09n2o')",
        "INSERT INTO mjga.user (id, username, password) VALUES (2, 'testUserB','lbHHwHjTzpOiRHTs')",
        "INSERT INTO mjga.user (id, username, password) VALUES (3, 'testUserC','yF25WscLYmA8')",
        "INSERT INTO mjga.user (id, username, password) VALUES (4, 'testUserD','yF25WscLYmA8')",
        "INSERT INTO mjga.user (id, username, password) VALUES (5, 'testUserE','x60FelJjyd0B')"
      })
  void pagingQuery() {
    List<User> users =
        dsl.select(USER.USERNAME).from(USER).limit(2).offset(1).fetchInto(User.class);
    assertThat(users.size()).isEqualTo(2);
    assertThat(users.get(0).getUsername()).isEqualTo("testUserB");
    assertThat(users.get(1).getUsername()).isEqualTo("testUserC");
  }

  @Test
  @Sql(
      statements = {
        "INSERT INTO mjga.user (id, username, password) VALUES (1, 'testUserA','5EUX1AIlV09n2o')",
        "INSERT INTO mjga.user (id, username, password) VALUES (2, 'testUserB','lbHHwHjTzpOiRHTs')",
        "INSERT INTO mjga.user (id, username, password) VALUES (3, 'testUserC','yF25WscLYmA8')",
        "INSERT INTO mjga.user (id, username, password) VALUES (4, 'testUserD','yF25WscLYmA8')",
        "INSERT INTO mjga.user (id, username, password) VALUES (5, 'testUserE','x60FelJjyd0B')"
      })
  void pagingSortQuery() {
    List<User> users =
        dsl.select(USER.USERNAME)
            .from(USER)
            .orderBy(USER.ID.desc())
            .limit(3)
            .offset(1)
            .fetchInto(User.class);
    assertThat(users.size()).isEqualTo(3);
    assertThat(users.get(0).getUsername()).isEqualTo("testUserD");
    assertThat(users.get(1).getUsername()).isEqualTo("testUserC");
  }

  @Test
  @Sql(
      statements = {
        "INSERT INTO mjga.user (id, username, password) VALUES (1, 'testUserA','a')",
        "INSERT INTO mjga.user (id, username, password) VALUES (2, 'testUserB','b')",
        "INSERT INTO mjga.user (id, username, password) VALUES (3, 'testUserC','c')",
        "INSERT INTO mjga.user (id, username, password) VALUES (4, 'testUserD','c')",
        "INSERT INTO mjga.user (id, username, password) VALUES (5, 'testUserE','c')"
      })
  void fetchAndTiesQuery() {
    List<User> users =
        dsl.select(USER.USERNAME)
            .from(USER)
            .orderBy(USER.PASSWORD.asc())
            .limit(3)
            .withTies()
            .offset(0)
            .fetchInto(User.class);
    assertThat(users.size()).isEqualTo(5);
    assertThat(users.get(0).getUsername()).isEqualTo("testUserA");
    assertThat(users.get(4).getUsername()).isEqualTo("testUserE");
  }

  @Test
  @Sql(
      statements = {
        "INSERT INTO mjga.user (id, username, password) VALUES (1, 'testUserA','a')",
        "INSERT INTO mjga.user (id, username, password) VALUES (2, 'testUserB','b')",
        "INSERT INTO mjga.user (id, username, password) VALUES (3, 'testUserC','c')",
        "INSERT INTO mjga.user (id, username, password) VALUES (4, 'testUserD','e')",
        "INSERT INTO mjga.user (id, username, password) VALUES (5, 'testUserE','f')"
      })
  void windowFunctionQuery() {
    Result<Record> resultWithWindow =
        dsl.select(asterisk(), DSL.count().over().as("total_user"))
            .from(USER)
            .orderBy(USER.ID.asc())
            .limit(4)
            .offset(0)
            .fetch();
    assertThat(resultWithWindow.size()).isEqualTo(4);
    assertThat(resultWithWindow.get(0).getValue("total_user")).isEqualTo(5);
    assertThat(resultWithWindow.get(0).getValue(USER.USERNAME)).isEqualTo("testUserA");
    assertThat(resultWithWindow.get(1).getValue(USER.USERNAME)).isEqualTo("testUserB");
  }
}
