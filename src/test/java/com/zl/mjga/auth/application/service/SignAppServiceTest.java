package com.zl.mjga.auth.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.zl.mjga.auth.application.SignAppService;
import com.zl.mjga.auth.web.SignInVm;
import com.zl.mjga.auth.web.SignUpVm;
import com.zl.mjga.common.BusinessException;
import com.zl.mjga.user.UserDomainApi;
import com.zl.mjga.user.UserQueryDto;
import com.zl.mjga.user.UserRolePermissionView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class SignAppServiceTest {

  @InjectMocks @Spy private SignAppService signAppService;

  @Mock private UserDomainApi userDomainApi;

  @Mock private PasswordEncoder passwordEncoder;

  @Test
  public void signUp_withExistUser_shouldThrowException() {
    when(signAppService.isUsernameDuplicate(anyString())).thenReturn(true);
    Assertions.assertThrows(
        BusinessException.class, () -> signAppService.signUp(new SignUpVm("username", "password")));
  }

  @Test
  public void signIn_withNonExistUser_shouldThrowException() {
    when(userDomainApi.queryUniqueUserRolePermissionBy(any(UserQueryDto.class))).thenReturn(null);
    Assertions.assertThrows(
        BusinessException.class, () -> signAppService.signIn(new SignInVm("username", "password")));
  }

  @Test
  public void signIn_withExistUser_shouldReturnId() {
    String password = "password";
    UserRolePermissionView mockUser = new UserRolePermissionView();
    mockUser.setId(1L);
    mockUser.setUsername("username");
    mockUser.setPassword(password);
    mockUser.setEnable(true);
    when(userDomainApi.queryUniqueUserRolePermissionBy(any(UserQueryDto.class)))
        .thenReturn(mockUser);
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
    Assertions.assertEquals(1L, signAppService.signIn(new SignInVm("username", password)));
  }
}
