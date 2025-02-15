package com.zl.mjga.auth.web;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zl.mjga.auth.application.SignAppService;
import com.zl.mjga.auth.config.Jwt;
import com.zl.mjga.common.security.HttpFireWallConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = {SignController.class})
@Import({HttpFireWallConfig.class})
class SignMvcTest {

  @MockBean private SignAppService signAppService;

  @Autowired private MockMvc mockMvc;

  @MockBean private Jwt jwt;

  @Test
  @WithMockUser
  void signIn_givenValidHttpRequest_shouldSucceedWith200() throws Exception {
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
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void signIn_givenInValidHttpRequest_shouldFailedWith400() throws Exception {
    String stubUsername = "test_04cb017e1fe6";
    String stubPassword = "test_567472858b8c";
    SignInVm SignInVm = new SignInVm();
    SignInVm.setUsername(stubUsername);
    SignInVm.setPassword(stubPassword);

    when(signAppService.signIn(SignInVm)).thenReturn(1L);
    mockMvc
        .perform(
            post("/auth/sign-in")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(
                    """
                    {
                      "username": "test_04cb017e1fe6",
                      "password": "test_567472858b8c"
                    }
                    """)
                .with(csrf()))
        .andExpect(status().isBadRequest());

    when(signAppService.signIn(SignInVm)).thenReturn(1L);
    mockMvc
        .perform(
            post("/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "username": "test_04cb017e1fe6"
                    }
                    """)
                .with(csrf()))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void signUp_givenValidHttpRequest_shouldSucceedWith200() throws Exception {
    mockMvc
        .perform(
            post("/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "username": "test_04cb017e1fe6",
                      "password": "test_567472858b8c"
                    }
                    """)
                .with(csrf()))
        .andExpect(status().isCreated());
  }

  @Test
  @WithMockUser
  void signUp_givenInValidHttpRequest_shouldFailedWith400() throws Exception {
    mockMvc
        .perform(
            post("/auth/sign-up")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(
                    """
                    {
                      "username": "test_04cb017e1fe6",
                      "password": "test_567472858b8c"
                    }
                    """)
                .with(csrf()))
        .andExpect(status().isBadRequest());

    mockMvc
        .perform(
            post("/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "username": "test_04cb017e1fe6"
                    }
                    """)
                .with(csrf()))
        .andExpect(status().isBadRequest());
  }
}
