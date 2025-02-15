package com.zl.mjga.user.persistence;

import com.zl.mjga.AbstractSpringModularTest;
import com.zl.mjga.user.PermissionInput;
import com.zl.mjga.user.domain.repository.PermissionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.jdbc.Sql;

@ApplicationModuleTest(module = "user")
public class PermissionDALTest extends AbstractSpringModularTest {

  @Autowired private PermissionRepository permissionRepository;

  @Test
  void upsert_withNonExistDto_shouldSaveSucceed() {
    PermissionInput mockInput = new PermissionInput.Builder().code("code").name("name").build();
    permissionRepository.upsert(mockInput);
    Assertions.assertEquals(mockInput.getCode(), permissionRepository.findByCode("code").code());
  }

  @Test
  @Sql(
      statements = {
        "INSERT INTO mjga.permission (code, name) VALUES ('testRoleA', 'testRoleA')",
      })
  void upsert_withExistDto_shouldUpdateSucceed() {
    PermissionInput mockInput =
        new PermissionInput.Builder().code("testRoleA").name("testRoleAName").build();
    permissionRepository.upsert(mockInput);
    Assertions.assertEquals(
        mockInput.getName(), permissionRepository.findByCode("testRoleA").name());
  }
}
