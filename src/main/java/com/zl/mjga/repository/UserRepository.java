package com.zl.mjga.repository;

import static org.jooq.generated.mjga.tables.User.USER;
import static org.jooq.impl.DSL.*;

import com.zl.mjga.dto.PageRequestDto;
import com.zl.mjga.dto.urp.PermissionDto;
import com.zl.mjga.dto.urp.RoleDto;
import com.zl.mjga.dto.urp.UserQueryDto;
import com.zl.mjga.dto.urp.UserRolePermissionDto;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.generated.mjga.tables.daos.*;
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

  public Result<Record> pageFetchUserBy(PageRequestDto pageRequestDto, UserQueryDto userQueryDto) {
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

  public Result<Record> pageFetchUserAggBy(
      PageRequestDto pageRequestDto, UserQueryDto userQueryDto) {
    return selectUserAgg()
        .where(
            userQueryDto.getUsername() != null
                ? USER.USERNAME.like("%" + userQueryDto.getUsername() + "%")
                : noCondition())
        .orderBy(pageRequestDto.getSortFields())
        .limit(pageRequestDto.getSize())
        .offset(pageRequestDto.getOffset())
        .fetch();
  }

  public UserRolePermissionDto getUserAggDtoBy(Long userId) {
    return selectUserAgg().where(USER.ID.eq(userId)).fetchOneInto(UserRolePermissionDto.class);
  }

  public SelectJoinStep<Record> selectUserAgg() {
    return ctx()
        .select(
            USER.asterisk(),
            DSL.count().over().as("total_user"),
            multiset(
                    select(
                            USER.role().asterisk(),
                            multiset(
                                    select(USER.role().permission().asterisk())
                                        .from(USER.role())
                                        .leftJoin(USER.role().permission()))
                                .convertFrom(
                                    r -> r.map((record) -> record.into(PermissionDto.class)))
                                .as("permissions"))
                        .from(USER)
                        .leftJoin(USER.role()))
                .convertFrom(r -> r.map((record) -> record.into(RoleDto.class)))
                .as("roles"))
        .from(USER);
  }

  public Result<Record> getUserFlatBy(Long userId) {
    return ctx()
        .select(USER.asterisk(), USER.role().asterisk(), USER.role().permission().asterisk())
        .from(USER)
        .leftJoin(USER.role())
        .leftJoin(USER.role().permission())
        .where(USER.ID.eq(userId))
        .fetch();
  }

  @Transactional
  public void deleteUserBy(String username) {
    ctx().delete(USER).where(USER.USERNAME.eq(username)).execute();
  }
}
