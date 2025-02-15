package com.zl.mjga.user.persistence;

import com.zl.mjga.AbstractSpringModularTest;
import com.zl.mjga.user.UserQueryDto;
import com.zl.mjga.user.domain.model.Fetchers;
import com.zl.mjga.user.domain.model.User;
import com.zl.mjga.user.domain.model.UserFetcher;
import com.zl.mjga.user.domain.repository.UserAggregateRepository;
import com.zl.mjga.user.web.UserPageQueryVm;
import org.babyfish.jimmer.Page;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.jdbc.Sql;

@ApplicationModuleTest(module = "user")
public class UserRolePermissionDALTest extends AbstractSpringModularTest {

  @Autowired private UserAggregateRepository userAggregateRepository;

  @Test
  @Sql(
      statements = {
        "INSERT INTO mjga.user (id, username, password) VALUES (1, 'testUserA','5EUX1AIlV09n2o')",
        "INSERT INTO mjga.role (id, code, name) VALUES (1, 'testRoleA', 'testRoleA')",
        "INSERT INTO mjga.user_role_map (user_id, role_id) VALUES (1, 1)",
        "INSERT INTO mjga.permission (id, code, name) VALUES (1, 'testPermissionA',"
            + " 'testPermissionA')",
        "INSERT INTO mjga.role_permission_map (role_id, permission_id) VALUES (1, 1)",
      })
  void fetchUniqueUser_withRolePermissionByValidId_shouldReturnUniqueUser() {
    Assertions.assertNotNull(userAggregateRepository);
    UserFetcher allScalarFields =
        Fetchers.USER_FETCHER
            .allScalarFields()
            .roles(
                Fetchers.ROLE_FETCHER
                    .allScalarFields()
                    .permissions(Fetchers.PERMISSION_FETCHER.allScalarFields()));
    User user =
        userAggregateRepository.fetchUniqueUserBy(allScalarFields, new UserQueryDto(1L, null));
    Assertions.assertNotNull(user);
    Assertions.assertEquals(1L, user.id());
    Assertions.assertEquals("testRoleA", user.roles().get(0).code());
    Assertions.assertEquals("testPermissionA", user.roles().get(0).permissions().get(0).code());
  }

  @Sql(
      statements = {
        "INSERT INTO mjga.user (id, username, password) VALUES (1, 'testUserA','5EUX1AIlV09n2o')",
        "INSERT INTO mjga.user (id, username,password) VALUES (2, 'testUserB','NTjRCeUq2EqCy')",
        "INSERT INTO mjga.role (id, code, name) VALUES (1, 'testRoleA', 'testRoleA')",
        "INSERT INTO mjga.role (id, code, name) VALUES (2, 'testRoleB', 'testRoleB')",
        "INSERT INTO mjga.permission (id, code, name) VALUES (1, 'testPermissionA',"
            + " 'testPermissionA')",
        "INSERT INTO mjga.permission (id, code, name) VALUES (2, 'testPermissionB',"
            + " 'testPermissionB')",
        "INSERT INTO mjga.user_role_map (user_id, role_id) VALUES (1, 1)",
        "INSERT INTO mjga.role_permission_map (role_id, permission_id) VALUES (1, 1)",
        "INSERT INTO mjga.role_permission_map (role_id, permission_id) VALUES (1, 2)",
        "INSERT INTO mjga.user_role_map (user_id, role_id) VALUES (2, 2)",
        "INSERT INTO mjga.role_permission_map (role_id, permission_id) VALUES (2, 2)",
      })
  @Test
  void pageQueryUserAggregate_withSpecifyPageParam_shouldReturnExpectPageResult() {
    UserPageQueryVm userPageQueryVm = new UserPageQueryVm(null, true);
    UserFetcher allScalarFields =
        Fetchers.USER_FETCHER
            .allScalarFields()
            .roles(
                Fetchers.ROLE_FETCHER
                    .allScalarFields()
                    .permissions(Fetchers.PERMISSION_FETCHER.allScalarFields()));
    Page<User> userPage =
        userAggregateRepository.fetchAggregateWithPageBy(allScalarFields, userPageQueryVm, 1, 10);
    Assertions.assertEquals(2, userPage.getTotalRowCount());
    Assertions.assertEquals(1, userPage.getTotalPageCount());

    Page<User> userPage2 =
        userAggregateRepository.fetchAggregateWithPageBy(allScalarFields, userPageQueryVm, 1, 1);
    Assertions.assertEquals(2, userPage2.getTotalRowCount());
    Assertions.assertEquals(2, userPage2.getTotalPageCount());
  }
}
