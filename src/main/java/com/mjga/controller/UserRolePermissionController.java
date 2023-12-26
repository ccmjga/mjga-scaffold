package com.mjga.controller;

import com.mjga.dto.PageRequestDto;
import com.mjga.dto.PageResponseDto;
import com.mjga.dto.urp.*;
import com.mjga.service.UserRolePermissionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/urp")
@RequiredArgsConstructor
public class UserRolePermissionController {

  private final UserRolePermissionService userRolePermissionService;

  @PreAuthorize("hasAuthority(T(com.mjga.modle.urp.EPermission).READ_USER_ROLE_PERMISSION)")
  @GetMapping("/user")
  @ResponseStatus(HttpStatus.OK)
  PageResponseDto<List<UserRolePermissionDto>> queryUser(
      @ModelAttribute PageRequestDto pageRequestDto, @ModelAttribute UserQueryDto userQueryDto) {
    return userRolePermissionService.pageQueryUser(pageRequestDto, userQueryDto);
  }

  @PreAuthorize("hasAuthority(T(com.mjga.modle.urp.EPermission).READ_USER_ROLE_PERMISSION)")
  @GetMapping("/role")
  @ResponseStatus(HttpStatus.OK)
  PageResponseDto<List<RoleDto>> queryRole(
      @ModelAttribute PageRequestDto pageRequestDto, @ModelAttribute RoleQueryDto roleQueryDto) {
    return userRolePermissionService.pageQueryRole(pageRequestDto, roleQueryDto);
  }

  @PreAuthorize("hasAuthority(T(com.mjga.modle.urp.EPermission).READ_USER_ROLE_PERMISSION)")
  @GetMapping("/permission")
  @ResponseStatus(HttpStatus.OK)
  PageResponseDto<List<PermissionDto>> queryPermission(
      @ModelAttribute PageRequestDto pageRequestDto,
      @ModelAttribute PermissionQueryDto permissionQueryDto) {
    return userRolePermissionService.pageQueryPermission(pageRequestDto, permissionQueryDto);
  }

  @PreAuthorize("hasAuthority(T(com.mjga.modle.urp.EPermission).WRITE_USER_ROLE_PERMISSION)")
  @PostMapping("/user/{userId}/bind-role")
  @ResponseStatus(HttpStatus.OK)
  void bindRoleToUser(@PathVariable Long userId, @RequestBody List<Long> roleIdList) {
    userRolePermissionService.bindRoleToUser(userId, roleIdList);
  }

  @PreAuthorize("hasAuthority(T(com.mjga.modle.urp.EPermission).WRITE_USER_ROLE_PERMISSION)")
  @PostMapping("/role/{roleId}/bind-permission")
  @ResponseStatus(HttpStatus.OK)
  void bindPermissionToRole(@PathVariable Long roleId, @RequestBody List<Long> permissionIdList) {
    userRolePermissionService.bindPermissionToRole(roleId, permissionIdList);
  }
}
