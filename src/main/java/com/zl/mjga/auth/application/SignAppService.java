package com.zl.mjga.auth.application;

import com.zl.mjga.auth.web.SignInVm;
import com.zl.mjga.auth.web.SignUpVm;
import com.zl.mjga.common.BusinessException;
import com.zl.mjga.user.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SignAppService {

  private final UserDomainApi userDomainApi;

  private final UserAppApi userAppApi;

  private final PasswordEncoder passwordEncoder;

  public Long signIn(SignInVm signInVm) {
    UserRolePermissionView userRolePermissionView =
        userDomainApi.queryUniqueUserRolePermissionBy(
            new UserQueryDto(null, signInVm.getUsername()));
    if (userRolePermissionView == null) {
      throw new BusinessException(String.format("%s user not found", signInVm.getUsername()));
    }
    if (!passwordEncoder.matches(signInVm.getPassword(), userRolePermissionView.getPassword())) {
      throw new BusinessException("password invalid");
    }
    return userRolePermissionView.getId();
  }

  public void signUp(SignUpVm signUpVm) {
    if (isUsernameDuplicate(signUpVm.getUsername())) {
      throw new BusinessException(
          String.format("username %s already exist", signUpVm.getUsername()));
    }
    userAppApi.addGeneralUser(
        new UserRoleShortInput.Builder()
            .username(signUpVm.getUsername())
            .password(passwordEncoder.encode(signUpVm.getPassword()))
            .build());
  }

  public boolean isUsernameDuplicate(String username) {
    UserRolePermissionView userRolePermissionView =
        userDomainApi.queryUniqueUserRolePermissionBy(new UserQueryDto(null, username));
    return userRolePermissionView != null;
  }
}
