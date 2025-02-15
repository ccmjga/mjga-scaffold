package com.zl.mjga.user.domain.repository;

import com.zl.mjga.user.UserQueryDto;
import com.zl.mjga.user.domain.model.User;
import com.zl.mjga.user.domain.model.UserTable;
import com.zl.mjga.user.web.UserPageQueryVm;
import lombok.RequiredArgsConstructor;
import org.babyfish.jimmer.Page;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserAggregateRepository {

  private final JSqlClient sqlClient;

  @Nullable public User fetchUniqueUserBy(@Nullable Fetcher<User> fetcher, UserQueryDto userQueryDto) {
    UserTable table = UserTable.$;
    return sqlClient
        .createQuery(table)
        .where(table.username().eqIf(userQueryDto.username()))
        .where(table.id().eqIf(userQueryDto.id()))
        .select(table.fetch(fetcher))
        .fetchOne();
  }

  public Page<User> fetchAggregateWithPageBy(
      @Nullable Fetcher<User> fetcher,
      UserPageQueryVm userPageQueryVm,
      int pageIndex,
      int pageSize) {
    UserTable table = UserTable.$;
    return sqlClient
        .createQuery(table)
        .where(table.enable().eqIf(userPageQueryVm.enable()))
        .where(table.username().eqIf(userPageQueryVm.username()))
        .select(table.fetch(fetcher))
        .fetchPage(pageIndex, pageSize);
  }
}
