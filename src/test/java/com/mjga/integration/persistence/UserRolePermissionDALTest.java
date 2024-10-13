package com.mjga.integration.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.generated.tables.Permission.PERMISSION;
import static org.jooq.generated.tables.Role.ROLE;
import static org.jooq.generated.tables.User.USER;

import com.mjga.dto.PageRequestDto;
import com.mjga.dto.urp.PermissionQueryDto;
import com.mjga.dto.urp.RoleQueryDto;
import com.mjga.dto.urp.UserQueryDto;
import com.mjga.repository.*;
import java.util.List;
import java.util.stream.Collectors;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.generated.tables.pojos.Permission;
import org.jooq.generated.tables.pojos.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

public class UserRolePermissionDALTest extends AbstractDataAccessLayerTest {

  @Autowired private UserRoleMapRepository userRoleMapRepository;

  @Autowired private RolePermissionMapRepository rolePermissionMapRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private RoleRepository roleRepository;

  @Autowired private PermissionRepository permissionRepository;

  @Test
  @Sql(statements = "INSERT INTO mjga.user_role_map (id, user_id, role_id) VALUES (1, 1, 1)")
  void userRoleMap_deleteByUserId() {
    userRoleMapRepository.deleteByUserId(1L);
    assertThat(userRoleMapRepository.fetchByUserId(1L).isEmpty()).isTrue();
  }

  @Test
  @Sql(
      statements =
          "INSERT INTO mjga.role_permission_map (id, role_id, permission_id) VALUES (1, 1, 1)")
  void rolePermissionMap_deleteByRoleId() {
    rolePermissionMapRepository.deleteByRoleId(1L);
    assertThat(rolePermissionMapRepository.fetchByRoleId(1L).isEmpty()).isTrue();
  }

  @Test
  @Sql(
      statements = {
        "INSERT INTO mjga.user (id, username, password) VALUES (1, 'testUserA','5EUX1AIlV09n2o')",
        "INSERT INTO mjga.role (id, code, name) VALUES (1, 'testRoleA', 'testRoleA')",
        "INSERT INTO mjga.user_role_map (id, user_id, role_id) VALUES (1, 1, 1)",
        "INSERT INTO mjga.role_permission_map (id, role_id, permission_id) VALUES (1, 1, 1)",
        "INSERT INTO mjga.role_permission_map (id, role_id, permission_id) VALUES (2, 1, 2)",
        "INSERT INTO mjga.permission (id, code, name) VALUES (1, 'testPermissionA',"
            + " 'testPermissionA')",
        "INSERT INTO mjga.user (id, username,password) VALUES (2, 'testUserB','NTjRCeUq2EqCy')",
        "INSERT INTO mjga.user_role_map (id, user_id, role_id) VALUES (2, 2, 2)",
        "INSERT INTO mjga.role (id, code, name) VALUES (2, 'testRoleB', 'testRoleB')",
        "INSERT INTO mjga.role_permission_map (id, role_id, permission_id) VALUES (3, 2, 2)",
        "INSERT INTO mjga.permission (id, code, name) VALUES (2, 'testPermissionB',"
            + " 'testPermissionB')",
      })
  void user_fetchUniqueUserWithRolePermissionBy() {
    Result<Record> records = userRepository.fetchUniqueUserWithRolePermissionBy(1L);
    assertThat(records.size()).isEqualTo(2);
    assertThat(records.get(0).get(USER.USERNAME)).isEqualTo("testUserA");
    assertThat(records.get(1).get(USER.USERNAME)).isEqualTo("testUserA");
    assertThat(records.get(0).get(ROLE.NAME)).isEqualTo("testRoleA");
    assertThat(records.get(1).get(ROLE.NAME)).isEqualTo("testRoleA");

    List<String> names =
        records.stream().map(record -> record.get(PERMISSION.NAME)).collect(Collectors.toList());
    assertThat(names).containsExactlyInAnyOrder("testPermissionA", "testPermissionB");
  }

  @Test
  @Sql(
      statements = {
        "INSERT INTO mjga.user (id, username, password) VALUES (1, 'testA','5EUX1AIlV09n2o')",
        "INSERT INTO mjga.user (id, username,password) VALUES (2, 'testB','NTjRCeUq2EqCy')",
      })
  void user_pageFetchBy() {
    UserQueryDto rbacQueryDto = new UserQueryDto("test");
    Result<Record> records = userRepository.pageFetchBy(PageRequestDto.of(0, 10), rbacQueryDto);
    assertThat(records.size()).isEqualTo(2);

    assertThat(records.get(0).get(USER.ID)).isEqualTo(1);
    assertThat(records.get(1).get(USER.ID)).isEqualTo(2);
  }

  @Test
  @Sql(
      statements = {
        "INSERT INTO mjga.role (id, code, name) VALUES (1, 'testRoleA', 'testRoleA')",
        "INSERT INTO mjga.role (id, code, name) VALUES (2, 'testRoleB', 'testRoleB')",
      })
  void role_selectByRoleIdIn() {
    List<Role> roles = roleRepository.selectByRoleIdIn(List.of(1L, 2L));
    assertThat(roles.size()).isEqualTo(2);
    assertThat(roles.get(0).getId()).isEqualTo(1L);
    assertThat(roles.get(1).getId()).isEqualTo(2L);
  }

