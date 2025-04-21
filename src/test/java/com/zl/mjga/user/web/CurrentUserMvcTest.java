package com.zl.mjga.user.web;

import static com.zl.mjga.user.web.CurrentUserController.CURRENT_USER_AGGREGATE;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zl.mjga.common.security.HttpFireWallConfig;
import com.zl.mjga.user.UserDomainApi;
import com.zl.mjga.user.UserQueryDto;
import com.zl.mjga.user.domain.model.PermissionDraft;
import com.zl.mjga.user.domain.model.RoleDraft;
import com.zl.mjga.user.domain.model.User;
import com.zl.mjga.user.domain.model.UserDraft;
import java.util.List;
import org.babyfish.jimmer.jackson.ImmutableModule;
import org.babyfish.jimmer.sql.JSqlClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = {CurrentUserController.class})
@Import({HttpFireWallConfig.class, JacksonSerializeConfig.class})
public class CurrentUserMvcTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private UserDomainApi userDomainApi;

  @MockBean private JSqlClient jsqlClient;

  @Test
  @WithMockUser(username = "username")
  void user_queryCurrentUser_shouldReturnUserInfoAndStatus200() throws Exception {
    User mockUser =
        UserDraft.$.produce(
            (draft -> {
              draft.setUsername("username");
              draft.setPassword("password");
              draft.setEnable(true);
              draft.setRoles(
                  List.of(
                      RoleDraft.$.produce(
                          (roleDraft -> {
                            roleDraft.setId(1L);
                            roleDraft.setCode("roleCode");
                            roleDraft.setName("roleName");
                            roleDraft.setPermissions(
                                List.of(
                                    PermissionDraft.$.produce(
                                        (permissionDraft -> {
                                          permissionDraft.setId(2L);
                                          permissionDraft.setCode("permissionCode");
                                          permissionDraft.setName("permissionName");
                                        }))));
                          }))));
            }));
    UserQueryDto userQueryDto = new UserQueryDto(null, mockUser.username());
    when(userDomainApi.queryUniqueUserRolePermissionBy(CURRENT_USER_AGGREGATE, userQueryDto))
        .thenReturn(mockUser);
    ObjectMapper mapper = new ObjectMapper().registerModule(new ImmutableModule());
    mockMvc
        .perform(get("/me"))
        .andExpect(status().isOk())
        .andExpect(content().json(mapper.writeValueAsString(mockUser)));
  }
}
