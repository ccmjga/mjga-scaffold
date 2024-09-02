package com.mjga.dto.sign;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SignInDto {

  @NotEmpty private String username;

  @NotEmpty private String password;
}
