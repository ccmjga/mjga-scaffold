package com.mjga.integration.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.generated.Tables.USER;

import java.util.List;
import org.jooq.DSLContext;
import org.jooq.generated.tables.pojos.User;
import org.jooq.generated.tables.records.UserRecord;
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
}
