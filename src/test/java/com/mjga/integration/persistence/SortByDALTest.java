package com.mjga.integration.persistence;

import static jooq.tables.User.USER;
import static org.assertj.core.api.Assertions.assertThat;

import com.mjga.dto.PageRequestDto;
import com.mjga.dto.urp.UserQueryDto;
import com.mjga.repository.*;
import java.util.HashMap;
import org.jooq.Record;
import org.jooq.Result;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;


public class SortByDALTest extends AbstractDataAccessLayerTest {
  @Autowired private UserRoleMapRepository userRoleMapRepository;

  @Autowired private RolePermissionMapRepository rolePermissionMapRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private RoleRepository roleRepository;

  @Autowired private PermissionRepository permissionRepository;

  @Test
  @Sql(
      statements = {
        "INSERT INTO mjga.user (id, username, password) VALUES (1, 'testA','5EUX1AIlV09n2o')",
        "INSERT INTO mjga.user (id, username,password) VALUES (2, 'testB','NTjRCeUq2EqCy')",
        "INSERT INTO mjga.user (id, username,password) VALUES (3, 'testC','qFVVFvPqs291k10')",
      })
  void userPageFetchWithNoSort() {
    UserQueryDto rbacQueryDto = new UserQueryDto("test");
    Result<Record> records = userRepository.pageFetchBy(PageRequestDto.of(0, 10), rbacQueryDto);
    assertThat(records.get(0).get(USER.ID)).isEqualTo(1);
    assertThat(records.get(1).get(USER.ID)).isEqualTo(2);
    assertThat(records.get(2).get(USER.ID)).isEqualTo(3);
  }

  @Test
  @Sql(
      statements = {
        "INSERT INTO mjga.user (id, username, password) VALUES (1, 'testA','1')",
        "INSERT INTO mjga.user (id, username,password) VALUES (2, 'testB','2')",
        "INSERT INTO mjga.user (id, username,password) VALUES (3, 'testC','3')",
        "INSERT INTO mjga.user (id, username,password) VALUES (4, 'testD','3')",
      })
  void userPageFetchWithSort() {
    UserQueryDto rbacQueryDto = new UserQueryDto("test");
    HashMap<String, PageRequestDto.Direction> sortByIdDesc = new HashMap<>();
    sortByIdDesc.put("id", PageRequestDto.Direction.DESC);
    Result<Record> records =
        userRepository.pageFetchBy(PageRequestDto.of(0, 10, sortByIdDesc), rbacQueryDto);
    assertThat(records.get(0).get(USER.ID)).isEqualTo(4);
    assertThat(records.get(1).get(USER.ID)).isEqualTo(3);
    assertThat(records.get(2).get(USER.ID)).isEqualTo(2);
    assertThat(records.get(3).get(USER.ID)).isEqualTo(1);

    HashMap<String, PageRequestDto.Direction> sortByPasswordAndId = new HashMap<>();
    sortByPasswordAndId.put("password", PageRequestDto.Direction.DESC);
    sortByIdDesc.put("id", PageRequestDto.Direction.ASC);
    Result<Record> records2 =
        userRepository.pageFetchBy(PageRequestDto.of(0, 10, sortByPasswordAndId), rbacQueryDto);
    assertThat(records2.get(0).get(USER.ID)).isEqualTo(3);
    assertThat(records2.get(1).get(USER.ID)).isEqualTo(4);
    assertThat(records2.get(2).get(USER.ID)).isEqualTo(2);
    assertThat(records2.get(3).get(USER.ID)).isEqualTo(1);
  }
}
