package com.mjga.dto.sign;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;


@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SignUpDto {
  @NotEmpty private String username;

  @NotEmpty private String password;
}
