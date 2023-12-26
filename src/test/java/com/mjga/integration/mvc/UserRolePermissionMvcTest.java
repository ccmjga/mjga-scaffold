package com.mjga.integration.mvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mjga.config.security.HttpFireWallConfig;
import com.mjga.controller.UserRolePermissionController;
import com.mjga.dto.PageRequestDto;
import com.mjga.dto.PageResponseDto;
import com.mjga.dto.urp.*;
import com.mjga.service.UserRolePermissionService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


@WebMvcTest(value = {UserRolePermissionController.class})
@Import({HttpFireWallConfig.class})
class UserRolePermissionMvcTest {

  @MockBean private UserRolePermissionService userRolePermissionService;
  @Autowired private MockMvc mockMvc;

  @Test
  @WithMockUser
  void pageQueryUser_givenValidHttpRequest_shouldSucceedWith200AndReturnJson() throws Exception {
    String stubUsername = "test_04cb017e1fe6";
    UserRolePermissionDto stubUserRolePermissionDto = new UserRolePermissionDto();
    stubUserRolePermissionDto.setId(1L);
    stubUserRolePermissionDto.setUsername(stubUsername);
    when(userRolePermissionService.pageQueryUser(
            PageRequestDto.of(0, 5), new UserQueryDto(stubUsername)))
        .thenReturn(new PageResponseDto<>(1, List.of(stubUserRolePermissionDto)));
    mockMvc
        .perform(
            get(String.format("/urp/user?page=0&size=5&username=%s", stubUsername))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].username").value(stubUsername));
  }

  @Test
  @WithMockUser
  void pageQueryRole_givenValidHttpRequest_shouldSucceedWith200AndReturnJson() throws Exception {
    Long stubUserId = 1L;
    Long stubRoleId = 1L;
    String stubRoleCode = "UZ1Ej9vx5y8L4";
    String stubRoleName = "B90KM9Pw2ZH9P8OAS";
    RoleQueryDto stubRoleQueryDto = new RoleQueryDto();
    stubRoleQueryDto.setUserId(stubUserId);
    stubRoleQueryDto.setRoleId(stubRoleId);
    stubRoleQueryDto.setRoleCode(stubRoleCode);
    stubRoleQueryDto.setRoleName(stubRoleName);
    RoleDto stubRoleDto = new RoleDto();
    stubRoleDto.setId(1L);
    stubRoleDto.setName(stubRoleName);
    stubRoleDto.setCode(stubRoleCode);
    stubRoleDto.setPermissionDtoList(
        List.of(new PermissionDto(1L, "9VWU1nmU89zEVH", "9VWU1nmU89zEVH")));
    when(userRolePermissionService.pageQueryRole(PageRequestDto.of(0, 5), stubRoleQueryDto))
        .thenReturn(new PageResponseDto<>(1, List.of(stubRoleDto)));

    mockMvc
        .perform(
            get(String.format(
                    "/urp/role?page=0&size=5&userId=%s&roleId=%s&roleCode=%s&roleName=%s",
                    stubUserId, stubRoleId, stubRoleCode, stubRoleName))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].name").value(stubRoleName));
  }

  @Test
  @WithMockUser
  void pageQueryPermission_givenValidHttpRequest_shouldSucceedWith200AndReturnJson()
      throws Exception {
    Long stubRoleId = 1L;
    Long stubPermissionId = 1L;
    String stubPermissionCode = "UZ1Ej9vx5y8L4";
    String stubPermissionName = "B90KM9Pw2ZH9P8OAS";
    PermissionQueryDto stubPermissionQueryDto = new PermissionQueryDto();
    stubPermissionQueryDto.setRoleId(stubRoleId);
    stubPermissionQueryDto.setPermissionId(stubPermissionId);
    stubPermissionQueryDto.setPermissionCode(stubPermissionCode);
    stubPermissionQueryDto.setPermissionName(stubPermissionName);

    PermissionDto stubPermissionDto = new PermissionDto();
    stubPermissionDto.setId(stubPermissionId);
    stubPermissionDto.setName(stubPermissionName);
    stubPermissionDto.setCode(stubPermissionCode);
    when(userRolePermissionService.pageQueryPermission(
            PageRequestDto.of(0, 5), stubPermissionQueryDto))
        .thenReturn(new PageResponseDto<>(1, List.of(stubPermissionDto)));

    mockMvc
        .perform(
            get(String.format(
                    "/urp/permission?page=0&size=5&roleId=%s&permissionId=%s&permissionCode=%s&permissionName=%s",
                    stubRoleId, stubPermissionId, stubPermissionCode, stubPermissionName))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].name").value(stubPermissionName));
  }

  @Test
  @WithMockUser
  void bindRoleToUser_givenValidHttpRequest_shouldSucceedWith200() throws Exception {
    Long stubUserId = 1L;
    Long stubRoleId1 = 1L;
    Long stubRoleId2 = 2L;
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(String.format("/urp/user/%s/bind-role", stubUserId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                       [1,2]
                    """)
                .with(csrf()))
        .andExpect(status().isOk());
    verify(userRolePermissionService, times(1))
        .bindRoleToUser(stubUserId, List.of(stubRoleId1, stubRoleId2));
  }

  @Test
  @WithMockUser
  void bindPermissionToRole_givenValidHttpRequest_shouldSucceedWith200() throws Exception {
    Long stubRoleId = 1L;
    Long stubPermissionId1 = 1L;
    Long stubPermissionId2 = 2L;
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(String.format("/urp/role/%s/bind-permission", stubRoleId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                       [1,2]
                    """)
                .with(csrf()))
        .andExpect(status().isOk());
    verify(userRolePermissionService, times(1))
        .bindPermissionToRole(stubRoleId, List.of(stubPermissionId1, stubPermissionId2));
  }
}
