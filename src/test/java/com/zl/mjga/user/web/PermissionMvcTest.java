package com.zl.mjga.user.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zl.mjga.common.security.HttpFireWallConfig;
import com.zl.mjga.user.PermissionInput;
import com.zl.mjga.user.domain.model.Permission;
import com.zl.mjga.user.domain.model.PermissionDraft;
import com.zl.mjga.user.domain.repository.PermissionRepository;
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

@WebMvcTest(value = {PermissionController.class})
@Import({HttpFireWallConfig.class, JacksonSerializeConfig.class})
public class PermissionMvcTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private PermissionRepository permissionRepository;

  @Test
  @WithMockUser
  void getPermissions_shouldReturnAllPermission() throws Exception {
    List<Permission> mockPermissions =
        List.of(
            PermissionDraft.$.produce(
                draft -> {
                  draft.setCode("codeA");
                  draft.setName("nameA");
                  draft.setId(1L);
                }),
            PermissionDraft.$.produce(
                draft -> {
                  draft.setCode("codeB");
                  draft.setName("nameB");
                  draft.setId(2L);
                }));
    when(permissionRepository.findAll()).thenReturn(mockPermissions);
    ObjectMapper mapper = new ObjectMapper().registerModule(new ImmutableModule());
    mockMvc
        .perform(get("/permissions"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(content().json(mapper.writeValueAsString(mockPermissions)));
  }

  @Test
  @WithMockUser
  void upsert_withValidContent_shouldReturnStatus200() throws Exception {
    doNothing().when(permissionRepository).upsert(any(PermissionInput.class));
    mockMvc
        .perform(
            post("/permissions/upsert")
                .content(
                    """
                    {
                      "code": "code_b15da84c8f1d",
                      "name": "name_768ec5210a9a"
                    }
                    """)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .with(csrf()))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void upsert_withInValidContent_shouldReturnStatus403() throws Exception {
    doNothing().when(permissionRepository).upsert(any(PermissionInput.class));
    mockMvc
        .perform(
            post("/permissions/upsert")
                .content(
                    """
                    {
                      "code": "code_b15da84c8f1d",
                    }
                    """)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .with(csrf()))
        .andExpect(status().isBadRequest());
  }
}
