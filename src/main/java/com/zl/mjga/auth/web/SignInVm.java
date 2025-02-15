package com.zl.mjga.auth.web;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SignInVm {

  @NotEmpty private String username;

  @NotEmpty private String password;
}
