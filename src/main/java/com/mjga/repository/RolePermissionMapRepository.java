package com.mjga.repository;

import static jooq.tables.RolePermissionMap.ROLE_PERMISSION_MAP;

import jooq.tables.daos.RolePermissionMapDao;
import org.jooq.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
public class RolePermissionMapRepository extends RolePermissionMapDao {

  @Autowired
  public RolePermissionMapRepository(Configuration configuration) {
    super(configuration);
  }

  @Transactional
  public void deleteByRoleId(Long roleId) {
    ctx().deleteFrom(ROLE_PERMISSION_MAP).where(ROLE_PERMISSION_MAP.ROLE_ID.eq(roleId)).execute();
  }
}
