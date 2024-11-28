package com.zl.mjga.dto.urp;

import java.util.List;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RoleQueryDto {

  private Long userId;
  private Long roleId;
  private String roleCode;
  private String roleName;
  private List<Long> roleIdList;
}
