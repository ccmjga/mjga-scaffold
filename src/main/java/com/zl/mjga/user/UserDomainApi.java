package com.zl.mjga.user;

import com.zl.mjga.user.domain.model.User;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public interface UserDomainApi {

  @Nullable UserRolePermissionView queryUniqueUserRolePermissionBy(UserQueryDto userQueryDto);

  @Nullable User queryUniqueUserRolePermissionBy(
      @Nullable Fetcher<User> fetcher, @NotNull UserQueryDto userQueryDto);

  Long addGeneralUser(UserRoleShortInput userRoleInputShort);
}
