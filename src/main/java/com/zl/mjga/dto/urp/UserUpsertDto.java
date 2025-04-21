package com.zl.mjga.dto.urp;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserUpsertDto {
  private Long id;
  @NotEmpty private String username;
  @NotEmpty private String password;
  @NotNull private Boolean enable;
}
