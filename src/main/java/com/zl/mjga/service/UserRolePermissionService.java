package com.zl.mjga.service;

import com.zl.mjga.dto.PageRequestDto;
import com.zl.mjga.dto.PageResponseDto;
import com.zl.mjga.dto.urp.*;
import com.zl.mjga.exception.BusinessException;
import com.zl.mjga.model.urp.ERole;
import com.zl.mjga.repository.*;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.generated.mjga.tables.pojos.*;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserRolePermissionService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final UserRoleMapRepository userRoleMapRepository;
  private final PermissionRepository permissionRepository;
  private final RolePermissionMapRepository rolePermissionMapRepository;

  public void upsertUser(UserUpsertDto userUpsertDto) {
    User user = new User();
    BeanUtils.copyProperties(userUpsertDto, user);
    userRepository.merge(user);
  }

  public void upsertRole(RoleUpsertDto roleUpsertDto) {
    Role role = new Role();
    BeanUtils.copyProperties(roleUpsertDto, role);
    roleRepository.merge(role);
  }

  public void upsertPermission(PermissionUpsertDto permissionUpsertDto) {
    Permission permission = new Permission();
    BeanUtils.copyProperties(permissionUpsertDto, permission);
    permissionRepository.merge(permission);
  }

  public PageResponseDto<List<UserRolePermissionDto>> pageQueryUserAgg(
      PageRequestDto pageRequestDto, UserQueryDto userQueryDto) {
    Result<Record> userRecords = userRepository.pageFetchUserAggBy(pageRequestDto, userQueryDto);
    if (userRecords.isEmpty()) {
      return PageResponseDto.empty();
    }
    List<UserRolePermissionDto> nestedUserAgg = userRecords.into(UserRolePermissionDto.class);
    return new PageResponseDto<>(
        userRecords.get(0).getValue("total_user", Integer.class), nestedUserAgg);
  }

  public @Nullable UserRolePermissionDto queryUniqueUserWithRolePermission(Long userId) {
    return userRepository.getUserAggDtoBy(userId);
  }

  public PageResponseDto<List<RoleDto>> pageQueryRoleAgg(
      PageRequestDto pageRequestDto, RoleQueryDto roleQueryDto) {
    Result<Record> roleRecords = roleRepository.pageFetchRoleAggBy(pageRequestDto, roleQueryDto);
    if (roleRecords.isEmpty()) {
      return PageResponseDto.empty();
    }
    List<RoleDto> roleAgg = roleRecords.into(RoleDto.class);
    return new PageResponseDto<>(roleRecords.get(0).getValue("total_role", Integer.class), roleAgg);
  }

  public PageResponseDto<List<PermissionDto>> pageQueryPermission(
      PageRequestDto pageRequestDto, PermissionQueryDto permissionQueryDto) {
    Result<Record> permissionRecords =
        permissionRepository.pageFetchPermissionBy(pageRequestDto, permissionQueryDto);
    if (permissionRecords.isEmpty()) {
      return PageResponseDto.empty();
    }
    List<PermissionDto> permissions = permissionRecords.into(PermissionDto.class);
    return new PageResponseDto<>(
        permissionRecords.get(0).getValue("total_permission", Integer.class), permissions);
  }

  @Transactional(rollbackFor = Throwable.class)
  public void bindPermissionToRole(Long roleId, List<Long> permissionIdList) {
    rolePermissionMapRepository.deleteByRoleId(roleId);
    if (CollectionUtils.isEmpty(permissionIdList)) {
      return;
    }
    List<Permission> permissions = permissionRepository.selectByPermissionIdIn(permissionIdList);
    if (CollectionUtils.isEmpty(permissions)) {
      throw new BusinessException("bind permission not exist");
    }
    List<RolePermissionMap> permissionMapList =
        permissions.stream()
            .map(
                (permission -> {
                  RolePermissionMap rolePermissionMap = new RolePermissionMap();
                  rolePermissionMap.setRoleId(roleId);
                  rolePermissionMap.setPermissionId(permission.getId());
                  return rolePermissionMap;
                }))
            .collect(Collectors.toList());
    rolePermissionMapRepository.insert(permissionMapList);
  }

  @Transactional(rollbackFor = Throwable.class)
  public void bindRoleToUser(Long userId, List<Long> roleIdList) {
    userRoleMapRepository.deleteByUserId(userId);
    if (CollectionUtils.isEmpty(roleIdList)) {
      return;
    }
    List<Role> roles = roleRepository.selectByRoleIdIn(roleIdList);
    if (CollectionUtils.isEmpty(roles)) {
      throw new BusinessException("bind role not exist");
    }
    List<UserRoleMap> userRoleMapList =
        roles.stream()
            .map(
                (role -> {
                  UserRoleMap userRoleMap = new UserRoleMap();
                  userRoleMap.setUserId(userId);
                  userRoleMap.setRoleId(role.getId());
                  return userRoleMap;
                }))
            .collect(Collectors.toList());
    userRoleMapRepository.insert(userRoleMapList);
  }

  @Transactional(rollbackFor = Throwable.class)
  public void bindRoleModuleToUser(Long userId, List<ERole> eRoleList) {
    bindRoleToUser(
        userId,
        roleRepository
            .selectByRoleCodeIn(eRoleList.stream().map(Enum::name).collect(Collectors.toList()))
            .stream()
            .map(Role::getId)
            .toList());
  }

  public boolean isRoleDuplicate(String roleCode, String name) {
    return roleRepository.fetchOneByCode(roleCode) != null
        || roleRepository.fetchOneByName(name) != null;
  }

  public boolean isUsernameDuplicate(String username) {
    return userRepository.fetchOneByUsername(username) != null;
  }

  public boolean isPermissionDuplicate(String code, String name) {
    return permissionRepository.fetchOneByCode(code) != null
        || permissionRepository.fetchOneByName(name) != null;
  }
}
