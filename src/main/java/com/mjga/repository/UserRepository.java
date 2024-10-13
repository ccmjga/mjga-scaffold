package com.mjga.repository;

import static org.jooq.generated.tables.Permission.PERMISSION;
import static org.jooq.generated.tables.Role.ROLE;
import static org.jooq.generated.tables.RolePermissionMap.ROLE_PERMISSION_MAP;
import static org.jooq.generated.tables.User.USER;
import static org.jooq.generated.tables.UserRoleMap.USER_ROLE_MAP;
import static org.jooq.impl.DSL.*;

import com.mjga.dto.PageRequestDto;
import com.mjga.dto.urp.PermissionDto;
import com.mjga.dto.urp.RoleDto;
import com.mjga.dto.urp.UserQueryDto;
import com.mjga.dto.urp.UserRolePermissionDto;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.generated.tables.daos.*;
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

  public UserRolePermissionDto fetchUniqueUserDtoWithNestedRolePermissionBy(Long userId) {
    return ctx()
        .select(
            USER.asterisk(),
            multiset(
                    select(
                            ROLE.asterisk(),
                            multiset(
                                    select(PERMISSION.asterisk())
                                        .from(ROLE_PERMISSION_MAP)
                                        .leftJoin(PERMISSION)
                                        .on(ROLE_PERMISSION_MAP.PERMISSION_ID.eq(PERMISSION.ID))
                                        .where(ROLE_PERMISSION_MAP.ROLE_ID.eq(ROLE.ID)))
                                .convertFrom(
                                    r -> r.map((record) -> record.into(PermissionDto.class)))
                                .as("permissions"))
                        .from(USER_ROLE_MAP)
                        .leftJoin(ROLE)
                        .on(USER_ROLE_MAP.ROLE_ID.eq(ROLE.ID))
                        .where(USER.ID.eq(USER_ROLE_MAP.USER_ID)))
                .convertFrom(r -> r.map((record) -> record.into(RoleDto.class)))
                .as("roles"))
        .from(USER)
        .where(USER.ID.eq(userId))
        .fetchOneInto(UserRolePermissionDto.class);
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
