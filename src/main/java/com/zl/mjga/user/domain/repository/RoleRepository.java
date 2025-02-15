package com.zl.mjga.user.domain.repository;

import com.zl.mjga.user.RolePermissionShortInput;
import com.zl.mjga.user.domain.model.Role;
import com.zl.mjga.user.domain.model.RoleTable;
import java.util.List;
import org.babyfish.jimmer.spring.repository.JRepository;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.jspecify.annotations.Nullable;

public interface RoleRepository extends JRepository<Role, Long> {
  Role findByCode(String code);

  List<Role> findByCodeIn(List<String> codeList);

  default List<Role> fetchRoleComplexBy(@Nullable Fetcher<Role> fetcher) {
    RoleTable table = RoleTable.$;
    return sql().createQuery(table).select(table.fetch(fetcher)).execute();
  }

  default void upsert(RolePermissionShortInput rolePermissionShortInput) {
    sql().save(rolePermissionShortInput);
  }
}
