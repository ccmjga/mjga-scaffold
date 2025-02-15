package com.zl.mjga.user.web;

import com.zl.mjga.user.RolePermissionShortInput;
import com.zl.mjga.user.domain.model.Fetchers;
import com.zl.mjga.user.domain.model.Role;
import com.zl.mjga.user.domain.model.RoleFetcher;
import com.zl.mjga.user.domain.repository.RoleRepository;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.babyfish.jimmer.client.FetchBy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

  private final RoleRepository roleRepository;

  private static final RoleFetcher ROLE_FETCHER =
      Fetchers.ROLE_FETCHER
          .allScalarFields()
          .permissions(Fetchers.PERMISSION_FETCHER.allScalarFields());

  @PreAuthorize(
      "hasAuthority(T(com.zl.mjga.user.domain.enums.EPermission).READ_USER_ROLE_PERMISSION)")
  @GetMapping()
  public List<@FetchBy("ROLE_FETCHER") Role> getRoles() {
    return roleRepository.fetchRoleComplexBy(ROLE_FETCHER);
  }

  @PreAuthorize(
      "hasAuthority(T(com.zl.mjga.user.domain.enums.EPermission).WRITE_USER_ROLE_PERMISSION)")
  @PostMapping("/upsert")
  public void upsert(@RequestBody @Valid RolePermissionShortInput rolePermissionShortInput) {
    roleRepository.upsert(rolePermissionShortInput);
  }
}
