package com.zl.mjga.user.application;

import com.zl.mjga.user.UserAppApi;
import com.zl.mjga.user.UserRoleShortInput;
import com.zl.mjga.user.domain.service.UserDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserAppService implements UserAppApi {

  private final UserDomainService userDomainService;

  @Override
  @Transactional(rollbackFor = Throwable.class)
  public void addGeneralUser(UserRoleShortInput userRoleShortInput) {
    Long savedId = userDomainService.addGeneralUser(userRoleShortInput);
  }
}
