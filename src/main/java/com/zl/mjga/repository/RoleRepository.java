package com.zl.mjga.repository;

import static org.jooq.generated.mjga.tables.Role.ROLE;
import static org.jooq.impl.DSL.*;

import com.zl.mjga.dto.PageRequestDto;
import com.zl.mjga.dto.urp.RoleQueryDto;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Configuration;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.generated.mjga.tables.daos.RoleDao;
import org.jooq.generated.mjga.tables.pojos.Role;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RoleRepository extends RoleDao {

  @Autowired
  public RoleRepository(Configuration configuration) {
    super(configuration);
  }

  public List<Role> selectByRoleCodeIn(List<String> roleCodeList) {
    return ctx().selectFrom(ROLE).where(ROLE.CODE.in(roleCodeList)).fetchInto(Role.class);
  }

  public List<Role> selectByRoleIdIn(List<Long> roleIdList) {
    return ctx().selectFrom(ROLE).where(ROLE.ID.in(roleIdList)).fetchInto(Role.class);
  }

  public Result<Record> pageFetchRoleAggBy(
      PageRequestDto pageRequestDto, RoleQueryDto roleQueryDto) {
    return ctx()
        .select(
            ROLE.asterisk(),
            DSL.count(ROLE.ID).over().as("total_role"),
            multiset(select(ROLE.permission().asterisk()).from(ROLE.permission())))
        .from(ROLE)
        .where(
            CollectionUtils.isEmpty(roleQueryDto.getRoleIdList())
                ? noCondition()
                : ROLE.ID.in(roleQueryDto.getRoleIdList()))
        .and(
            roleQueryDto.getRoleId() == null ? noCondition() : ROLE.ID.eq(roleQueryDto.getRoleId()))
        .and(
            StringUtils.isEmpty(roleQueryDto.getRoleName())
                ? noCondition()
                : ROLE.NAME.like("%" + roleQueryDto.getRoleName() + "%"))
        .and(
            StringUtils.isEmpty(roleQueryDto.getRoleCode())
                ? noCondition()
                : ROLE.CODE.eq(roleQueryDto.getRoleCode()))
        .orderBy(pageRequestDto.getSortFields())
        .limit(pageRequestDto.getSize())
        .offset(pageRequestDto.getOffset())
        .fetch();
  }

  public Result<Record> pageFetchRolesBy(PageRequestDto pageRequestDto, RoleQueryDto roleQueryDto) {
    return ctx()
        .select(asterisk(), DSL.count(ROLE.ID).over().as("total_role"))
        .from(ROLE)
        .where(
            CollectionUtils.isEmpty(roleQueryDto.getRoleIdList())
                ? noCondition()
                : ROLE.ID.in(roleQueryDto.getRoleIdList()))
        .and(
            roleQueryDto.getRoleId() == null ? noCondition() : ROLE.ID.eq(roleQueryDto.getRoleId()))
        .and(
            StringUtils.isEmpty(roleQueryDto.getRoleName())
                ? noCondition()
                : ROLE.NAME.like("%" + roleQueryDto.getRoleName() + "%"))
        .and(
            StringUtils.isEmpty(roleQueryDto.getRoleCode())
                ? noCondition()
                : ROLE.CODE.eq(roleQueryDto.getRoleCode()))
        .orderBy(pageRequestDto.getSortFields())
        .limit(pageRequestDto.getSize())
        .offset(pageRequestDto.getOffset())
        .fetch();
  }

  public Result<Record> getRoleAggBy(Long roleId) {
    return ctx()
        .select(ROLE.asterisk(), ROLE.permission().asterisk())
        .from(ROLE)
        .leftJoin(ROLE.permission())
        .where(ROLE.ID.eq(roleId))
        .orderBy(ROLE.ID)
        .fetch();
  }
}
