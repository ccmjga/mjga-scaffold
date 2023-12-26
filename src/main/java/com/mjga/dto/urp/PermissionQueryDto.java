package com.mjga.dto.urp;

import java.util.List;
import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class PermissionQueryDto {

  private Long roleId;
  private Long permissionId;
  private String permissionCode;
  private String permissionName;
  private List<Long> permissionIdList;
}
