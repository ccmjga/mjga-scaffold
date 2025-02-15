package com.zl.mjga.auth.web;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zl.mjga.auth.application.SignAppService;
import com.zl.mjga.auth.application.UserDetailsAppService;
import com.zl.mjga.auth.config.Jwt;
import com.zl.mjga.auth.config.WebSecurityConfig;
import com.zl.mjga.common.security.HttpFireWallConfig;
import com.zl.mjga.user.domain.model.User;
import com.zl.mjga.user.domain.model.UserDraft;
import com.zl.mjga.user.domain.model.UserFetcher;
import com.zl.mjga.user.domain.repository.UserAggregateRepository;
import com.zl.mjga.user.web.JacksonSerializeConfig;
import com.zl.mjga.user.web.UserController;
import com.zl.mjga.user.web.UserPageQueryVm;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.babyfish.jimmer.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = {SignController.class, UserController.class})
@Import({WebSecurityConfig.class, HttpFireWallConfig.class, JacksonSerializeConfig.class})
public class AuthenticationAndAuthorityTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private SignAppService signAppService;

  @MockBean private Jwt jwt;

  @MockBean private UserDetailsAppService userDetailsAppService;

  @MockBean private UserAggregateRepository userAggregateRepository;

  @Test
  public void givenRequestOnPublicService_shouldSucceedWith200() throws Exception {
    when(signAppService.signIn(any(SignInVm.class))).thenReturn(1L);
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
    User mockUser =
        UserDraft.$.produce(
            (userDraft) -> {
              userDraft.setId(1L);
              userDraft.setUsername("admin");
              userDraft.setPassword("admin");
            });
    when(jwt.extract(any(HttpServletRequest.class))).thenReturn(("u9T05Tg3ULCgRn8ja2"));
    when(jwt.getSubject(any(String.class))).thenReturn(("4J2HX9r5JcXg0BT"));
    when(jwt.verify(any(String.class))).thenReturn(Boolean.TRUE);

    when(userDetailsAppService.loadUserByUsername(any(String.class)))
        .thenReturn(
            new org.springframework.security.core.userdetails.User(
                "test_04cb017e1fe6", "test_567472858b8c", List.of()));

    Page<com.zl.mjga.user.domain.model.User> mockPageResult = new Page<>(List.of(mockUser), 2, 1);
    when(userAggregateRepository.fetchAggregateWithPageBy(
            any(UserFetcher.class), any(UserPageQueryVm.class), anyInt(), anyInt()))
        .thenReturn(mockPageResult);
    // Act and Assert
    mockMvc
        .perform(
            get("/users/search?pageIndex=1&pageSize=10&enable=true")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
        .andExpect(status().isForbidden());
  }

  //
  @Test
  public void givenAuthorityRequestOnPrivateService_shouldSuccessWith200() throws Exception {
    User mockUser =
        UserDraft.$.produce(
            (userDraft) -> {
              userDraft.setId(1L);
              userDraft.setUsername("admin");
              userDraft.setPassword("admin");
            });
    when(jwt.extract(any(HttpServletRequest.class))).thenReturn(("u9T05Tg3ULCgRn8ja2"));
    when(jwt.getSubject(any(String.class))).thenReturn(("4J2HX9r5JcXg0BT"));
    when(jwt.verify(any(String.class))).thenReturn(Boolean.TRUE);

    when(userDetailsAppService.loadUserByUsername(any(String.class)))
        .thenReturn(
            new org.springframework.security.core.userdetails.User(
                "test_04cb017e1fe6",
                "test_567472858b8c",
                List.of(new SimpleGrantedAuthority("READ_USER_ROLE_PERMISSION"))));

    Page<com.zl.mjga.user.domain.model.User> mockPageResult = new Page<>(List.of(mockUser), 2, 1);
    when(userAggregateRepository.fetchAggregateWithPageBy(
            any(UserFetcher.class), any(UserPageQueryVm.class), anyInt(), anyInt()))
        .thenReturn(mockPageResult);
    // Act and Assert
    mockMvc
        .perform(
            get("/users/search?pageIndex=1&pageSize=10&enable=true")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
        .andExpect(status().isOk());
  }
}
