package com.zl.mjga.user.domain;

import static org.mockito.Mockito.when;

import com.zl.mjga.user.UserQueryDto;
import com.zl.mjga.user.UserRolePermissionView;
import com.zl.mjga.user.UserRoleShortInput;
import com.zl.mjga.user.domain.enums.ERole;
import com.zl.mjga.user.domain.model.*;
import com.zl.mjga.user.domain.repository.RoleRepository;
import com.zl.mjga.user.domain.repository.UserAggregateRepository;
import com.zl.mjga.user.domain.service.UserDomainService;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.mutation.SimpleSaveResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserDomainServiceTest {

  @InjectMocks @Spy private UserDomainService userDomainService;

  @Mock private UserAggregateRepository userAggregateRepository;

  @Mock private RoleRepository roleRepository;

  @Mock private JSqlClient sqlClient;

  @Test
  public void queryUniqueUserRolePermissionBy_withSpecifyShapeFetch_shouldReturnSpecifyView() {
    UserFetcher fetcher =
        Fetchers.USER_FETCHER
            .allScalarFields()
            .roles(
                Fetchers.ROLE_FETCHER
                    .allScalarFields()
                    .permissions(Fetchers.PERMISSION_FETCHER.allScalarFields()));
    User mockUser =
        UserDraft.$.produce(
            draft -> {
              draft.setId(1L);
              draft.setUsername("username");
              draft.setPassword("password");
              draft.setCreateTime(OffsetDateTime.now().toLocalDateTime());
              draft.setEnable(true);
              Role role =
                  RoleDraft.$.produce(
                      roleDraft -> {
                        roleDraft.setId(10L);
                        roleDraft.setCode("code");
                        roleDraft.setName("name");
                        roleDraft.setPermissions(
                            List.of(
                                PermissionDraft.$.produce(
                                    permission -> {
                                      permission.setId(100L);
                                      permission.setCode("code");
                                      permission.setName("name");
                                    })));
                      });
              draft.setRoles(List.of(role));
            });
    UserQueryDto mockUserQueryDto = new UserQueryDto(1L, "");
    when(userAggregateRepository.fetchUniqueUserBy(fetcher, mockUserQueryDto)).thenReturn(mockUser);
    UserRolePermissionView userRolePermissionView =
        userDomainService.queryUniqueUserRolePermissionBy(mockUserQueryDto);
    Assertions.assertThat(userRolePermissionView).isNotNull();
    Assertions.assertThat(userRolePermissionView.getId()).isEqualTo(1L);
    Assertions.assertThat(userRolePermissionView.getRoles().size()).isEqualTo(1);
    Assertions.assertThat(userRolePermissionView.getRoles().get(0).getId()).isEqualTo(10L);
    Assertions.assertThat(userRolePermissionView.getRoles().get(0).getPermissions().get(0).getId())
        .isEqualTo(100L);
  }

  @Test
  public void addGeneralUser_withValidUserShortInput_shouldSuccessAndReturnUserId() {
    UserRoleShortInput mockInput =
        new UserRoleShortInput.Builder().username("username").password("password").build();
    Role mockRole =
        RoleDraft.$.produce(
            roleDraft -> {
              roleDraft.setId(1L);
              roleDraft.setCode("code");
              roleDraft.setName("name");
            });
    when(roleRepository.findByCodeIn(List.of(ERole.GENERAL.name()))).thenReturn(List.of(mockRole));
    SimpleSaveResult<User> mockResult =
        new SimpleSaveResult<>(
            new HashMap<>(),
            null,
            UserDraft.$.produce(
                draft -> {
                  draft.setId(1L);
                  draft.setUsername("username");
                  draft.setPassword("password");
                  draft.setRoles(List.of(mockRole));
                }));
    when(sqlClient.save(mockInput)).thenReturn(mockResult);
    Assertions.assertThat(userDomainService.addGeneralUser(mockInput)).isEqualTo(1L);
  }
}
