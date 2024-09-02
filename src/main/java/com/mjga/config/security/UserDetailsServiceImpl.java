package com.mjga.config.security;

import com.mjga.dto.urp.UserRolePermissionDto;
import com.mjga.service.UserRolePermissionService;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRolePermissionService userRolePermissionService;

  @Override
  public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
    Optional<UserRolePermissionDto> queryOptional =
        userRolePermissionService.queryUniqueUserWithRolePermission(Long.valueOf(id));
    UserRolePermissionDto userRolePermissionDto =
        queryOptional.orElseThrow(
            () -> new UsernameNotFoundException(String.format("uid %s user not found", id)));
    return new User(
        userRolePermissionDto.getUsername(),
        userRolePermissionDto.getPassword(),
        userRolePermissionDto.getEnable(),
        true,
        true,
        true,
        userRolePermissionDto.getPermissions().stream()
            .map((permission) -> new SimpleGrantedAuthority(permission.getCode()))
            .collect(Collectors.toSet()));
  }
}