  @Test
  @Sql(
      statements = {
        "INSERT INTO mjga.role (id, code, name) VALUES (1, 'testRoleA', 'testRoleA')",
        "INSERT INTO mjga.role (id, code, name) VALUES (2, 'testRoleB', 'testRoleB')",
      })
  void role_selectByRoleCodeIn() {
    List<Role> roles = roleRepository.selectByRoleCodeIn(List.of("testRoleA", "testRoleB"));
    assertThat(roles.size()).isEqualTo(2);
    assertThat(roles.get(0).getId()).isEqualTo(1L);
    assertThat(roles.get(1).getId()).isEqualTo(2L);
  }

  @Test
  @Sql(
      statements = {
        "INSERT INTO mjga.role (id, code, name) VALUES (1, 'testRoleA', 'testRoleA')",
        "INSERT INTO mjga.role (id, code, name) VALUES (2, 'testRoleB', 'testRoleB')",
      })
  void role_pageFetchBy() {
    RoleQueryDto roleQueryDto = new RoleQueryDto();
    roleQueryDto.setRoleName("testRole");
    Result<Record> records = roleRepository.pageFetchBy(PageRequestDto.of(0, 10), roleQueryDto);
    assertThat(records.get(0).getValue("total_role")).isEqualTo(2);
    assertThat(records.get(0).getValue(ROLE.NAME)).isEqualTo("testRoleA");
    assertThat(records.get(1).getValue(ROLE.NAME)).isEqualTo("testRoleB");

    roleQueryDto = new RoleQueryDto();
    roleQueryDto.setRoleCode("testRoleA");
    records = roleRepository.pageFetchBy(PageRequestDto.of(0, 10), roleQueryDto);
    assertThat(records.get(0).getValue("total_role")).isEqualTo(1);
    assertThat(records.get(0).getValue(ROLE.NAME)).isEqualTo("testRoleA");

    roleQueryDto = new RoleQueryDto();
    roleQueryDto.setRoleName("test");
    roleQueryDto.setRoleCode("testRoleA");
    records = roleRepository.pageFetchBy(PageRequestDto.of(0, 10), roleQueryDto);
    assertThat(records.get(0).getValue("total_role")).isEqualTo(1);
    assertThat(records.get(0).getValue(ROLE.NAME)).isEqualTo("testRoleA");
  }

  @Test
  @Sql(
      statements = {
        "INSERT INTO mjga.role (id, code, name) VALUES (1, 'testRoleA', 'testRoleA')",
        "INSERT INTO mjga.role_permission_map (id, role_id, permission_id) VALUES (1, 1, 1)",
        "INSERT INTO mjga.role_permission_map (id, role_id, permission_id) VALUES (2, 1, 2)",
        "INSERT INTO mjga.permission (id, code, name) VALUES (1, 'testPermissionA',"
            + " 'testPermissionA')",
        "INSERT INTO mjga.role (id, code, name) VALUES (2, 'testRoleB', 'testRoleB')",
        "INSERT INTO mjga.role_permission_map (id, role_id, permission_id) VALUES (3, 2, 2)",
        "INSERT INTO mjga.permission (id, code, name) VALUES (2, 'testPermissionB',"
            + " 'testPermissionB')",
      })
  void role_fetchUniqueRoleWithPermission() {
    Result<Record> records = roleRepository.fetchUniqueRoleWithPermission(1L);
    assertThat(records.size()).isEqualTo(2L);
    assertThat(records.get(0).getValue(ROLE.NAME)).isEqualTo("testRoleA");
    assertThat(records.get(1).getValue(ROLE.NAME)).isEqualTo("testRoleA");
    assertThat(records.get(0).getValue(PERMISSION.NAME)).isEqualTo("testPermissionA");
    assertThat(records.get(1).getValue(PERMISSION.NAME)).isEqualTo("testPermissionB");
  }

  @Test
  @Sql(
      statements = {
        "INSERT INTO mjga.permission (id, code, name) VALUES (1, 'testPermissionA',"
            + " 'testPermissionA')",
        "INSERT INTO mjga.permission (id, code, name) VALUES (2, 'testPermissionB',"
            + " 'testPermissionB')",
      })
  void permission_selectByPermissionIdIn() {
    List<Permission> permissions = permissionRepository.selectByPermissionIdIn(List.of(1L));
    assertThat(permissions.size()).isEqualTo(1);
    assertThat(permissions.get(0).getId()).isEqualTo(1L);
  }

  @Test
  @Sql(
      statements = {
        "INSERT INTO mjga.permission (id, code, name) VALUES (1, 'testPermissionA',"
            + " 'testPermissionA')",
        "INSERT INTO mjga.permission (id, code, name) VALUES (2, 'testPermissionB',"
            + " 'testPermissionB')",
      })
  void permission_pageFetchBy() {
    PermissionQueryDto permissionQueryDto = new PermissionQueryDto();
    permissionQueryDto.setPermissionCode("testPermissionA");
    permissionQueryDto.setPermissionName("test");
    permissionQueryDto.setPermissionIdList(List.of(1L));

    Result<Record> records =
        permissionRepository.pageFetchBy(PageRequestDto.of(0, 10), permissionQueryDto);
    assertThat(records.get(0).getValue("total_permission")).isEqualTo(1);
    assertThat(records.get(0).getValue(PERMISSION.NAME)).isEqualTo("testPermissionA");
  }
}
