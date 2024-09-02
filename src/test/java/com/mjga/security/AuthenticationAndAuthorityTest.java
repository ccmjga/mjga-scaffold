package com.mjga.security;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mjga.config.security.CookieJwt;
import com.mjga.config.security.HttpFireWallConfig;
import com.mjga.config.security.UserDetailsServiceImpl;
import com.mjga.config.security.WebSecurityConfig;
import com.mjga.controller.SignController;
import com.mjga.controller.UserRolePermissionController;
import com.mjga.dto.sign.SignInDto;
import com.mjga.modle.urp.EPermission;
import com.mjga.service.SignService;
import com.mjga.service.UserRolePermissionService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = {SignController.class, UserRolePermissionController.class})
@Import({WebSecurityConfig.class, HttpFireWallConfig.class})
public class AuthenticationAndAuthorityTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private SignService signService;

  @MockBean private CookieJwt cookieJwt;

  @MockBean private UserDetailsServiceImpl userDetailsService;

  @MockBean private UserRolePermissionService userRolePermissionService;

  @Test
  public void givenRequestOnPublicService_shouldSucceedWith200() throws Exception {
    when(signService.signIn(any(SignInDto.class))).thenReturn(1L);
    mockMvc
        .perform(
            post("/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "username": "test_04cb017e1fe6",
                      "password": "test_567472858b8c"
                    }
                     """)
                .with(csrf()))
        .andExpect(status().isOk());
  }

  @Test
  public void givenUnAuthenticateRequestOnPrivateService_shouldFailedWith401() throws Exception {
    mockMvc.perform(post("/auth/sign-out").with(csrf())).andExpect(status().isUnauthorized());
  }

  @Test
  public void givenUnAuthorityRequestOnPrivateService_shouldFailedWith403() throws Exception {
    // Arrange
    User stubUserNoPermission =
        new User("test_04cb017e1fe6", "test_567472858b8c", Collections.emptyList());
    when(cookieJwt.extractJwt(any(HttpServletRequest.class))).thenReturn(("u9T05Tg3ULCgRn8ja2"));
    when(cookieJwt.getSubject(any(String.class))).thenReturn(("4J2HX9r5JcXg0BT"));
    when(cookieJwt.verifyToken(any(String.class))).thenReturn(Boolean.TRUE);
    when(userDetailsService.loadUserByUsername(any(String.class))).thenReturn(stubUserNoPermission);

    // Act and Assert
    mockMvc
        .perform(
            post("/urp/role/1/bind-permission")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        [1]
                         """))
        .andExpect(status().isForbidden());
  }

  @Test
  public void givenAuthorityRequestOnPrivateService_shouldSuccessWith200() throws Exception {
    // Arrange
    User stubUserNoPermission =
        new User(
            "test_04cb017e1fe6",
            "test_567472858b8c",
            List.of(new SimpleGrantedAuthority(EPermission.WRITE_USER_ROLE_PERMISSION.toString())));
    when(cookieJwt.extractJwt(any(HttpServletRequest.class))).thenReturn(("u9T05Tg3ULCgRn8ja2"));
    when(cookieJwt.getSubject(any(String.class))).thenReturn(("4J2HX9r5JcXg0BT"));
    when(cookieJwt.verifyToken(any(String.class))).thenReturn(Boolean.TRUE);
    when(userDetailsService.loadUserByUsername(any(String.class))).thenReturn(stubUserNoPermission);

    // Act and Assert
    mockMvc
        .perform(
            post("/urp/role/1/bind-permission")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        [1]
                         """))
        .andExpect(status().isOk());
  }
}
