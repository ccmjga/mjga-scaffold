package com.zl.mjga.auth.application;

import com.zl.mjga.user.UserDomainApi;
import com.zl.mjga.user.UserQueryDto;
import com.zl.mjga.user.UserRolePermissionView;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsAppService implements UserDetailsService {

  private final UserDomainApi userDomainApi;

  @Override
  public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
    UserRolePermissionView userRolePermissionView =
        userDomainApi.queryUniqueUserRolePermissionBy(new UserQueryDto(Long.valueOf(id), null));
    if (userRolePermissionView == null) {
      throw new UsernameNotFoundException(String.format("uid %s user not found", id));
    }
    return new User(
        userRolePermissionView.getUsername(),
        userRolePermissionView.getPassword(),
        userRolePermissionView.isEnable(),
        true,
        true,
        true,
        userRolePermissionView.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(permission -> new SimpleGrantedAuthority(permission.getCode()))
            .toList());
  }
}
