package com.zl.mjga.user.domain.service;

import com.zl.mjga.user.*;
import com.zl.mjga.user.domain.enums.ERole;
import com.zl.mjga.user.domain.model.Fetchers;
import com.zl.mjga.user.domain.model.Role;
import com.zl.mjga.user.domain.model.User;
import com.zl.mjga.user.domain.repository.RoleRepository;
import com.zl.mjga.user.domain.repository.UserAggregateRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.mutation.SimpleSaveResult;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserDomainService implements UserDomainApi {

  private final UserAggregateRepository userAggregateRepository;

  private final RoleRepository roleRepository;

  private final JSqlClient sqlClient;

  @Override
  public User queryUniqueUserRolePermissionBy(
      @Nullable Fetcher<User> fetcher, @NotNull UserQueryDto userQueryDto) {
    return userAggregateRepository.fetchUniqueUserBy(fetcher, userQueryDto);
  }

  @Override
  public UserRolePermissionView queryUniqueUserRolePermissionBy(
      @NotNull UserQueryDto userQueryDto) {
    User userModel =
        userAggregateRepository.fetchUniqueUserBy(
            Fetchers.USER_FETCHER
                .allScalarFields()
                .roles(
                    Fetchers.ROLE_FETCHER
                        .allScalarFields()
                        .permissions(Fetchers.PERMISSION_FETCHER.allScalarFields())),
            userQueryDto);
    if (userModel == null) {
      return null;
    } else {
      return new UserRolePermissionView(userModel);
    }
  }

  @Override
  public Long addGeneralUser(UserRoleShortInput userRoleShortInput) {
    List<Role> roles = roleRepository.findByCodeIn(List.of(ERole.GENERAL.name()));
    userRoleShortInput.setRolesId(roles.stream().map(Role::id).collect(Collectors.toList()));
    SimpleSaveResult<User> save = sqlClient.save(userRoleShortInput);
    return save.getModifiedEntity().id();
  }
}
