package com.zl.mjga.auth.web;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SignUpVm {
  @NotEmpty private String username;

  @NotEmpty private String password;
}
