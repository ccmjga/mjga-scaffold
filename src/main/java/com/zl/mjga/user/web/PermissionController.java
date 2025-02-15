package com.zl.mjga.user.web;

import com.zl.mjga.user.PermissionInput;
import com.zl.mjga.user.domain.model.Fetchers;
import com.zl.mjga.user.domain.model.Permission;
import com.zl.mjga.user.domain.repository.PermissionRepository;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.babyfish.jimmer.client.FetchBy;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {

  private final PermissionRepository permissionRepository;

  private static final Fetcher<Permission> PERMISSION_FETCHER =
      Fetchers.PERMISSION_FETCHER.allScalarFields();

  @PreAuthorize(
      "hasAuthority(T(com.zl.mjga.user.domain.enums.EPermission).READ_USER_ROLE_PERMISSION)")
  @GetMapping
  public List<@FetchBy("PERMISSION_FETCHER") Permission> getPermissions() {
    return permissionRepository.findAll();
  }

  @PreAuthorize(
      "hasAuthority(T(com.zl.mjga.user.domain.enums.EPermission).WRITE_USER_ROLE_PERMISSION)")
  @PostMapping("/upsert")
  public void upsert(@RequestBody @Valid PermissionInput permissionInput) {
    permissionRepository.upsert(permissionInput);
  }
}
