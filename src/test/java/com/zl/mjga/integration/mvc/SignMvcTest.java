package com.zl.mjga.integration.mvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zl.mjga.config.security.HttpFireWallConfig;
import com.zl.mjga.config.security.Jwt;
import com.zl.mjga.controller.SignController;
import com.zl.mjga.dto.sign.SignInDto;
import com.zl.mjga.service.SignService;
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

  @MockBean private SignService signService;

  @MockBean private Jwt jwt;

  @Autowired private MockMvc mockMvc;

  @Test
  @WithMockUser
  void signIn_givenValidHttpRequest_shouldSucceedWith200() throws Exception {
    String stubUsername = "test_04cb017e1fe6";
    String stubPassword = "test_567472858b8c";
    SignInDto signInDto = new SignInDto();
    signInDto.setUsername(stubUsername);
    signInDto.setPassword(stubPassword);

    when(signService.signIn(signInDto)).thenReturn(1L);
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
    SignInDto signInDto = new SignInDto();
    signInDto.setUsername(stubUsername);
    signInDto.setPassword(stubPassword);

    when(signService.signIn(signInDto)).thenReturn(1L);
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

    when(signService.signIn(signInDto)).thenReturn(1L);
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
