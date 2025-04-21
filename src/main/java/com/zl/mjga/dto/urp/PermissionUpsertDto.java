package com.zl.mjga.dto.urp;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PermissionUpsertDto {
  private Long id;
  @NotEmpty private String code;
  @NotEmpty private String name;
}
