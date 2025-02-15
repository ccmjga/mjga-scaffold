package com.zl.mjga.user.web;

import com.zl.mjga.user.domain.model.Fetchers;
import com.zl.mjga.user.domain.model.User;
import com.zl.mjga.user.domain.repository.UserAggregateRepository;
import lombok.RequiredArgsConstructor;
import org.babyfish.jimmer.Page;
import org.babyfish.jimmer.client.FetchBy;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

  private final UserAggregateRepository userAggregateRepository;

  private static final Fetcher<User> USER_AGGREGATE =
      Fetchers.USER_FETCHER
          .allScalarFields()
          .roles(
              Fetchers.ROLE_FETCHER
                  .allScalarFields()
                  .permissions(Fetchers.PERMISSION_FETCHER.allScalarFields()));

  @PreAuthorize(
      "hasAuthority(T(com.zl.mjga.user.domain.enums.EPermission).READ_USER_ROLE_PERMISSION)")
  @GetMapping("/search")
  public Page<@FetchBy("USER_AGGREGATE") User> pageQuery(
      @RequestParam int pageIndex,
      @RequestParam int pageSize,
      @ModelAttribute UserPageQueryVm userPageQueryVm) {
    return userAggregateRepository.fetchAggregateWithPageBy(
        USER_AGGREGATE, userPageQueryVm, pageIndex, pageSize);
  }
}
