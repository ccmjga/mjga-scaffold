package com.zl.mjga.integration.mvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zl.mjga.config.security.HttpFireWallConfig;
import com.zl.mjga.controller.UserRolePermissionController;
import com.zl.mjga.dto.PageRequestDto;
import com.zl.mjga.dto.PageResponseDto;
import com.zl.mjga.dto.urp.UserQueryDto;
import com.zl.mjga.dto.urp.UserRolePermissionDto;
import com.zl.mjga.repository.PermissionRepository;
import com.zl.mjga.repository.RoleRepository;
import com.zl.mjga.repository.UserRepository;
import com.zl.mjga.service.UserRolePermissionService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = {UserRolePermissionController.class})
@Import({HttpFireWallConfig.class})
public class JacksonAnnotationMvcTest {

  @MockBean private UserRolePermissionService userRolePermissionService;
  @Autowired private MockMvc mockMvc;
  @MockBean private UserRepository userRepository;
  @MockBean private RoleRepository roleRepository;
  @MockBean private PermissionRepository permissionRepository;

  @Test
  @WithMockUser
  void fieldWithJsonWriteOnlyAnnotation_whenResponseIncludeField_responseJsonShouldNotExist()
      throws Exception {
    String stubUsername = "test_04cb017e1fe6";
    String stubPassword = "y1hxAC0V0e4B3s8sJ";
    UserRolePermissionDto stubUserRolePermissionDto = new UserRolePermissionDto();
    stubUserRolePermissionDto.setId(1L);
    stubUserRolePermissionDto.setUsername(stubUsername);
    stubUserRolePermissionDto.setPassword(stubPassword);
    when(userRolePermissionService.pageQueryUser(
            PageRequestDto.of(0, 5), new UserQueryDto(stubUsername)))
        .thenReturn(new PageResponseDto<>(1, List.of(stubUserRolePermissionDto)));
    mockMvc
        .perform(
            get(String.format("/urp/users?page=0&size=5&username=%s", stubUsername))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].username").value(stubUsername))
        .andExpect(jsonPath("$.data[0].password").doesNotExist());
  }

  @Test
  @WithMockUser
  void dateFieldWithFormatAnnotation_whenResponseIncludeField_fieldShouldBeExpectDataFormat()
      throws Exception {
    OffsetDateTime stubCreateDateTime =
        OffsetDateTime.of(2023, 12, 2, 1, 1, 1, 0, OffsetDateTime.now().getOffset());
    UserRolePermissionDto stubUserRolePermissionDto = new UserRolePermissionDto();
    stubUserRolePermissionDto.setCreateTime(stubCreateDateTime);
    when(userRolePermissionService.pageQueryUser(
            any(PageRequestDto.class), any(UserQueryDto.class)))
        .thenReturn(new PageResponseDto<>(1, List.of(stubUserRolePermissionDto)));
    mockMvc
        .perform(
            get(String.format("/urp/users?page=0&size=5&username=%s", "7bF3mcNVTj6P6v2"))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].createTime").value("2023-12-02 01:01:01"));
  }
}
