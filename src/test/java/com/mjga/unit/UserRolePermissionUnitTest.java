package com.mjga.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.jooq.generated.tables.Permission.PERMISSION;
import static org.jooq.generated.tables.Role.ROLE;
import static org.jooq.generated.tables.User.USER;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

import com.mjga.dto.PageRequestDto;
import com.mjga.dto.PageResponseDto;
import com.mjga.dto.urp.*;
import com.mjga.exception.BusinessException;
import com.mjga.repository.*;
import com.mjga.service.UserRolePermissionService;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.generated.tables.pojos.Permission;
import org.jooq.generated.tables.pojos.Role;
import org.jooq.generated.tables.pojos.RolePermissionMap;
import org.jooq.generated.tables.pojos.UserRoleMap;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserRolePermissionUnitTest {
  @InjectMocks @Spy private UserRolePermissionService userRolePermissionService;

  @Mock private UserRepository userRepository;

  @Mock private RoleRepository roleRepository;
  @Mock private UserRoleMapRepository userRoleMapRepository;
  @Mock private PermissionRepository permissionRepository;
  @Mock private RolePermissionMapRepository rolePermissionMapRepository;

  private static DSLContext dslContext;

  private static MockConnection connection;

  @BeforeAll
  static void setUp() {
    MockDataProvider provider = ctx -> new MockResult[0];
    connection = new MockConnection(provider);
    dslContext = DSL.using(connection, SQLDialect.POSTGRES);
  }

  @AfterAll
  static void setDown() throws SQLException {
    connection.close();
  }

  @Test
  void pageQueryUser_selected2UserShouldReturnUserRolePermissionAndTotal() {
    // arrange
    Long stubUserId1 = 1L;
    String stubUserName1 = "yEJVEJBC2j9PGi";
    String stubUserPassword1 = "c21W03p1201jCz";

    Long stubUserId2 = 2L;
    String stubUserName2 = "1jpziB82YUs3Jbh";
    String stubUserPassword2 = "c21W03p1201jCz";

    Long stubRoleId = 1L;
    String stubRoleName = "54X3UYRzx0wiy9";
    String stubRoleCode = "mzxN6WQA3AErI";

    Long stubPermissionId = 1L;
    String stubPermissionName = "BNOz058K9EWE";
    String stubPermissionCode = "BNOz058K9EWE";

    Result<Record> mockResult =
        dslContext.newResult(
            List.of(
                USER.ID,
                USER.USERNAME,
                USER.PASSWORD,
                USER.ENABLE,
                USER.CREATE_TIME,
                DSL.field("total_user", Integer.class)));
    mockResult.add(
        dslContext
            .newRecord(
                USER.ID,
                USER.USERNAME,
                USER.PASSWORD,
                USER.ENABLE,
                USER.CREATE_TIME,
                DSL.field("total_user", Integer.class))
            .values(stubUserId1, stubUserName2, stubUserPassword2, true, null, 2));
    mockResult.add(
        dslContext
            .newRecord(
                USER.ID,
                USER.USERNAME,
                USER.PASSWORD,
                USER.ENABLE,
                USER.CREATE_TIME,
                DSL.field("total_user", Integer.class))
            .values(stubUserId2, stubUserName2, stubUserPassword2, true, null, 2));
    UserRolePermissionDto mockUserRolePermissionDto1 = new UserRolePermissionDto();
    RoleDto mockRoleDto = new RoleDto();
    mockRoleDto.setId(stubRoleId);
    mockRoleDto.setCode(stubRoleCode);
    mockRoleDto.setName(stubRoleName);
    PermissionDto permissionDto = new PermissionDto();
    permissionDto.setId(stubPermissionId);
    permissionDto.setCode(stubPermissionCode);
    permissionDto.setName(stubPermissionName);
    mockRoleDto.getPermissions().add(permissionDto);
    mockUserRolePermissionDto1.setId(stubUserId1);
    mockUserRolePermissionDto1.setUsername(stubUserName1);
    mockUserRolePermissionDto1.setPassword(stubUserPassword1);
    mockUserRolePermissionDto1.getRoles().add(mockRoleDto);

    UserRolePermissionDto mockUserRolePermissionDto2 = new UserRolePermissionDto();
    mockUserRolePermissionDto2.setId(stubUserId2);
    mockUserRolePermissionDto2.setUsername(stubUserName2);
    mockUserRolePermissionDto2.setPassword(stubUserPassword2);

    doReturn(Optional.of(mockUserRolePermissionDto1))
        .when(userRolePermissionService)
        .queryUniqueUserWithRolePermission(stubUserId1);
    //    when(rbacService.queryUniqueUserWithDetail(stubUserId1))
    //        .thenReturn(Optional.of(mockUserRolePermissionDto1));
    doReturn(Optional.of(mockUserRolePermissionDto2))
        .when(userRolePermissionService)
        .queryUniqueUserWithRolePermission(stubUserId2);
    when(userRepository.pageFetchBy(any(PageRequestDto.class), any(UserQueryDto.class)))
        .thenReturn(mockResult);

    // action
    PageResponseDto<List<UserRolePermissionDto>> result =
        userRolePermissionService.pageQueryUser(
            PageRequestDto.of(0, 10), new UserQueryDto(stubUserName2));

    // assert
    List<UserRolePermissionDto> userRolePermissionDtoList = result.getData();
    assertThat(result.getTotal()).isEqualTo(2L);
    assertThat(userRolePermissionDtoList.size()).isEqualTo(2L);
    assertThat(userRolePermissionDtoList.get(0).getRoles().size()).isEqualTo(1L);
    assertThat(userRolePermissionDtoList.get(1).getRoles().size()).isEqualTo(0L);
    assertThat(userRolePermissionDtoList.get(1).getUsername()).isEqualTo(stubUserName2);
    assertThat(userRolePermissionDtoList.get(0).getRoles().get(0).getName())
        .isEqualTo(stubRoleName);
    assertThat(userRolePermissionDtoList.get(0).getRoles().get(0).getPermissions().get(0).getCode())
        .isEqualTo(stubPermissionCode);
  }

  @Test
  void queryUser_selected0Row_shouldReturnEmptyListWithPage() {
    Result<Record> mockResult =
        dslContext.newResult(
            List.of(
                USER.ID,
                USER.USERNAME,
                USER.PASSWORD,
                USER.ENABLE,
                USER.CREATE_TIME,
                DSL.field("total_user", Integer.class)));
    when(userRepository.pageFetchBy(any(PageRequestDto.class), any(UserQueryDto.class)))
        .thenReturn(mockResult);
    PageResponseDto<List<UserRolePermissionDto>> result =
        userRolePermissionService.pageQueryUser(
            PageRequestDto.of(0, 10), new UserQueryDto("agydCO1Yi99a"));
    assertThat(result.getTotal()).isEqualTo(0);
    assertThat(result.getData()).isNull();
  }

  @Test
  void
      queryUniqueUserWithRolePermission_whenUserHasBeenFound_shouldReturnUniqueUserRolePermissionDto() {
    Long stubUserId = 1L;
    String stubUserName = "yEJVEJBC2j9PGi";
    String stubUserPassword = "c21W03p1201jCz";
    Long stubRoleId = 1L;
    String stubRoleName = "G5N6Xkjg0i9UC4Vltv";
    String stubRoleCode = "G5N6Xkjg0i9UC4Vltv";

    Long stubPermissionId = 1L;
    String stubPermissionName = "BNOz058K9EWE";
    String stubPermissionCode = "BNOz058K9EWE";

    Long stubPermissionId2 = 2L;
    String stubPermissionName2 = "u6igc4BctOm1ON6X";
    String stubPermissionCode2 = "u6igc4BctOm1ON6X";

    UserRolePermissionDto mockResult = new UserRolePermissionDto();
    mockResult.setUsername(stubUserName);
    mockResult.setPassword(stubUserPassword);
    mockResult.setId(stubRoleId);
    mockResult.setRoles(
        List.of(
            new RoleDto(
                stubRoleId,
                stubRoleName,
                stubRoleCode,
                List.of(
                    new PermissionDto(stubPermissionId, stubPermissionName, stubPermissionCode),
                    new PermissionDto(
                        stubPermissionId2, stubPermissionName2, stubPermissionCode2)))));

    when(userRepository.fetchUniqueUserDtoWithNestedRolePermissionBy(stubUserId))
        .thenReturn(mockResult);
    Optional<UserRolePermissionDto> userRolePermissionDto =
        userRolePermissionService.queryUniqueUserWithRolePermission(stubUserId);
    assertThat(userRolePermissionDto.isPresent()).isTrue();
    UserRolePermissionDto result = userRolePermissionDto.get();
    assertThat(result.getRoles().size()).isEqualTo(1L);
    assertThat(result.getRoles().get(0).getPermissions().size()).isEqualTo(2L);
    assertThat(result.getUsername()).isEqualTo(stubUserName);
    assertThat(result.getRoles().get(0).getPermissions().get(0).getName())
        .isEqualTo(stubPermissionName);
    assertThat(result.getRoles().get(0).getPermissions().get(0).getCode())
        .isEqualTo(stubPermissionCode);
  }

  @Test
  void queryUniqueUserWithRolePermission_whenUserNotFound_shouldReturnEmpty() {
    UserRolePermissionDto mockResult = null;
    when(userRepository.fetchUniqueUserDtoWithNestedRolePermissionBy(anyLong()))
        .thenReturn(mockResult);
    Optional<UserRolePermissionDto> userRolePermissionDtoOptional =
        userRolePermissionService.queryUniqueUserWithRolePermission(1L);
    assertThat(userRolePermissionDtoOptional.isEmpty()).isTrue();
  }

  @Test
  void pageQueryRole_givenUserId_shouldReturnRelevantRolePermissionDto() {
    // arrange
    UserRoleMap stubUserRoleMap = new UserRoleMap();
    stubUserRoleMap.setId(1L);
    stubUserRoleMap.setUserId(1L);
    stubUserRoleMap.setRoleId(1L);
    UserRoleMap stubUserRoleMap2 = new UserRoleMap();
    stubUserRoleMap2.setId(2L);
    stubUserRoleMap2.setUserId(1L);
    stubUserRoleMap2.setRoleId(2L);
    when(userRoleMapRepository.fetchByUserId(any(Long.class)))
        .thenReturn(List.of(stubUserRoleMap, stubUserRoleMap2));

    Result<Record> mockRoleResult =
        dslContext.newResult(
            List.of(ROLE.ID, ROLE.NAME, ROLE.CODE, DSL.field("total_role", Integer.class)));
    mockRoleResult.addAll(
        List.of(
            dslContext
                .newRecord(ROLE.ID, ROLE.NAME, ROLE.CODE, DSL.field("total_role", Integer.class))
                .values(1L, "G5N6Xkjg0i9UC4Vltv", "G5N6Xkjg0i9UC4Vltv", 2),
            dslContext
                .newRecord(ROLE.ID, ROLE.NAME, ROLE.CODE, DSL.field("total_role", Integer.class))
                .values(2L, "JszWMfgI1HpN2hON90", "JszWMfgI1HpN2hON90", 2)));
    when(roleRepository.pageFetchBy(any(PageRequestDto.class), any(RoleQueryDto.class)))
        .thenReturn(mockRoleResult);

    RoleDto mockRoleDto1 = new RoleDto();
    mockRoleDto1.setId(1L);
    mockRoleDto1.setName("ghe41YG2FSbc");
    mockRoleDto1.setCode("ghe41YG2FSbc");
    mockRoleDto1
        .getPermissions()
        .addAll(
            List.of(
                new PermissionDto(1L, "4QBYM93jI5c3jxuZW", "4QBYM93jI5c3jxuZW"),
                new PermissionDto(2L, "r0he6iMMHBze", "r0he6iMMHBze")));
    RoleDto mockRoleDto2 = new RoleDto();
    mockRoleDto2.setId(2L);
    mockRoleDto2.setName("AfRcdGk0zc15Lz2F");
    mockRoleDto2.setCode("AfRcdGk0zc15Lz2F");
    doReturn(Optional.of(mockRoleDto1))
        .when(userRolePermissionService)
        .queryUniqueRoleWithPermission(1L);
    doReturn(Optional.of(mockRoleDto2))
        .when(userRolePermissionService)
        .queryUniqueRoleWithPermission(2L);

    // action & assert
    RoleQueryDto roleQueryDto = new RoleQueryDto();
    roleQueryDto.setUserId(1L);
    PageResponseDto<List<RoleDto>> pageResult =
        userRolePermissionService.pageQueryRole(PageRequestDto.of(0, 5), roleQueryDto);

    assertThat(pageResult.getTotal()).isEqualTo(2L);
    List<RoleDto> roleResult = pageResult.getData();
    assertThat(roleResult.size()).isEqualTo(2);
    assertThat(roleResult.get(0).getId()).isEqualTo(1L);
    assertThat(roleResult.get(1).getId()).isEqualTo(2L);
    assertThat(roleResult.get(0).getPermissions().get(0).getId()).isEqualTo(1L);
    assertThat(roleResult.get(1).getPermissions().size()).isEqualTo(0L);
  }

  @Test
  void pageQueryRole_whenRoleNotFound_shouldReturnEmpty() {
    Result<Record> mockRoleResult =
        dslContext.newResult(
            List.of(ROLE.ID, ROLE.NAME, ROLE.CODE, DSL.field("total_role", Integer.class)));
    when(roleRepository.pageFetchBy(any(PageRequestDto.class), any(RoleQueryDto.class)))
        .thenReturn(mockRoleResult);
    RoleQueryDto roleQueryDto = new RoleQueryDto();
    PageResponseDto<List<RoleDto>> pageResult =
        userRolePermissionService.pageQueryRole(PageRequestDto.of(0, 5), roleQueryDto);
    assertThat(pageResult.getTotal()).isEqualTo(0L);

    roleQueryDto.setUserId(1L);
    when(userRoleMapRepository.fetchByUserId(any(Long.class))).thenReturn(new ArrayList<>());
    PageResponseDto<List<RoleDto>> pageResult2 =
        userRolePermissionService.pageQueryRole(PageRequestDto.of(0, 5), roleQueryDto);
    assertThat(pageResult2.getTotal()).isEqualTo(0L);
  }

  @Test
  void pageQueryPermission_givenRoleId_shouldReturnPermissionDto() {
    RolePermissionMap stubRolePermissionMap = new RolePermissionMap();
    stubRolePermissionMap.setId(1L);
    stubRolePermissionMap.setRoleId(1L);
    stubRolePermissionMap.setPermissionId(1L);
    RolePermissionMap stubRolePermissionMap2 = new RolePermissionMap();
    stubRolePermissionMap2.setId(2L);
    stubRolePermissionMap2.setRoleId(1L);
    stubRolePermissionMap2.setPermissionId(2L);

    when(rolePermissionMapRepository.fetchByRoleId(any(Long.class)))
        .thenReturn(List.of(stubRolePermissionMap, stubRolePermissionMap2));
    Result<Record> mockRoleResult =
        dslContext.newResult(
            List.of(
                PERMISSION.ID,
                PERMISSION.NAME,
                PERMISSION.CODE,
                DSL.field("total_permission", Integer.class)));
    mockRoleResult.addAll(
        List.of(
            dslContext
                .newRecord(
                    PERMISSION.ID,
                    PERMISSION.NAME,
                    PERMISSION.CODE,
                    DSL.field("total_permission", Integer.class))
                .values(1L, "vP0dKiHJpMsi", "vP0dKiHJpMsi", 2),
            dslContext
                .newRecord(
                    PERMISSION.ID,
                    PERMISSION.NAME,
                    PERMISSION.CODE,
                    DSL.field("total_permission", Integer.class))
                .values(2L, "NHQED41jQQ4C1IgG", "NHQED41jQQ4C1IgG", 2)));
    when(permissionRepository.pageFetchBy(any(PageRequestDto.class), any(PermissionQueryDto.class)))
        .thenReturn(mockRoleResult);
    PermissionQueryDto permissionQueryDto = new PermissionQueryDto();
    permissionQueryDto.setRoleId(1L);
    PageResponseDto<List<PermissionDto>> pageResult =
        userRolePermissionService.pageQueryPermission(PageRequestDto.of(0, 5), permissionQueryDto);
    assertThat(pageResult.getTotal()).isEqualTo(2L);
    List<PermissionDto> permissionResult = pageResult.getData();
    assertThat(permissionResult.get(0).getId()).isEqualTo(1L);
    assertThat(permissionResult.get(1).getId()).isEqualTo(2L);
  }

  @Test
  void pageQueryPermission_permissionNotFound_shouldReturnEmpty() {
    Result<Record> mockRoleResult =
        dslContext.newResult(
            List.of(
                PERMISSION.ID,
                PERMISSION.NAME,
                PERMISSION.CODE,
                DSL.field("total_permission", Integer.class)));
    when(permissionRepository.pageFetchBy(any(PageRequestDto.class), any(PermissionQueryDto.class)))
        .thenReturn(mockRoleResult);
    PermissionQueryDto permissionQueryDto = new PermissionQueryDto();
    PageResponseDto<List<PermissionDto>> pageResult =
        userRolePermissionService.pageQueryPermission(PageRequestDto.of(0, 5), permissionQueryDto);

    assertThat(pageResult.getTotal()).isEqualTo(0L);
    permissionQueryDto.setRoleId(1L);
    when(rolePermissionMapRepository.fetchByRoleId(any(Long.class))).thenReturn(new ArrayList<>());
    PageResponseDto<List<PermissionDto>> pageResult2 =
        userRolePermissionService.pageQueryPermission(PageRequestDto.of(0, 5), permissionQueryDto);
    assertThat(pageResult2.getTotal()).isEqualTo(0);
  }

  @Test
  void bindRoleToUser_givenExistRoleId_shouldInsertUserRoleMapWithNoError() {
    Long stubUserId = 1L;
    Role stubRole = new Role();
    stubRole.setId(1L);
    stubRole.setCode("rfX60vSEwfYyMuu");
    stubRole.setName("wl5xx78tqlIZo3JE");
    UserRoleMap userRoleMap = new UserRoleMap();
    userRoleMap.setUserId(stubUserId);
    userRoleMap.setRoleId(stubRole.getId());

    when(roleRepository.selectByRoleIdIn(anyList())).thenReturn(List.of(stubRole));
    userRolePermissionService.bindRoleToUser(stubUserId, List.of(stubRole.getId()));
    verify(userRoleMapRepository, times(1)).deleteByUserId(anyLong());
    verify(userRoleMapRepository, times(1)).insert(Mockito.eq(List.of(userRoleMap)));
  }

  @Test
  void bindRoleToUser_givenNotExistRoleId_shouldRunError() {
    Long stubUserId = 1L;
    Role stubRole = new Role();
    stubRole.setId(1L);
    stubRole.setCode("rfX60vSEwfYyMuu");
    stubRole.setName("wl5xx78tqlIZo3JE");

    when(roleRepository.selectByRoleIdIn(anyList())).thenReturn(new ArrayList<>());
    assertThatThrownBy(
            () -> userRolePermissionService.bindRoleToUser(stubUserId, List.of(stubRole.getId())))
        .isInstanceOf(BusinessException.class)
        .hasMessage("bind role not exist");
    verify(userRoleMapRepository, times(1)).deleteByUserId(anyLong());
    verify(userRoleMapRepository, times(0)).insert(anyList());
  }

  @Test
  void bindRoleToUser_givenNotExistRoleId_shouldUnbindUsersRole() {
    Long stubUserId = 1L;

    userRolePermissionService.bindRoleToUser(stubUserId, new ArrayList<>());
    verify(userRoleMapRepository, times(1)).deleteByUserId(anyLong());
    verify(userRoleMapRepository, times(0)).insert(anyList());
  }

  @Test
  void bindPermissionToRole_givenExistPermissionId_shouldInsertRolePermissionMapWithNoError() {
    Long stubRoleId = 1L;
    Permission stubPermission = new Permission();
    stubPermission.setId(1L);
    stubPermission.setCode("rfX60vSEwfYyMuu");
    stubPermission.setName("wl5xx78tqlIZo3JE");
    RolePermissionMap rolePermissionMap = new RolePermissionMap();
    rolePermissionMap.setRoleId(stubRoleId);
    rolePermissionMap.setPermissionId(stubPermission.getId());

    when(permissionRepository.selectByPermissionIdIn(anyList()))
        .thenReturn(List.of(stubPermission));
    userRolePermissionService.bindPermissionToRole(stubRoleId, List.of(stubPermission.getId()));
    verify(rolePermissionMapRepository, times(1)).deleteByRoleId(anyLong());
    verify(rolePermissionMapRepository, times(1)).insert(Mockito.eq(List.of(rolePermissionMap)));
  }

  @Test
  void bindPermissionToRole_givenNotExistPermissionId_shouldRunError() {
    Long stubRoleId = 1L;
    Permission stubPermission = new Permission();
    stubPermission.setId(1L);
    stubPermission.setCode("rfX60vSEwfYyMuu");
    stubPermission.setName("wl5xx78tqlIZo3JE");

    when(permissionRepository.selectByPermissionIdIn(anyList())).thenReturn(new ArrayList<>());
    assertThatThrownBy(
            () ->
                userRolePermissionService.bindPermissionToRole(
                    stubRoleId, List.of(stubPermission.getId())))
        .isInstanceOf(BusinessException.class)
        .hasMessage("bind permission not exist");
    verify(rolePermissionMapRepository, times(1)).deleteByRoleId(anyLong());
    verify(rolePermissionMapRepository, times(0)).insert(Mockito.eq(new ArrayList<>()));
  }

  @Test
  void bindPermissionToRole_givenEmptyPermission_shouldUnbindRolesPermission() {
    Long stubRoleId = 1L;

    userRolePermissionService.bindPermissionToRole(stubRoleId, new ArrayList<>());
    verify(rolePermissionMapRepository, times(1)).deleteByRoleId(anyLong());
    verify(rolePermissionMapRepository, times(0)).insert(Mockito.eq(new ArrayList<>()));
  }
}
