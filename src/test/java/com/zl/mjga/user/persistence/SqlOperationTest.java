package com.zl.mjga.user.persistence;

import com.zl.mjga.AbstractSpringModularTest;
import com.zl.mjga.user.UserRolePermissionView;
import com.zl.mjga.user.UserRoleShortInput;
import com.zl.mjga.user.domain.model.UserTable;
import java.util.List;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.mutation.SaveMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.jdbc.Sql;

@ApplicationModuleTest(module = "user")
public class SqlOperationTest extends AbstractSpringModularTest {

  private final JSqlClient sqlClient;

  public SqlOperationTest(JSqlClient sqlClient) {
    this.sqlClient = sqlClient;
  }

  @Test
  @Sql(
      statements = {
        "INSERT INTO mjga.role (id, code, name) VALUES (1, 'testRoleA', 'testRoleA')",
      })
  public void testShortSaveMode() {
    UserRoleShortInput userRoleInputShort = new UserRoleShortInput();
    userRoleInputShort.setUsername("testUserA");
    userRoleInputShort.setPassword("5EUX1AIlV09n2o");
    userRoleInputShort.setRolesId(List.of(1L));
    sqlClient.save(userRoleInputShort, SaveMode.INSERT_ONLY);

    UserTable table = UserTable.$;
    UserRolePermissionView userRolePermissionView =
        sqlClient
            .createQuery(table)
            .where(table.username().eq("testUserA"))
            .select(table.fetch(UserRolePermissionView.class))
            .fetchOne();
    Assertions.assertEquals("testUserA", userRolePermissionView.getUsername());
    Assertions.assertEquals("5EUX1AIlV09n2o", userRolePermissionView.getPassword());
    Assertions.assertEquals("testRoleA", userRolePermissionView.getRoles().get(0).getCode());
  }
}
