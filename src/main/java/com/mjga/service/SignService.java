package com.mjga.service;

import com.mjga.dto.sign.SignInDto;
import com.mjga.dto.sign.SignUpDto;
import com.mjga.exception.BusinessException;
import com.mjga.model.urp.ERole;
import com.mjga.repository.UserRepository;
import java.util.List;
import org.jooq.generated.tables.pojos.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SignService {

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  private final UserRolePermissionService userRolePermissionService;

  public Long signIn(SignInDto signInDto) {
    User user = userRepository.fetchOneByUsername(signInDto.getUsername());
    if (user == null) {
      throw new BusinessException(String.format("%s user not found", signInDto.getUsername()));
    }
    if (!passwordEncoder.matches(signInDto.getPassword(), user.getPassword())) {
      throw new BusinessException("password invalid");
    }
    return user.getId();
  }

  @Transactional(rollbackFor = Throwable.class)
  public void signUp(SignUpDto signUpDto) {
    if (isUsernameDuplicate(signUpDto.getUsername())) {
      throw new BusinessException(
          String.format("username %s already exist", signUpDto.getUsername()));
    }
    User user = new User();
    user.setUsername(signUpDto.getUsername());
    user.setPassword(passwordEncoder.encode(signUpDto.getPassword()));
    userRepository.insert(user);
    User insertUser = userRepository.fetchOneByUsername(signUpDto.getUsername());
    userRolePermissionService.bindRoleModuleToUser(insertUser.getId(), List.of(ERole.GENERAL));
  }

  public boolean isUsernameDuplicate(String username) {
    return userRepository.fetchOneByUsername(username) != null;
  }
}
