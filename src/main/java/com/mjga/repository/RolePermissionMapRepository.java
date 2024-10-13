package com.mjga.repository;

import static org.jooq.generated.tables.RolePermissionMap.ROLE_PERMISSION_MAP;

import org.jooq.Configuration;
import org.jooq.generated.tables.daos.RolePermissionMapDao;
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
