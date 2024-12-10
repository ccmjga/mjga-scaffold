package com.zl.mjga.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.zl.mjga.dto.sign.SignInDto;
import com.zl.mjga.dto.sign.SignUpDto;
import com.zl.mjga.exception.BusinessException;
import com.zl.mjga.model.urp.ERole;
import com.zl.mjga.repository.UserRepository;
import com.zl.mjga.service.SignService;
import com.zl.mjga.service.UserRolePermissionService;
import java.util.List;
import org.jooq.generated.mjga.tables.pojos.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class SignUnitTest {
  @InjectMocks @Spy private SignService signService;

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private UserRolePermissionService userRolePermissionService;

  @Test
  void signIn_givenValidSignInfo_shouldReturnUserId() {
    // arrange
    User stubUser = new User();
    stubUser.setId(1L);
    stubUser.setUsername("testUserName");
    stubUser.setPassword("GjFH2fzRB2y7DDrO");
    when(userRepository.fetchOneByUsername("testUserName")).thenReturn(stubUser);
    when(passwordEncoder.matches("GjFH2fzRB2y7DDrO", "GjFH2fzRB2y7DDrO")).thenReturn(true);
    // action
    Long userId = signService.signIn(new SignInDto("testUserName", "GjFH2fzRB2y7DDrO"));
    assertThat(userId).isEqualTo(1L);
  }

  @Test
  void signIn_givenInvalidUserName_shouldThrowUserNotFoundException() {
    when(userRepository.fetchOneByUsername("notFoundUserName")).thenReturn(null);
    assertThatThrownBy(
            () -> signService.signIn(new SignInDto("notFoundUserName", "GjFH2fzRB2y7DDrO")))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void signIn_givenInvalidPassword_shouldThrowBadCredentialsException() {
    // arrange
    User stubUser = new User();
    stubUser.setId(1L);
    stubUser.setUsername("testUserName");
    stubUser.setPassword("GjFH2fzRB2y7DDrO");
    when(userRepository.fetchOneByUsername("testUserName")).thenReturn(stubUser);
    when(passwordEncoder.matches("InvalidPassword", "GjFH2fzRB2y7DDrO")).thenReturn(false);
    // action
    assertThatThrownBy(() -> signService.signIn(new SignInDto("testUserName", "InvalidPassword")))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void signUp_givenDuplicateUsername_shouldThrowDuplicateException() {
    SignUpDto signUpDto = new SignUpDto();
    signUpDto.setUsername("testUserName");
    signUpDto.setPassword("B0pjKYnIK67hz4");
    User stubUser = new User();
    stubUser.setId(1L);
    stubUser.setUsername("testUserName");
    stubUser.setPassword("B0pjKYnIK67hz4");
    when(userRepository.fetchOneByUsername("testUserName")).thenReturn(stubUser);
    assertThatThrownBy(() -> signService.signUp(signUpDto)).isInstanceOf(BusinessException.class);
  }

  @Test
  void signUp_givenValidUsername_shouldRunSuccess() {
    SignUpDto signUpDto = new SignUpDto();
    signUpDto.setUsername("newUser");
    signUpDto.setPassword("B0pjKYnIK67hz4");
    User stubUser = new User();
    stubUser.setUsername("newUser");
    stubUser.setPassword("encodedB0pjKYnIK67hz4");
    User insertUser = new User();
    insertUser.setId(1L);
    insertUser.setUsername("newUser");
    insertUser.setPassword("encodedB0pjKYnIK67hz4");
    when(signService.isUsernameDuplicate(signUpDto.getUsername())).thenReturn(false);
    when(userRepository.fetchOneByUsername("newUser")).thenReturn(insertUser);
    when(passwordEncoder.encode("B0pjKYnIK67hz4")).thenReturn("encodedB0pjKYnIK67hz4");
    signService.signUp(signUpDto);
    verify(userRepository, times(1)).insert(stubUser);
    verify(userRolePermissionService, times(1))
        .bindRoleModuleToUser(insertUser.getId(), List.of(ERole.GENERAL));
  }
}
