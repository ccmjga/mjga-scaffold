package com.mjga.repository;

import static org.jooq.generated.Tables.USER_ROLE_MAP;

import org.jooq.Configuration;
import org.jooq.generated.tables.daos.UserRoleMapDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class UserRoleMapRepository extends UserRoleMapDao {

  @Autowired
  public UserRoleMapRepository(Configuration configuration) {
    super(configuration);
  }

  @Transactional
  public void deleteByUserId(Long userId) {
    ctx().deleteFrom(USER_ROLE_MAP).where(USER_ROLE_MAP.USER_ID.eq(userId)).execute();
  }
}
