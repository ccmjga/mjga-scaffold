package com.zl.mjga.user.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zl.mjga.common.security.HttpFireWallConfig;
import com.zl.mjga.user.RolePermissionShortInput;
import com.zl.mjga.user.domain.model.PermissionDraft;
import com.zl.mjga.user.domain.model.Role;
import com.zl.mjga.user.domain.model.RoleDraft;
import com.zl.mjga.user.domain.model.RoleFetcher;
import com.zl.mjga.user.domain.repository.RoleRepository;
import java.util.List;
import org.babyfish.jimmer.jackson.ImmutableModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = {RoleController.class})
@Import({HttpFireWallConfig.class, JacksonSerializeConfig.class})
public class RoleMvcTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private RoleRepository roleRepository;

  @Test
  @WithMockUser
  void getRoles_shouldReturnAllRoles() throws Exception {
    List<Role> mockRoles =
        List.of(
            RoleDraft.$.produce(
                draft -> {
                  draft.setCode("code");
                  draft.setName("name");
                  draft.setPermissions(
                      List.of(
                          PermissionDraft.$.produce(
                              roleDraft -> {
                                roleDraft.setId(1L);
                                roleDraft.setName("name");
                                roleDraft.setCode("code");
                              })));
                }));
    ObjectMapper mapper = new ObjectMapper().registerModule(new ImmutableModule());
    when(roleRepository.fetchRoleComplexBy(any(RoleFetcher.class))).thenReturn(mockRoles);
    mockMvc
        .perform(get("/roles"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(mapper.writeValueAsString(mockRoles)));
  }

  @Test
  @WithMockUser
  void upsert_withValidContent_shouldReturnSuccess() throws Exception {
    doNothing().when(roleRepository).upsert(any(RolePermissionShortInput.class));
    mockMvc
        .perform(
            post("/roles/upsert")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(
"""
                    {
                      "code": "code_b15da84c8f1d",
                      "name": "name_b15da84c8f1d",
                      "permissionIds": [1,2]
                    }

""")
                .with(csrf()))
        .andExpect(status().isOk());
  }
}
