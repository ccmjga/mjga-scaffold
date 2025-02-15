package com.zl.mjga.user.persistence;

import com.zl.mjga.AbstractSpringModularTest;
import com.zl.mjga.user.RolePermissionShortInput;
import com.zl.mjga.user.domain.model.Fetchers;
import com.zl.mjga.user.domain.model.Role;
import com.zl.mjga.user.domain.model.RoleFetcher;
import com.zl.mjga.user.domain.repository.RoleRepository;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.jdbc.Sql;

@ApplicationModuleTest(module = "user")
public class RoleDALTest extends AbstractSpringModularTest {

  @Autowired private RoleRepository roleRepository;

  @Test
  @Sql(
      statements = {
        "INSERT INTO mjga.role (id, code, name) VALUES (1, 'testRoleA', 'testRoleA')",
        "INSERT INTO mjga.permission (id, code, name) VALUES (1, 'testPermissionA',"
            + " 'testPermissionA')",
        "INSERT INTO mjga.role_permission_map (role_id, permission_id) VALUES (1, 1)",
      })
  void fetchRoleComplexBy_withFetcher_shouldReturnComplexRoleByFetcher() {
    RoleFetcher roleFetcher =
        Fetchers.ROLE_FETCHER
            .allScalarFields()
            .permissions(Fetchers.PERMISSION_FETCHER.allScalarFields());
    List<Role> all = roleRepository.fetchRoleComplexBy(roleFetcher);
    Assertions.assertNotNull(all);
    Assertions.assertFalse(all.isEmpty());
    Assertions.assertEquals(1, all.size());
    String permissionCode = all.get(0).permissions().get(0).code();
    Assertions.assertEquals("testPermissionA", permissionCode);
  }

  @Test
  void upsert_withNonExistRole_shouldAppendSuccess() {
    RolePermissionShortInput mockRole =
        new RolePermissionShortInput.Builder().code("testRoleA").name("testRoleA").build();
    roleRepository.upsert(mockRole);
    Role role = roleRepository.findByCode("testRoleA");
    Assertions.assertEquals("testRoleA", role.code());
  }

  @Sql(
      statements = {
        "INSERT INTO mjga.role (id, code, name) VALUES (1, 'testRoleA', 'testRoleA')",
        "INSERT INTO mjga.permission (id, code, name) VALUES (1, 'testPermissionA',"
            + " 'testPermissionA')",
        "INSERT INTO mjga.role_permission_map (role_id, permission_id) VALUES (1, 1)",
      })
  @Test
  void upsert_withExistRole_shouldUpdateSuccess() {
    RolePermissionShortInput mockRole =
        new RolePermissionShortInput.Builder()
            .code("testRoleA")
            .name("name")
            .permissionIds(null)
            .build();
    roleRepository.upsert(mockRole);
    RoleFetcher roleFetcher = RoleFetcher.$.allScalarFields().permissions().allScalarFields();
    List<Role> roles = roleRepository.fetchRoleComplexBy(roleFetcher);
    Role role = roles.get(0);
    Assertions.assertEquals("name", role.name());
    Assertions.assertEquals(0, role.permissions().size());
  }
}
