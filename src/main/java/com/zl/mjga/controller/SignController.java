package com.zl.mjga.controller;

import com.zl.mjga.config.security.Jwt;
import com.zl.mjga.dto.sign.SignInDto;
import com.zl.mjga.dto.sign.SignUpDto;
import com.zl.mjga.service.SignService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class SignController {

  private final SignService signService;

  private final Jwt jwt;

  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/sign-in")
  void signIn(
      HttpServletRequest request,
      HttpServletResponse response,
      @RequestBody @Valid SignInDto signInDto) {
    jwt.makeToken(request, response, String.valueOf(signService.signIn(signInDto)));
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/sign-up")
  void signUp(@RequestBody @Valid SignUpDto signUpDto) {
    signService.signUp(signUpDto);
  }

  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/sign-out")
  void signOut(HttpServletRequest request, HttpServletResponse response) {
    jwt.removeToken(request, response);
  }
}
