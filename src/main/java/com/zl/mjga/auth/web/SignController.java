package com.zl.mjga.auth.web;

import com.zl.mjga.auth.application.SignAppService;
import com.zl.mjga.auth.config.Jwt;
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

  private final SignAppService signAppService;

  private final Jwt jwt;

  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/sign-in")
  void signIn(
      HttpServletRequest request,
      HttpServletResponse response,
      @RequestBody @Valid SignInVm signInVm) {
    jwt.makeToken(request, response, String.valueOf(signAppService.signIn(signInVm)));
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/sign-up")
  void signUp(@RequestBody @Valid SignUpVm signUpVm) {
    signAppService.signUp(signUpVm);
  }

  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/sign-out")
  void signOut(HttpServletRequest request, HttpServletResponse response) {
    jwt.removeToken(request, response);
  }
}
