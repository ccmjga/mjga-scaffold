package com.zl.mjga.integration.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.generated.mjga.tables.Permission.PERMISSION;
import static org.jooq.generated.mjga.tables.Role.ROLE;
import static org.jooq.generated.mjga.tables.User.USER;

import com.zl.mjga.dto.PageRequestDto;
import com.zl.mjga.dto.urp.PermissionQueryDto;
import com.zl.mjga.dto.urp.RoleQueryDto;
import com.zl.mjga.dto.urp.UserQueryDto;
import com.zl.mjga.dto.urp.UserRolePermissionDto;
import com.zl.mjga.repository.*;
import java.util.List;
import java.util.stream.Collectors;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.generated.mjga.tables.pojos.Permission;
import org.jooq.generated.mjga.tables.pojos.Role;
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
  @Sql(
      statements = {
        "INSERT INTO mjga.user (id, username, password) VALUES (1, 'testUserA','5EUX1AIlV09n2o')",
        "INSERT INTO mjga.role (id, code, name) VALUES (1, 'testRoleA', 'testRoleA')",
        "INSERT INTO mjga.user_role_map (user_id, role_id) VALUES (1, 1)"
      })
  void userRoleMap_deleteByUserId() {
    userRoleMapRepository.deleteByUserId(1L);
    assertThat(userRoleMapRepository.fetchByUserId(1L).isEmpty()).isTrue();
  }

  @Test
  @Sql(
      statements = {
        "INSERT INTO mjga.user (id, username, password) VALUES (1, 'testUserA','5EUX1AIlV09n2o')",
        "INSERT INTO mjga.role (id, code, name) VALUES (1, 'testRoleA', 'testRoleA')",
        "INSERT INTO mjga.permission (id, code, name) VALUES (1, 'testPermissionA',"
            + " 'testPermissionA')",
        "INSERT INTO mjga.role_permission_map (role_id, permission_id) VALUES (1, 1)",
      })
  void rolePermissionMap_deleteByRoleId() {
    rolePermissionMapRepository.deleteByRoleId(1L);
    assertThat(rolePermissionMapRepository.fetchByRoleId(1L).isEmpty()).isTrue();
  }

  @Test
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
  void getUserFlatBy() {
    Result<Record> records = userRepository.getUserFlatBy(1L);
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
  void pageFetchUserAggBy() {
    PageRequestDto pageRequestDto = new PageRequestDto();
    pageRequestDto.setPage(0);
    pageRequestDto.setSize(10);
    Result<Record> records = userRepository.pageFetchUserAggBy(pageRequestDto, new UserQueryDto());
    assertThat(records.size()).isEqualTo(2);
    assertThat(records.get(0).get(USER.USERNAME)).isEqualTo("testUserA");
    assertThat(records.get(1).get(USER.USERNAME)).isEqualTo("testUserB");
    assertThat(records.get(0).get("roles", List.class).size()).isEqualTo(2);
    List<UserRolePermissionDto> result = records.into(UserRolePermissionDto.class);
    assertThat(result.get(0).getRoles().get(0).getName()).isEqualTo("testRoleA");
    assertThat(result.get(0).getRoles().get(1).getName()).isEqualTo("testRoleB");
    assertThat(result.get(0).getRoles().get(0).getPermissions().get(0).getName())
        .isEqualTo("testPermissionA");
  }

  @Test
  @Sql(
      statements = {
        "INSERT INTO mjga.user (id, username, password) VALUES (1, 'testA','5EUX1AIlV09n2o')",
        "INSERT INTO mjga.user (id, username,password) VALUES (2, 'testB','NTjRCeUq2EqCy')",
      })
  void user_pageFetchBy() {
    UserQueryDto rbacQueryDto = new UserQueryDto("test");
    Result<Record> records = userRepository.pageFetchUserBy(PageRequestDto.of(0, 10), rbacQueryDto);
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
    Result<Record> records =
        roleRepository.pageFetchRolesBy(PageRequestDto.of(0, 10), roleQueryDto);
    assertThat(records.get(0).getValue("total_role")).isEqualTo(2);
    assertThat(records.get(0).getValue(ROLE.NAME)).isEqualTo("testRoleA");
    assertThat(records.get(1).getValue(ROLE.NAME)).isEqualTo("testRoleB");

    roleQueryDto = new RoleQueryDto();
    roleQueryDto.setRoleCode("testRoleA");
    records = roleRepository.pageFetchRolesBy(PageRequestDto.of(0, 10), roleQueryDto);
    assertThat(records.get(0).getValue("total_role")).isEqualTo(1);
    assertThat(records.get(0).getValue(ROLE.NAME)).isEqualTo("testRoleA");

    roleQueryDto = new RoleQueryDto();
    roleQueryDto.setRoleName("test");
    roleQueryDto.setRoleCode("testRoleA");
    records = roleRepository.pageFetchRolesBy(PageRequestDto.of(0, 10), roleQueryDto);
    assertThat(records.get(0).getValue("total_role")).isEqualTo(1);
    assertThat(records.get(0).getValue(ROLE.NAME)).isEqualTo("testRoleA");
  }

  @Test
  @Sql(
      statements = {
        "INSERT INTO mjga.role (id, code, name) VALUES (1, 'testRoleA', 'testRoleA')",
        "INSERT INTO mjga.role (id, code, name) VALUES (2, 'testRoleB', 'testRoleB')",
        "INSERT INTO mjga.permission (id, code, name) VALUES (1, 'testPermissionA',"
            + " 'testPermissionA')",
        "INSERT INTO mjga.permission (id, code, name) VALUES (2, 'testPermissionB',"
            + " 'testPermissionB')",
        "INSERT INTO mjga.role_permission_map (role_id, permission_id) VALUES (1, 1)",
        "INSERT INTO mjga.role_permission_map (role_id, permission_id) VALUES (1, 2)",
        "INSERT INTO mjga.role_permission_map (role_id, permission_id) VALUES (2, 2)",
      })
  void role_fetchUniqueRoleWithPermission() {
    Result<Record> records = roleRepository.getRoleAggBy(1L);
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
        permissionRepository.pageFetchPermissionBy(PageRequestDto.of(0, 10), permissionQueryDto);
    assertThat(records.get(0).getValue("total_permission")).isEqualTo(1);
    assertThat(records.get(0).getValue(PERMISSION.NAME)).isEqualTo("testPermissionA");
  }
}
