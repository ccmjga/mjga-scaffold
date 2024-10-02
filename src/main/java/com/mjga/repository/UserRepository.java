package com.mjga.repository;

import static org.jooq.generated.tables.Permission.PERMISSION;
import static org.jooq.generated.tables.Role.ROLE;
import static org.jooq.generated.tables.RolePermissionMap.ROLE_PERMISSION_MAP;
import static org.jooq.generated.tables.User.USER;
import static org.jooq.generated.tables.UserRoleMap.USER_ROLE_MAP;
import static org.jooq.impl.DSL.*;

import com.mjga.dto.PageRequestDto;
import com.mjga.dto.urp.UserQueryDto;
import org.jooq.generated.tables.daos.*;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class UserRepository extends UserDao {

  @Autowired
  public UserRepository(Configuration configuration) {
    super(configuration);
  }

  public Result<Record> pageFetchBy(PageRequestDto pageRequestDto, UserQueryDto userQueryDto) {
    return ctx()
        .select(asterisk(), DSL.count().over().as("total_user"))
        .from(USER)
        .where(
            userQueryDto.getUsername() != null
                ? USER.USERNAME.like("%" + userQueryDto.getUsername() + "%")
                : noCondition())
        .orderBy(pageRequestDto.getSortFields())
        .limit(pageRequestDto.getSize())
        .offset(pageRequestDto.getOffset())
        .fetch();
  }

  public Result<Record> fetchUniqueUserWithRolePermissionBy(Long userId) {
    return ctx()
        .select()
        .from(USER)
        .leftJoin(USER_ROLE_MAP)
        .on(USER.ID.eq(USER_ROLE_MAP.USER_ID))
        .leftJoin(ROLE)
        .on(USER_ROLE_MAP.ROLE_ID.eq(ROLE.ID))
        .leftJoin(ROLE_PERMISSION_MAP)
        .on(ROLE.ID.eq(ROLE_PERMISSION_MAP.ROLE_ID))
        .leftJoin(PERMISSION)
        .on(ROLE_PERMISSION_MAP.PERMISSION_ID.eq(PERMISSION.ID))
        .where(USER.ID.eq(userId))
        .fetch();
  }

  @Transactional
  public void deleteByUsername(String username) {
    ctx().delete(USER).where(USER.USERNAME.eq(username)).execute();
  }
}
