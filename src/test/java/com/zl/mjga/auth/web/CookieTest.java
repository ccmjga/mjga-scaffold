package com.zl.mjga.auth.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.zl.mjga.auth.application.SignAppService;
import com.zl.mjga.auth.config.Jwt;
import com.zl.mjga.common.security.HttpFireWallConfig;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(value = {SignController.class, Jwt.class})
@Import({HttpFireWallConfig.class})
public class CookieTest {

  @Value("${jwt.cookie-name}")
  private String jwtCookieName;

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration-min}")
  private int expirationMin;

  @MockBean private SignAppService signAppService;

  @Autowired private MockMvc mockMvc;

  @Test
  @WithMockUser
  void signIn_givenValidHttpRequest_shouldCreateExpectCookie() throws Exception {
    String stubUsername = "test_04cb017e1fe6";
    String stubPassword = "test_567472858b8c";
    SignInVm signInVm = new SignInVm();
    signInVm.setUsername(stubUsername);
    signInVm.setPassword(stubPassword);

    when(signAppService.signIn(signInVm)).thenReturn(1L);
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
        .andExpect(cookie().exists(jwtCookieName))
        .andExpect(cookie().maxAge(jwtCookieName, expirationMin * 60))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void signOut_givenValidHttpRequest_shouldRemoveCookie() throws Exception {
    when(signAppService.signIn(any(SignInVm.class))).thenReturn(1L);
    MvcResult signInMvcResult =
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
            .andReturn();
    Cookie cookie = signInMvcResult.getResponse().getCookie(jwtCookieName);

    mockMvc
        .perform(
            post("/auth/sign-out")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookie)
                .content(
                    """
                    {
                      "username": "test_04cb017e1fe6",
                      "password": "test_567472858b8c"
                    }
                    """)
                .with(csrf()))
        .andExpect(cookie().exists(jwtCookieName))
        .andExpect(cookie().maxAge(jwtCookieName, 0))
        .andExpect(status().isOk());
  }
}
