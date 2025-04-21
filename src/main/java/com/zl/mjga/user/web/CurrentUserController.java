package com.zl.mjga.user.web;

import com.zl.mjga.user.UpdateUserInput;
import com.zl.mjga.user.UserDomainApi;
import com.zl.mjga.user.UserQueryDto;
import com.zl.mjga.user.domain.model.Fetchers;
import com.zl.mjga.user.domain.model.User;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
public class CurrentUserController {

  private final UserDomainApi userDomainApi;

  private final JSqlClient sqlClient;

  public static final Fetcher<User> CURRENT_USER_AGGREGATE =
      Fetchers.USER_FETCHER
          .allScalarFields()
          .password(false)
          .roles(
              Fetchers.ROLE_FETCHER
                  .allScalarFields()
                  .permissions(Fetchers.PERMISSION_FETCHER.allScalarFields()));

  @GetMapping
  public User user(Principal principal) {
    UserQueryDto userQueryDto = new UserQueryDto(null, principal.getName());
    return userDomainApi.queryUniqueUserRolePermissionBy(CURRENT_USER_AGGREGATE, userQueryDto);
  }

  @PutMapping
  public void user(Principal principal, @RequestBody UpdateUserInput updateUserInput) {
    String name = principal.getName();
    UserQueryDto userQueryDto = new UserQueryDto(null, name);
    User user = userDomainApi.queryUniqueUserRolePermissionBy(Fetchers.USER_FETCHER, userQueryDto);
    updateUserInput.setId(user.id());
    sqlClient.saveCommand(updateUserInput).execute();
  }
}
