package com.zl.mjga.user.domain.repository;

import com.zl.mjga.user.PermissionInput;
import com.zl.mjga.user.domain.model.Permission;
import org.babyfish.jimmer.spring.repository.JRepository;
import org.babyfish.jimmer.sql.ast.mutation.SaveMode;

public interface PermissionRepository extends JRepository<Permission, Long> {
  Permission findByCode(String code);

  Permission findByName(String name);

  default void upsert(PermissionInput permissionInput) {
    sql().save(permissionInput, SaveMode.UPSERT);
  }
}
