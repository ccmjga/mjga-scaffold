package com.zl.mjga.user;

import org.jspecify.annotations.Nullable;

public interface UserDomainApi {

  @Nullable UserRolePermissionView queryUniqueUserRolePermissionBy(UserQueryDto userQueryDto);

  Long addGeneralUser(UserRoleShortInput userRoleInputShort);
}
